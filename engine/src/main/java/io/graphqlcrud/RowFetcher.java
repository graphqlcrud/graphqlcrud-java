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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

// This must be thread safe, as it will be called by multiple threads at same time
public class RowFetcher implements DataFetcher<Object> {
    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        SQLContext ctx = environment.getContext();
        Object source = environment.getSource();
        if (source == null) {
            return null;
        }
        ResultSetWrapper rs = null;
        if (source instanceof ResultSetList) {
            rs = (ResultSetWrapper)((ResultSetList) source).get();
        } else {
            rs = (ResultSetWrapper)source;
        }
        Field f = environment.getField();

        SQLDirective sqlDirective = SQLDirective.find(environment.getFieldDefinition().getDirectives());
        // this is link to another table
        if (sqlDirective != null){
            if (sqlDirective.getForeignFields() != null) {
                Map<String, Object> values = new HashMap<>();
                if (ctx.getKeyColumns(f.getName()) != null) {
                    for (String col: ctx.getKeyColumns(f.getName())) {
                        values.put(col, rs.getObject(col));
                    }
                }
                // if there are no rows then return empty array
                GraphQLType type = environment.getFieldDefinition().getType();
                if (type instanceof GraphQLModifiedType) {
                    type = ((GraphQLModifiedType) type).getWrappedType();
                }
                for (String colName : SQLQueryBuilderVisitor.getIdentityColumns((GraphQLObjectType)type)) {
                    if (rs.getObject(colName) == null) {
                        return Collections.emptyList();
                    }
                }
                return new ResultSetList(rs, values, false);
            }
        }
        return rs.getObject(f.getName());
    }
}
