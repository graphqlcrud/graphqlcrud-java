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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

// This must be thread safe, as it will be called by multiple threads at same time
public class RowFetcher implements DataFetcher<Object> {
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        Object source = environment.getSource();
        if (source == null) {
            return null;
        }
        ResultSetWrapper rs = null;
        if (source instanceof ResultSetList) {
            rs = (ResultSetWrapper)((ResultSetList) source).get();
        } else if (source instanceof ResultSetWrapper){
            rs = (ResultSetWrapper)source;
        }
        Field f = environment.getField();
        String colName = SQLQueryBuilderVisitor.fieldName(f);

        SQLDirective sqlDirective = SQLDirective.find(environment.getFieldDefinition().getDirectives());
        // this is link to another table
        if (sqlDirective != null && rs != null){
            GraphQLFieldDefinition definition = environment.getFieldDefinition();
            GraphQLType type = definition.getType();
            List<?> node = this.mapper.readValue(rs.getBytes(f.getName()), List.class);
            if (type instanceof GraphQLObjectType) {
                return node.get(0);
            }
            return node;
        }
        if (rs != null) {
            return rs.getObject(f.getName());
        } else {
            return ((Map<?,?>)source).get(colName);
        }
    }
}
