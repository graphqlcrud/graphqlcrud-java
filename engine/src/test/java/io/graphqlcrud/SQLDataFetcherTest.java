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

import java.sql.Connection;

import javax.inject.Inject;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.agroal.api.AgroalDataSource;
import io.graphqlcrud.model.Schema;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTestResource(H2DatabaseTestResource.class)
@QuarkusTest

class SQLDataFetcherTest {

    @Inject
    private AgroalDataSource datasource;

    @Test
    public void testDataFetcher() throws Exception {

        try (Connection connection = datasource.getConnection()) {
            Assertions.assertNotNull(connection);
            Schema schema = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            Assertions.assertNotNull(schema);
            GraphQLSchema graphQLSchema = GraphQLSchemaBuilder.getSchema(schema);
            Assertions.assertNotNull(graphQLSchema);

            String query1 = "{\n" +
                    "  customers {\n" +
                    "    SSN\n" +
                    "  }\n" +
                    "}";
            String result1 = executeSQL(query1,graphQLSchema);
            Assertions.assertEquals(result1,"SELECT SSN FROM PUBLIC.CUSTOMER");

            String query2 = "{\n" +
                    "  accounts {\n" +
                    "    ACCOUNT_ID\n" +
                    "  }\n" +
                    "}";
            String result2 = executeSQL(query2,graphQLSchema);
            Assertions.assertEquals(result2,"SELECT ACCOUNT_ID FROM PUBLIC.ACCOUNT");

            String query3 = "{\n" +
                    "  customer(SSN : \"CST01002\") {\n" +
                    "    SSN\n" +
                    "  }\n" +
                    "}";
            String result3 = executeSQL(query3,graphQLSchema);
            Assertions.assertEquals(result3,"SELECT SSN FROM PUBLIC.CUSTOMER WHERE SSN = 'CST01002'");

        }
    }

    @Test
    public String executeSQL(String query, GraphQLSchema graphQLSchema) throws Exception {
        String sql;
        ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
                .query(query);

        try (SQLContext ctx = new SQLContext(this.datasource.getConnection())) {
            executionInput.context(ctx);

            GraphQL graphQL = GraphQL
                    .newGraphQL(graphQLSchema)
                    .build();

            ExecutionResult executionResult = graphQL.execute(executionInput.build());
            Assertions.assertNotNull(executionResult);
            sql = ctx.getSQL();
            Assertions.assertNotNull(sql);
        }
        return sql;
    }
}




