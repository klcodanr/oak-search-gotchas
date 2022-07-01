package com.danklco.blog.oaksearch.it.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.sling.testing.clients.ClientException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This test demonstrates that even if you index all the properties, that you
 * still need to consider ordering
 */
class OakSearchTest_03_Ordering extends OakSearchITBase {

    @BeforeAll
    static void beforeAll() throws Exception {
        OakSearchITBase.setup();
    }

    // In OakSearchTest_01_QueryNodeType, we ran this query and it worked
    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void canQueryUnordered(int iteration) throws ClientException, IOException {
        TestQueryResult result1 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=" + iteration,
                100);
        assertEquals(100, result1.getResults().size());
    }

    // But what happens if we wanted to order the nodes by the test:child property?

    // The query below will fail becase while the test:.* properties are indexed,
    // they are not ordered

    // To fix this, add the following line to the beforeAll method:
    // updateIndex("03_Ordering/indexDef.json");

    // This will load a new index definition which will add ordering to all of the
    // properties
    @ParameterizedTest
    @MethodSource(value = "generateScenarios")
    void canRunOrdered(String order, int iteration) throws ClientException, IOException {

        TestQueryResult result2 = super.runQuery(adminAuthor,
                "SELECT * FROM [test:content] AS s WHERE ISDESCENDANTNODE([/tests]) AND [test:iteration]=" + iteration
                        + " ORDER BY [" + order + "]",
                100);
        assertEquals(100, result2.getResults().size());

        // You can verify this on the AEM Query Performance Tool:
        // http://localhost:4502/libs/granite/operations/content/diagnosistools/queryPerformance.html
        // The Query plan tells us how the query is executed, specifically:
        // - It's using the index:
        // lucene:testContentLucene(/oak:index/testContentLucene)
        // - Filtering from the index by path: +:ancestors:/tests
        // - Ordering by the test:child property: ordering:[{ propertyName : test:child,
        // propertyType : UNDEFINED, order : ASCENDING }]
        // - Querying against the indexed properties: +test:iteration:[{num} TO {num}]
        assertEquals(
                "[test:content] as [s] /* lucene:testContentLucene(/oak:index/testContentLucene) +:ancestors:/tests +test:iteration:["
                        + iteration + " TO " + iteration
                        + "] ordering:[{ propertyName : " + order
                        + ", propertyType : UNDEFINED, order : ASCENDING }] where (isdescendantnode([s], [/tests])) and ([s].[test:iteration] = "
                        + iteration + ") */",
                result2.getPlan());
    }

    static Stream<Arguments> generateScenarios() {
        List<String> properties = List.of("test:child", "test:item", "test:name");
        List<Arguments> scenarios = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            for (String property : properties) {
                scenarios.add(Arguments.of(property, i));
            }
        }
        return scenarios.stream();
    }

}
