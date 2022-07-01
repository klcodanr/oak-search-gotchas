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
 * This test demonstrates that even if you index all the properties, that some
 * queries may require null checks
 */
class OakSearchTest_04_Nulls extends OakSearchITBase {

    public OakSearchTest_04_Nulls() throws ClientException, IOException, InterruptedException {
        super();
    }

    @Override
    protected void updateIndex() throws ClientException, IOException, InterruptedException {
        // TODO Probably should do something here
    }

    @Test
    void canQueryForNull() throws ClientException, IOException {
        // In OakSearchTest_02_QueryNodeType, we ran this query and it worked:
        TestQueryResult result1 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=9 AND [test:item]=9",
                100);
        assertEquals(100, result1.getResults().size());
        // But what happens if we wanted to know what nodes that don't have the value 7
        // for the property [test:item]. This is actually tricky as
        // negation in Oak search only works when matching nodes that have a value for
        // the field, e.g. test:item equals some other value. If you
        // want to match nodes where test:item does not equal 7 including items where
        // test:item is not set, you need to add an OR with a null check

        // The query below may fail becase while the test:.* properties are indexed,
        // they are not checked for null status. This actually
        // is unpredictable because it depends on the order of the nodes within the
        // index, making these issues hard to spot without real data
        // and extensive testing

        // To fix this, add the following line to the beforeClass method:
        // doUpdateIndex("04_NullProperties/indexDef.json");

        // This will load a new index definition which will include null checks
        for (int i = 0; i < 10; i++) {
            TestQueryResult result2 = super.runQuery(adminAuthor,
                    "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND ([test:iteration] IS NULL OR [test:iteration]<>"
                            + i + ")",
                    100);
            assertEquals(100, result2.getResults().size());
        }

        // It's worth noting that this comes with a cost. Null fields in Oak Lucene
        // indexes add another field to each document
        // with the properties which support null checks which are null for that
        // document. Especially if this is pervasive in a document
        // this can increase the total index size, so if there's a way to NOT require
        // null checks, that's the better option
    }
}
