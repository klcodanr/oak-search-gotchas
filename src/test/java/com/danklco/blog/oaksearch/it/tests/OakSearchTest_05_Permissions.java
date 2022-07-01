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

/**
 * This test demonstrates that even if you index all the properties, that
 * complex permission models may still be problematic
 */
class OakSearchTest_05_Permissions extends OakSearchITBase {

    @BeforeAll
    static void beforeAll() throws Exception {
        OakSearchITBase.setup();
    }

    @Test
    void canQueryByType() throws ClientException, IOException {
        // In OakSearchTest_01_QueryNodeType, we ran this query and it worked:
        TestQueryResult result1 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s", 100);
        assertEquals(100, result1.getResults().size());
        // But what happens if someone who isn't an administrator ran this query?
        // What happens if they could only see say the last folder of nodes
        // found in the results?

        String path = "/tests";

        // The query below will fail because although Oak retrieves the results
        // from the index, it has no way to determine whether or not the calling
        // user has access without retrieving the nodes.
        TestQueryResult result2 = super.runQuery(limitedAccess,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([" + path + "])", 100);
        assertEquals(100, result2.getResults().size());

        // Unfortunately, really the only fix for this problem is to set up the
        // query so that it won't query over Nodes the user cannot see. To resolve
        // this issue, change the path to:
        // String path = "/tests/it-9";

    }
}
