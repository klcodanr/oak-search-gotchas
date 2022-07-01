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
package com.danklco.blog.oaksearch.core.servlets;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(resourceTypes = "test:content", methods = HttpConstants.METHOD_GET, extensions = "json", selectors = "query")
public class QueryServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(QueryServlet.class);

    private static final long serialVersionUID = 1L;

    private static final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Override
    protected void doGet(final SlingHttpServletRequest request,
            final SlingHttpServletResponse response) throws ServletException, IOException {

        String query = null;
        Long limit = null;
        try {
            query = Optional.ofNullable(request.getParameter("query")).orElseThrow();
            limit = getLong(request.getParameter("limit"), 1000L);
        } catch (IllegalArgumentException iae) {
            sendProblem(response, 400, "Invalid value for parameter limit: " + iae.getMessage());
            return;
        } catch (NoSuchElementException nsee) {
            sendProblem(response, 400, "Parameter query required: " + nsee.getMessage());
            return;
        }

        QueryManager queryManager = null;
        try {
            Session session = Optional.ofNullable(request.getResourceResolver().adaptTo(Session.class))
                    .orElseThrow(() -> new ServletException("Unexpected Exception: Failed to get JCR Session!"));
            queryManager = session.getWorkspace().getQueryManager();
        } catch (RepositoryException e) {
            sendProblem(response, 500, "Failed to get Query Manager");
            return;
        }

        Query explainQuery = null;
        Query parsedQuery = null;
        try {
            parsedQuery = queryManager.createQuery(query, Query.JCR_SQL2);
            explainQuery = queryManager.createQuery("explain " + query, Query.JCR_SQL2);
        } catch (RepositoryException e) {
            log.info("Failed to create query for: {}", query, e);
            sendProblem(response, 400, "Invalid query: [" + query + "], Exception: " + e.toString());
            return;
        }
        parsedQuery.setLimit(limit);

        String caughtException = "";
        Long executionDuration = -1L;
        Long iterationDuration = -1L;
        List<String> results = new ArrayList<>();
        String plan = "";
        Instant start = Instant.now();
        try {
            plan = explainQuery.execute().getRows().nextRow().getValue("plan").getString();
            QueryResult result = parsedQuery.execute();
            executionDuration = Duration.between(start, Instant.now()).toMillis();

            start = Instant.now();
            NodeIterator nodes = result.getNodes();
            while (nodes.hasNext()) {
                results.add(nodes.nextNode().getPath());
            }
            iterationDuration = Duration.between(start, Instant.now()).toMillis();
        } catch (RepositoryException | UnsupportedOperationException e) {
            caughtException = e.toString();
        } finally {
            if (!results.isEmpty()) {
                iterationDuration = Duration.between(start, Instant.now()).toMillis();
            }
        }

        String res = objectWriter.writeValueAsString(
                Map.of("query", query, "limit", limit, "plan", plan, "executionDuration", executionDuration,
                        "iterationDuration", iterationDuration, "results", results, "caughtException",
                        caughtException));
        response.setContentType("application/json");
        response.getWriter().write(res);
    }

    private void sendProblem(HttpServletResponse response, int statusCode, String title)
            throws IOException {
        response.setContentType("application/problem+json");
        response.setStatus(statusCode);
        String res = objectWriter.writeValueAsString(
                Map.of("status", statusCode, "title", title));
        response.getWriter().write(res);
    }

    private Long getLong(@Nullable String param, @NotNull Long defaultValue) {
        return Optional.ofNullable(param).map(Long::parseLong).orElse(defaultValue);
    }
}
