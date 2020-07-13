/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.graphqlcrud.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryParameters {
    String query;
    String operationName;
    Map<String, Object> variables = Collections.emptyMap();

    public String getQuery() {
        return query;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public static QueryParameters from(String request) throws Exception {
        QueryParameters parameters = new QueryParameters();
        Map<String, Object> json = readJSON(request);
        parameters.query = (String) json.get("query");
        parameters.operationName = (String) json.get("operationName");
        parameters.variables = getVariables(json.get("variables"));
        return parameters;
    }


    private static Map<String, Object> getVariables(Object variables) {
        Map<String, Object> vars = new HashMap<>();
        if (variables != null) {
            Map<?, ?> inputVars = (Map) variables;
            inputVars.forEach((k, v) -> vars.put(String.valueOf(k), v));
        }
        return vars;
    }

    private static Map<String, Object> readJSON(String json) throws Exception  {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<Map<String,Object>>(){});
    }
}