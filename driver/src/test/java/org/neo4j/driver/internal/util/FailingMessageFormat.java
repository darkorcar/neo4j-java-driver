/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.internal.util;

import io.netty.util.internal.PlatformDependent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.neo4j.driver.internal.messaging.FailureMessage;
import org.neo4j.driver.internal.messaging.Message;
import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.MessageHandler;
import org.neo4j.driver.internal.messaging.PackStreamMessageFormatV1;
import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.packstream.PackOutput;

public class FailingMessageFormat implements MessageFormat
{
    private final MessageFormat delegate;
    private final AtomicReference<Throwable> writerThrowableRef = new AtomicReference<>();
    private final AtomicReference<Throwable> readerThrowableRef = new AtomicReference<>();
    private final AtomicReference<FailureMessage> readerFailureRef = new AtomicReference<>();

    public FailingMessageFormat()
    {
        this( new PackStreamMessageFormatV1() );
    }

    public FailingMessageFormat( MessageFormat delegate )
    {
        this.delegate = delegate;
    }

    public void makeWriterThrow( Throwable error )
    {
        writerThrowableRef.set( error );
    }

    public void makeReaderThrow( Throwable error )
    {
        readerThrowableRef.set( error );
    }

    public void makeReaderFail( FailureMessage failureMsg )
    {
        readerFailureRef.set( failureMsg );
    }

    @Override
    public Writer newWriter( PackOutput output, boolean byteArraySupportEnabled )
    {
        return new ThrowingWriter( delegate.newWriter( output, byteArraySupportEnabled ), writerThrowableRef );
    }

    @Override
    public Reader newReader( PackInput input )
    {
        return new ThrowingReader( delegate.newReader( input ), readerThrowableRef, readerFailureRef );
    }

    @Override
    public int version()
    {
        return delegate.version();
    }

    private static class ThrowingWriter implements MessageFormat.Writer
    {
        final MessageFormat.Writer delegate;
        final AtomicReference<Throwable> throwableRef;

        ThrowingWriter( Writer delegate, AtomicReference<Throwable> throwableRef )
        {
            this.delegate = delegate;
            this.throwableRef = throwableRef;
        }

        @Override
        public Writer write( Message msg ) throws IOException
        {
            Throwable error = throwableRef.getAndSet( null );
            if ( error != null )
            {
                PlatformDependent.throwException( error );
            }
            else
            {
                return delegate.write( msg );
            }
            return this;
        }
    }

    private static class ThrowingReader implements MessageFormat.Reader
    {
        final MessageFormat.Reader delegate;
        final AtomicReference<Throwable> throwableRef;
        final AtomicReference<FailureMessage> failureRef;

        ThrowingReader( Reader delegate, AtomicReference<Throwable> throwableRef,
                AtomicReference<FailureMessage> failureRef )
        {
            this.delegate = delegate;
            this.throwableRef = throwableRef;
            this.failureRef = failureRef;
        }

        @Override
        public void read( MessageHandler handler ) throws IOException
        {
            Throwable error = throwableRef.getAndSet( null );
            if ( error != null )
            {
                PlatformDependent.throwException( error );
                return;
            }

            FailureMessage failureMsg = failureRef.getAndSet( null );
            if ( failureMsg != null )
            {
                failureMsg.dispatch( handler );
                return;
            }

            delegate.read( handler );
        }
    }
}
