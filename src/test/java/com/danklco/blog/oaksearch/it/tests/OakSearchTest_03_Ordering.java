package com.danklco.blog.oaksearch.it.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.sling.testing.clients.ClientException;
import org.junit.jupiter.api.Test;

/**
 * This test demonstrates that even if you index all the properties, that you
 * still need to consider ordering
 */
class OakSearchTest_03_Ordering extends OakSearchITBase {

    public OakSearchTest_03_Ordering() throws ClientException, IOException, InterruptedException {
        super();
    }

    @Override
    protected void updateIndex() throws ClientException, IOException, InterruptedException {
        // TODO Probably should do something here
    }

    @Test
    void canOrder() throws ClientException, IOException {
        // In OakSearchTest_01_QueryNodeType, we ran this query and it worked:
        TestQueryResult result1 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=9", 100);
        assertEquals(100, result1.getResults().size());
        // But what happens if we wanted to order the nodes by the test:child property?

        // The query below will fail becase while the test:.* properties are indexed,
        // they are not ordered

        // To fix this, add the following line to the beforeClass method:
        // doUpdateIndex("03_Ordering/indexDef.json");

        // This will load a new index definition which will index all properties
        // starting with test: on test:content
        TestQueryResult result2 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=9 ORDER BY [test:child]",
                100);
        assertEquals(100, result2.getResults().size());

        // Note that you can see this on the AEM Query Performance Tool:
        // http://localhost:4502/libs/granite/operations/content/diagnosistools/queryPerformance.html
        // The Query plan tells us how the query is executed, specifically:
        // - It's using the index:
        // lucene:testContentLucene(/oak:index/testContentLucene)
        // - Filtering from the index by path: +:ancestors:/tests
        // - Ordering by the test:child property: ordering:[{ propertyName : test:child,
        // propertyType : UNDEFINED, order : ASCENDING }]
        // - Querying against the indexed properties: +test:iteration:[9 TO 9]
        assertEquals(
                "[test:content] as [s] /* lucene:testContentLucene(/oak:index/testContentLucene) +:ancestors:/tests +test:iteration:[9 TO 9] ordering:[{ propertyName : test:child, propertyType : UNDEFINED, order : ASCENDING }] where (isdescendantnode([s], [/tests])) and ([s].[test:iteration] = 9) */",
                result2.getPlan());
    }
}
