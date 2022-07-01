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
import org.junit.jupiter.api.Test;

/**
 * This test demonstrates that you cannot just rely on node types to ensure that
 * queries will be performant
 */
class OakSearchTest_01_QueryNodeType extends OakSearchITBase {

    public OakSearchTest_01_QueryNodeType() throws ClientException, IOException, InterruptedException {
        super();
    }

    @Override
    protected void updateIndex() throws ClientException, IOException, InterruptedException {
        // TODO Probably should do something here
    }

    @Test
    void canQueryByType() throws ClientException, IOException {
        // First, we'll execute a query and it should work!
        TestQueryResult result1 = super.runQuery(adminAuthor, "SELECT * FROM [test:content] AS s", 100);
        assertEquals(100, result1.getResults().size());
        // But what happens as soon as we add a constraint?

        // This should fail while the first works because the first query was ONLY using
        // the nodeType index and could just return the first N number of nodes it found
        // of the type for a constrained query without an index, Oak has to first get
        // the nodes of the type from the node type index, then read them in memory to
        // determine which match the constraints of the query.
        //
        // Add the following line to the updateIndex() method and re-run this test:

        // doUpdateIndex("01_QueryNodeType/indexDef.json");

        // Review the 01_QueryNodeType/indexDef.json file and note that it will
        // create an index of the property test:iteration for the node type test:content
        TestQueryResult result2 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=9", 100);
        assertEquals(100, result2.getResults().size());

        // Note that you can see this on the AEM Query Performance Tool:
        // http://localhost:4502/libs/granite/operations/content/diagnosistools/queryPerformance.html
        // The Query plan tells us how the query is executed, specifically:
        // - It's using the index:
        // lucene:testContentLucene(/oak:index/testContentLucene)
        // - Filtering from the index by path: +:ancestors:/tests
        // - Querying against the indexed property: +test:iteration:[9 TO 9]
        assertEquals(
                "[test:content] as [s] /* lucene:testContentLucene(/oak:index/testContentLucene) +:ancestors:/tests +test:iteration:[9 TO 9] where (isdescendantnode([s], [/tests])) and ([s].[test:iteration] = 9) */",
                result2.getPlan());
    }

}
