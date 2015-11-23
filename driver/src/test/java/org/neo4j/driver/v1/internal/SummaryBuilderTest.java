/**
 * Copyright (c) 2002-2015 "Neo Technology,"
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
package org.neo4j.driver.v1.internal;

import org.junit.Test;

import org.neo4j.driver.v1.ResultSummary;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.UpdateStatistics;
import org.neo4j.driver.v1.internal.summary.SimpleUpdateStatistics;
import org.neo4j.driver.v1.internal.summary.SummaryBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class SummaryBuilderTest
{
    @Test
    public void shouldReturnEmptyStatisticsIfNotProvided() throws Throwable
    {
        // Given
        SummaryBuilder builder = new SummaryBuilder( mock( Statement.class ) );

        // When
        ResultSummary summary = builder.build();
        UpdateStatistics stats = summary.updateStatistics();

        // Then
        assertEquals( stats, new SimpleUpdateStatistics( 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ) );
    }


    @Test
    public void shouldReturnNullIfNoPlanProfileProvided() throws Throwable
    {
        // Given
        SummaryBuilder builder = new SummaryBuilder( mock( Statement.class ) );

        // When
        ResultSummary summary = builder.build();

        // Then
        assertThat( summary.hasPlan(), equalTo( false ) );
        assertThat( summary.hasProfile(), equalTo( false ) );
        assertNull( summary.plan() );
        assertNull( summary.profile() );
    }


    @Test
    public void shouldReturnNullIfNoStatementTypeProvided() throws Throwable
    {
        // Given
        SummaryBuilder builder = new SummaryBuilder( mock( Statement.class ) );

        // When
        ResultSummary summary = builder.build();

        // Then
        assertNull( summary.statementType() );
    }
}