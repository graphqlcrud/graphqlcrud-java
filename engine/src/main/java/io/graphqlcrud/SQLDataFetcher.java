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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;

// This must be thread safe, as it will be called by multiple threads at same time
// This is very simple naively written class, will require more structure here
public class SQLDataFetcher implements DataFetcher<ResultSetList>{
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLDataFetcher.class);

    @Override
    public ResultSetList get(DataFetchingEnvironment environment) throws Exception {
        SQLContext ctx = environment.getContext();

        String sql = buildSQL(environment);
        ctx.setSQL(sql);
        LOGGER.info("SQL Executed:" + sql);

        ResultSet rs = null;
        Connection c = ctx.getConnection();
        CallableStatement stmt = c.prepareCall(sql);
        boolean hasResults = stmt.execute();
        if (hasResults) {
            rs = stmt.getResultSet();
        }
        ctx.setResultSet(rs);
        return new ResultSetList(rs);
    }

    private GraphQLDirective sqlDirective(List<GraphQLDirective> directives) {
        if (directives == null) {
            return null;
        }
        for (GraphQLDirective d : directives) {
            if (d.getName().equals("sql")  || d.getName().equals("relation") || d.getName().equals("type")) {
                return d;
            }
        }
        return null;
    }

    private String buildSQL(DataFetchingEnvironment environment) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        GraphQLDirective parentDirective = sqlDirective(environment.getFieldDefinition().getDirectives());
        GraphQLArgument parentArg = parentDirective.getArgument("from");
        GraphQLDirective joinDir = null;

        List<SelectedField> fields = environment.getSelectionSet().getFields();
        for (int i = 0; i < fields.size(); i++) {
            SelectedField field = fields.get(i);
            GraphQLFieldDefinition definition = field.getFieldDefinition();

            GraphQLDirective fieldDirective = sqlDirective(definition.getDirectives());

            GraphQLArgument relationArg = fieldDirective.getArgument("kind");
            GraphQLArgument fieldArg = fieldDirective.getArgument("tablename");
            if (relationArg == null) {
                if (parentArg.getValue().toString().equals(fieldArg.getValue().toString())) {
                    sb.append(parentArg.getValue().toString());
                } else {
                    sb.append(fieldArg.getValue().toString());
                }

                GraphQLType type = definition.getType();
                if (type instanceof GraphQLModifiedType) {
                    type = ((GraphQLModifiedType) type).getWrappedType();
                }
                if (GraphQLTypeUtil.isScalar(type)) {
                    sb.append("." + field.getName());
                    if (i < fields.size() - 1) {
                        sb.append(",");
                    }
                }
            }
            else {
                joinDir = fieldDirective;
            }
        }

        sb.append(" FROM ");
        if (joinDir == null) {
//            sb.append(environment.getMergedField().getName());
            sb.append(parentArg.getValue().toString());
        } else  {
            GraphQLArgument joinArg = joinDir.getArgument("tablename");

                if (joinArg.getValue().toString().equals(parentArg.getValue().toString())){
                    sb.append(parentArg.getValue().toString());
                 }
                else {
                    sb.append(parentArg.getValue().toString() + " INNER JOIN " + joinArg.getValue().toString());
                    sb.append(" ON ");
                    GraphQLArgument primaryArg = joinDir.getArgument("primaryField");
                    GraphQLArgument foreignArg = joinDir.getArgument("foreignField");
                    sb.append(parentArg.getValue().toString() + "." + foreignArg.getValue().toString() + " = " + joinArg.getValue().toString() + "." + primaryArg.getValue().toString());
                }
            }
        Field f = environment.getField();
        List<Argument> args = f.getArguments();
        if (args != null && !args.isEmpty()) {
            sb.append(" WHERE ");
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    sb.append(" AND ");
                }
                Argument arg = args.get(i);
                // TODO: Need to handle all other types of values
                // TODO: May be this can be place holders like "?" then values in prepared statement
                Value v = arg.getValue();
                if (v instanceof StringValue) {
                    sb.append(arg.getName()).append(" = ").append("'").append(((StringValue) v).getValue()).append("'");
                } else if (v instanceof BooleanValue) {
                    sb.append(arg.getName()).append(" = ").append("'").append(((BooleanValue)v).isValue()).append("'");
                } else if (v instanceof IntValue) {
                    sb.append(arg.getName()).append(" = ").append(((IntValue)v).getValue());
                }
            }
        }
        return sb.toString();
    }
}
