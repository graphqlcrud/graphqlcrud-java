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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
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
    private GraphQLSchema graphQLSchema;

    @BeforeEach
    public void setup() throws Exception {
        try (Connection connection = datasource.getConnection()) {
            Assertions.assertNotNull(connection);
            Schema schema = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            Assertions.assertNotNull(schema);
            this.graphQLSchema = GraphQLSchemaBuilder.getSchema(schema);
            Assertions.assertNotNull(this.graphQLSchema);
        }
    }

    @Test
    public void testSimplequery() throws Exception {
        String query1 = "{\n" +
                "  customers {\n" +
                "    SSN\n" +
                "  }\n" +
                "}";
        String result1 = executeSQL(query1);
        Assertions.assertEquals("SELECT g0.SSN FROM PUBLIC.CUSTOMER AS g0 ORDER BY g0.SSN", result1);

        String query2 = "{\n" +
                "  accounts {\n" +
                "    ACCOUNT_ID\n" +
                "  }\n" +
                "}";
        String result2 = executeSQL(query2);
        Assertions.assertEquals("SELECT g0.ACCOUNT_ID FROM PUBLIC.ACCOUNT AS g0 ORDER BY g0.ACCOUNT_ID", result2);
    }

    @Test
    public void testQueryWithID()  throws Exception {
        String query3 = "{\n" +
                "  customer(SSN : \"CST01002\") {\n" +
                "    SSN\n" +
                "  }\n" +
                "}";
        String result3 = executeSQL(query3);
        Assertions.assertEquals("SELECT g0.SSN FROM PUBLIC.CUSTOMER AS g0 WHERE g0.SSN = 'CST01002' ORDER BY g0.SSN", result3);
    }

    @Test
    public void testNestedQuery() throws Exception {
        String query4 = "{\n" +
          "  customers {\n" +
          "    SSN\n" +
          "    accounts {\n" +
          "      ACCOUNT_ID\n" +
          "    }\n" +
          "  }\n" +
          "}";
        String result4 = executeSQL(query4);
        Assertions.assertEquals(
                "SELECT g0.SSN, g1.ACCOUNT_ID FROM PUBLIC.CUSTOMER AS g0 "
                + "LEFT OUTER JOIN PUBLIC.ACCOUNT AS g1 ON g0.SSN = g1.SSN ORDER BY g0.SSN, g1.ACCOUNT_ID",
                result4);
    }

    @Test
    public void testDeepNestedQuery() throws Exception {
        String query4 = "{\n" +
          "  customers {\n" +
          "    FIRSTNAME\n" +
          "    accounts {\n" +
          "      ACCOUNT_ID\n" +
          "      holdinges {\n" +
          "        PRODUCT_ID,\n" +
          "        SHARES_COUNT\n" +
          "      }\n" +
          "    }\n" +
          "  }\n" +
          "}";
        String result4 = executeSQL(query4);
        Assertions.assertEquals(
                "SELECT g0.SSN, g0.FIRSTNAME, g1.ACCOUNT_ID, g2.TRANSACTION_ID, g2.PRODUCT_ID, g2.SHARES_COUNT "
                + "FROM PUBLIC.CUSTOMER AS g0 "
                + "LEFT OUTER JOIN PUBLIC.ACCOUNT AS g1 ON g0.SSN = g1.SSN "
                + "LEFT OUTER JOIN PUBLIC.HOLDINGS AS g2 ON g1.ACCOUNT_ID = g2.ACCOUNT_ID "
                + "ORDER BY g0.SSN, g1.ACCOUNT_ID, g2.TRANSACTION_ID",
                result4);
    }

    @Test
    public String executeSQL(String query) throws Exception {
        String sql;
        ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
                .query(query);

        try (SQLContext ctx = new SQLContext(this.datasource.getConnection())) {
            executionInput.context(ctx);

            GraphQL graphQL = GraphQL
                    .newGraphQL(this.graphQLSchema)
                    .build();

            ExecutionResult executionResult = graphQL.execute(executionInput.build());
            Assertions.assertNotNull(executionResult);
            sql = ctx.getSQL();
            Assertions.assertNotNull(sql);
        }
        return sql;
    }
}




