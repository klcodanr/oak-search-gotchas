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

import java.util.List;

public class TestQueryResult {

    private String query;
    private long limit;
    private String plan;
    private long executionDuration;
    private long iterationDuration;
    private List<String> results;
    private String caughtException;

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the limit
     */
    public long getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(long limit) {
        this.limit = limit;
    }

    /**
     * @return the plan
     */
    public String getPlan() {
        return plan;
    }

    /**
     * @param plan the plan to set
     */
    public void setPlan(String plan) {
        this.plan = plan;
    }

    /**
     * @return the executionDuration
     */
    public long getExecutionDuration() {
        return executionDuration;
    }

    /**
     * @param executionDuration the executionDuration to set
     */
    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    /**
     * @return the iterationDuration
     */
    public long getIterationDuration() {
        return iterationDuration;
    }

    /**
     * @param iterationDuration the iterationDuration to set
     */
    public void setIterationDuration(long iterationDuration) {
        this.iterationDuration = iterationDuration;
    }

    /**
     * @return the results
     */
    public List<String> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(List<String> results) {
        this.results = results;
    }

    /**
     * @return the caughtException
     */
    public String getCaughtException() {
        return caughtException;
    }

    /**
     * @param caughtException the caughtException to set
     */
    public void setCaughtException(String caughtException) {
        this.caughtException = caughtException;
    }

}
