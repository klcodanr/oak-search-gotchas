# Oak Search Gotchas

This project demonstrates some gotchas when using Oak Search & Indexing.

## How to build

First you must install the codebase into your local AEM instance. 

    mvn clean install sling:install

## Running the Tests

There are five tests which demonstrate various challenges in defining Oak indexes. 

Follow the instructions below to run the tests fron the `it.tests` module. Each test is expected to fail until you make the changes described in the test file.

### Test 1 - Query by Node Type

- Run Command: `mvn clean verify -Prun-it,test-01`
- Test: [it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_01_QueryNodeType.java](it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_01_QueryNodeType.java)
- Index [it.tests/src/main/resources/01_QueryNodeType/indexDef.json](it.tests/src/main/resources/01_QueryNodeType/indexDef.json)


### Test 2 - Query by Properties

- Run Command: `mvn clean verify -Prun-it,test-02`
- Test: [it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_02_QueryProperties.java](it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_02_QueryProperties.java)
- Index [it.tests/src/main/resources/02_QueryProperties/indexDef.json](it.tests/src/main/resources/02_QueryProperties/indexDef.json)


### Test 3 - Ordering

- Run Command: `mvn clean verify -Prun-it,test-03`
- Test: [it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_03_Ordering.java](it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_03_Ordering.java)
- Index [it.tests/src/main/resources/03_Ordering/indexDef.json](it.tests/src/main/resources/03_Ordering/indexDef.json)


### Test 4 - Nulls

- Run Command: `mvn clean verify -Prun-it,test-04`
- Test: [it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_04_Nulls.java](it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_04_Nulls.java)
- Index [it.tests/src/main/resources/04_NullProperties/indexDef.json](it.tests/src/main/resources/04_NullProperties/indexDef.json)


### Test 5 - Permissions

- Run Command: `mvn clean verify -Prun-it,test-05`
- Test: [it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_05_Permissions.java](it.tests/src/main/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_05_Permissions.java)

