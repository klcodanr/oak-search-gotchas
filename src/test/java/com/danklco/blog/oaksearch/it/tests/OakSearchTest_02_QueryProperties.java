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

import java.io.IOException;

import org.apache.sling.testing.clients.ClientException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This test demonstrates that even if you index _some_ properties queries can
 * still be slow / fail with traversal
 */
class OakSearchTest_02_QueryProperties extends OakSearchITBase {

    @BeforeAll
    static void beforeAll() throws Exception {
        OakSearchITBase.setup();
    }

    // In OakSearchTest_01_QueryNodeType, we ran this query and it worked
    @Test
    void canQueryByAProperty() throws ClientException, IOException {
        TestQueryResult result = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=9", 100);
        assertEquals("", result.getCaughtException());
        assertEquals(100, result.getResults().size());
    }

    // But what happens as soon as we add another constraint?

    // The query below will fail becase while the test:iteration property is
    // indexed, the query engine still needs to read 100000+ nodes to find nodes
    // matching BOTH test:iteration and test:item

    // To fix this, add the following line to the beforeAll method:
    // updateIndex("02_QueryProperties/indexDef.json");

    // This will load a new index definition which will index all properties
    // starting with test: on test:content
    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void canQueryAgainstMultipleProperties(int item) throws ClientException, IOException {
        TestQueryResult result2 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=9 AND [test:item]="
                        + item,
                100);

        assertEquals(100, result2.getResults().size());

        // You can validate this on the AEM Query Performance Tool:
        // http://localhost:4502/libs/granite/operations/content/diagnosistools/queryPerformance.html
        // The Query plan tells us how the query is executed, specifically:
        // - It's using the index:
        // lucene:testContentLucene(/oak:index/testContentLucene)
        // - Filtering from the index by path: +:ancestors:/tests
        // - Querying against the indexed properties: +test:item:[9 TO 9] and
        // +test:iteration:[{num} TO {num}]
        assertEquals(
                "[test:content] as [s] /* lucene:testContentLucene(/oak:index/testContentLucene) +:ancestors:/tests +test:item:["
                        + item + " TO " + item
                        + "] +test:iteration:[9 TO 9] where (isdescendantnode([s], [/tests])) and ([s].[test:iteration] = 9) and ([s].[test:item] = "
                        + item + ") */",
                result2.getPlan());
    }
}
