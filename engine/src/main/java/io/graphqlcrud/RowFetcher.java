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
package io.graphqlcrud;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

// This must be thread safe, as it will be called by multiple threads at same time
public class RowFetcher implements DataFetcher<Object> {
    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        Object source = environment.getSource();
        if (source == null) {
            return null;
        }
        if (source instanceof ResultSetList) {
            source = ((ResultSetList) source).get();
        }
        Field f = environment.getField();

        SQLDirective sqlDirective = SQLDirective.find(environment.getFieldDefinition().getDirectives());
        // this is link to another table
        if (sqlDirective != null){
            if (sqlDirective.getForeignFields() != null) {
                List<ResultSet> result = new ArrayList<>();
                result.add((ResultSet)source);
                return result;
            }
        }
        return ((ResultSet)source).getObject(f.getName());
    }
}
