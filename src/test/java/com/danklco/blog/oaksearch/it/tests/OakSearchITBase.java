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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base test for the IT's
 */
public abstract class OakSearchITBase {

    private static final String INDEX_PATH = "/oak:index/testContentLucene";

    private static final Logger log = LoggerFactory.getLogger(OakSearchITBase.class);

    protected static SlingClient adminAuthor;
    protected static SlingClient limitedAccess;

    protected static void setup() throws ClientException, InterruptedException, UnsupportedEncodingException {
        adminAuthor = new SlingClient(URI.create("http://localhost:4502"), "admin", "admin");
        limitedAccess = new SlingClient(URI.create("http://localhost:4502"), "test-limited-access-user",
                "test-limited-access-user");

        if (!adminAuthor.exists(
                "/apps/system/config/org.apache.sling.jcr.repoinit.RepositoryInitializer~oak-search.cfg.json")) {
            log.info("Creating RepoInit Configuration...");
            adminAuthor.upload(new File("src/test/resources/repoinit.json"), "application/json",
                    "/apps/system/config/org.apache.sling.jcr.repoinit.RepositoryInitializer~oak-search.cfg.json", true,
                    200, 201);
            TimeUnit.SECONDS.sleep(10);
        }

        if (!adminAuthor.exists("/tests/it-9")) {
            log.info("Creating test content, this can take a few minutes...");
            adminAuthor.doPost("/bin/oak-search/ensurecontent", new StringEntity(""), List.of(), 200);
            log.info("Test content created!");
        } else {
            log.info("Test content already exists!");
        }
    }

    protected TestQueryResult runQuery(SlingClient client, String query, long limit)
            throws ClientException, IOException {
        SlingHttpResponse response = client.doGet("/tests/it-9.query.json",
                List.of(new BasicNameValuePair("query", query), new BasicNameValuePair("limit", String.valueOf(limit))),
                Collections.emptyList(), 200);
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Retrieved result: \n{}", response.getContent());
        return objectMapper.readValue(response.getContent(), TestQueryResult.class);
    }

    /**
     * Upserts an index into Oak and forces it to reindex.
     * 
     * @param definitionFile the definition file in the classpath to upsert
     * @throws ClientException      an exception occurs communicating to AEM
     * @throws IOException          an exception occurs reading the file
     * @throws InterruptedException the process is interrupted
     */
    protected static void updateIndex(String definitionFile) throws ClientException, IOException, InterruptedException {

        String definition = IOUtils.toString(OakSearchITBase.class.getClassLoader().getResourceAsStream(definitionFile),
                StandardCharsets.UTF_8);

        if (adminAuthor.exists(INDEX_PATH)) {
            log.info("Removing old index at: {}", INDEX_PATH);
            adminAuthor.deletePath(INDEX_PATH, 201, 200);
        }

        log.info("Setting index definition {} to: \n{}", INDEX_PATH, definition);
        HttpEntity entity = FormEntityBuilder.create()
                .addParameter(":operation", "import")
                .addParameter(":contentType", "json")
                .addParameter(":name", "testContentLucene")
                .addParameter(":content", definition)
                .build();
        adminAuthor.doPost("/oak:index", entity, HttpUtils.getExpectedStatus(200, 201));

        log.info("Index updated, waiting for reindexing to complete...");
        setReindex(false);
        setReindex(true);

        boolean reindex = true;
        for (int i = 0; i < 86400 && reindex; i++) {
            if (i > 0 && i % 60 == 0) {
                log.info("Still waiting for reindexing to complete after {} seconds...", i);
            }
            TimeUnit.SECONDS.sleep(1);
            JsonNode index = adminAuthor.doGetJson(INDEX_PATH, 1, 200);
            reindex = index.get("reindex").asBoolean();
        }
        log.info("Reindexing complete!");
    }

    private static void setReindex(boolean reindex) throws UnsupportedEncodingException, ClientException {
        adminAuthor
                .doPost(INDEX_PATH,
                        new UrlEncodedFormEntity(List.of(new BasicNameValuePair("reindex", String.valueOf(reindex)),
                                new BasicNameValuePair("reindex@TypeHint", "Boolean"))),
                        Collections.emptyList(), 200, 201);
    }
}
