/*
 *  Copyright 2022 - Dan Klco
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.danklco.blog.oaksearch.it.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This test demonstrates that you cannot just rely on node types to ensure that
 * queries will be performant
 */
class OakSearchTest_01_QueryNodeType extends OakSearchITBase {

    @BeforeAll
    static void beforeAll() throws Exception{
        OakSearchITBase.setup();
    }

    // First, we'll execute a query just using the node type and it should work!
    @Test
    void canQueryByType() throws Exception {
        TestQueryResult result = super.runQuery(adminAuthor, "SELECT * FROM [test:content] AS s", 100);
        assertEquals("", result.getCaughtException());
        assertEquals(100, result.getResults().size());
    }

    // In fact I can even query against certain values and get back results
    @Test
    void someOtherQuerysWork() throws Exception {
        TestQueryResult result = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests])", 100);
        assertEquals("", result.getCaughtException());
        assertEquals(100, result.getResults().size());
        int succeeded = 0;
        for (int i = 1; i < 10; i++) {
            result = super.runQuery(adminAuthor,
                    "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=" + i,
                    100);
            if (StringUtils.isBlank(result.getCaughtException())) {
                succeeded++;
            }
        }
        assertTrue(succeeded > 0);
    }

    // But what happens as soon as we add a constraint?

    // This should fail while the first works because the first query was ONLY using
    // the nodeType index and could just return the first N number of nodes it found
    // of the type for a constrained query without an index, Oak has to first get
    // the nodes of the type from the node type index, then read them in memory to
    // determine which match the constraints of the query.
    //
    // Add the following line to the beforeAll() method and re-run this test:

    // updateIndex("01_QueryNodeType/indexDef.json");

    // Review the 01_QueryNodeType/indexDef.json file and note that it will
    // create an index of the property test:iteration for the node type test:content
    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void notAllQueryValuesWork(int iteration) throws Exception {
        TestQueryResult result = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=" + iteration,
                100);
        assertEquals("", result.getCaughtException());
        assertEquals(100, result.getResults().size());

        // You can verify this on the AEM Query Performance Tool:
        // http://localhost:4502/libs/granite/operations/content/diagnosistools/queryPerformance.html
        // The Query plan tells us how the query is executed, specifically:
        // - It's using the index:
        // lucene:testContentLucene(/oak:index/testContentLucene)
        // - Filtering from the index by path: +:ancestors:/tests
        // - Querying against the indexed property: +test:iteration:[{NUM} TO {NUM}]
        assertEquals(
                "[test:content] as [s] /* lucene:testContentLucene(/oak:index/testContentLucene) +:ancestors:/tests +test:iteration:["
                        + iteration + " TO " + iteration
                        + "] where (isdescendantnode([s], [/tests])) and ([s].[test:iteration] = " + iteration + ") */",
                result.getPlan());
    }

}
