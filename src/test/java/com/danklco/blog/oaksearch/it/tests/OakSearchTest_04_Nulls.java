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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;

import org.apache.sling.testing.clients.ClientException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This test demonstrates that even if you index all the properties, that some
 * queries may require null checks
 */
class OakSearchTest_04_Nulls extends OakSearchITBase {

    @BeforeAll
    static void beforeAll() throws Exception {
        OakSearchITBase.setup();
    }

    // In OakSearchTest_02_QueryNodeType, we ran this query and it worked:
    @Test
    void canQueryProperties() throws ClientException, IOException {
        TestQueryResult result = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=9 AND [test:item]=9",
                100);
        assertEquals("", result.getCaughtException());
        assertEquals(100, result.getResults().size());
    }

    // But what happens if we wanted to know what nodes that don't have the value 7
    // for the property [test:item]. This is actually tricky as
    // negation in Oak search only works when matching nodes that have a value for
    // the field, e.g. test:item equals some other value. If you
    // want to match nodes where test:item does not equal 7 including items where
    // test:item is not set, you need to add an OR with a null check
    @Test
    void negationWorksOnNullValues() throws ClientException, IOException {
        TestQueryResult result = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISCHILDNODE([/tests])",
                100);
        assertEquals("", result.getCaughtException());
        assertEquals(9, result.getResults().size());
        for (String path : result.getResults()) {
            JsonNode json = adminAuthor.doGetJson(path, 0, 200);
            assertFalse(json.has("test:item"));
        }

        // logically we'd think this would result in 9 results, but no! 
        result = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISCHILDNODE([/tests]) AND [test:item]<>9",
                100);
        assertEquals(9, result.getResults().size());
        // to fix this, change the query to:
        // SELECT * FROM [test:content] AS s WHERE ISCHILDNODE([/tests]) AND ([test:item]<>9 OR [test:item] IS NULL)
    }

    // Unfortunately, with the additional null check the test below will fail
    // because while the test:.* properties are indexed, they are not checked for
    // null status

    // To fix this, add the following line to the beforeAll method:
    // updateIndex("04_NullProperties/indexDef.json");

    // This will load a new index definition which will include null checks
    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void canQueryForNulls(int iteration) throws ClientException, IOException {

        TestQueryResult result = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND ([test:iteration] IS NULL OR [test:iteration]<>"
                        + iteration + ")",
                100);
        assertEquals("", result.getCaughtException());
        assertEquals(100, result.getResults().size());

        // Null checks comes with a cost. Null fields in Oak Lucene indexes add another
        // field to each document with the properties which support null checks which
        // are null for that document. Especially if this is pervasive in a document
        // this can increase the total index size, so if there's a way to NOT require
        // null checks, that's the better option
    }

}
