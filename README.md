# Oak Search Gotchas

This project demonstrates some gotchas when using Oak Search & Indexing.

## How to build

First you must install the codebase into your local AEM instance. 

    mvn clean install sling:install

## Running the Tests

There are five tests which demonstrate various challenges in defining Oak indexes. 

Follow the instructions below to run the tests. Each test is expected to fail until you make the changes described in the test file.

### Test 1 - Query by Node Type

- Run Command: `mvn clean verify -Prun-it,test-01`
- Test: [src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_01_QueryNodeType.java](src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_01_QueryNodeType.java)
- Index Definition: [src/test/resources/01_QueryNodeType/indexDef.json](src/test/resources/01_QueryNodeType/indexDef.json)


### Test 2 - Query by Properties

- Run Command: `mvn clean verify -Prun-it,test-02`
- Test: [src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_02_QueryProperties.java](src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_02_QueryProperties.java)
- Index Definition: [src/test/resources/02_QueryProperties/indexDef.json](src/test/resources/02_QueryProperties/indexDef.json)


### Test 3 - Ordering

- Run Command: `mvn clean verify -Prun-it,test-03`
- Test: [src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_03_Ordering.java](src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_03_Ordering.java)
- Index Definition: [src/test/resources/03_Ordering/indexDef.json](src/test/resources/03_Ordering/indexDef.json)


### Test 4 - Nulls

- Run Command: `mvn clean verify -Prun-it,test-04`
- Test: [src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_04_Nulls.java](src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_04_Nulls.java)
- Index Definition: [src/test/resources/04_NullProperties/indexDef.json](src/test/resources/04_NullProperties/indexDef.json)


### Test 5 - Permissions

- Run Command: `mvn clean verify -Prun-it,test-05`
- Test: [src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_05_Permissions.java](src/test/java/com/danklco/blog/oaksearch/it/tests/OakSearchTest_05_Permissions.java)

