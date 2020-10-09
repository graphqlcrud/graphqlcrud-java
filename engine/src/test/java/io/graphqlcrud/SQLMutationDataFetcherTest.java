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
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.agroal.api.AgroalDataSource;
import io.graphqlcrud.model.Schema;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTestResource(H2DatabaseTestResource.class)
@QuarkusTest
class SQLMutationDataFetcherTest {

    @Inject
    AgroalDataSource datasource;
    GraphQLSchema graphQLSchema;

    @BeforeEach
    public void setup() throws Exception {
        try (Connection connection = this.datasource.getConnection()) {
            Assertions.assertNotNull(connection);
            Schema schema = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            Assertions.assertNotNull(schema);
            this.graphQLSchema = GraphQLSchemaBuilder.getSchema(schema);
            Assertions.assertNotNull(this.graphQLSchema);

            SchemaPrinter sp = new SchemaPrinter();
            System.out.println(sp.print(this.graphQLSchema));
        }
    }

    @Test
    public void CreateMutation() throws Exception {
        String create_query = "mutation {\n" +
                "  createCustomer (input: {\n" +
                "    SSN: \"CST01040\",\n" +
                "    FIRSTNAME: \"Gorge\",\n" +
                "    LASTNAME: \"Corners\",\n" +
                "    PHONE: \"(651)590-9023\"\n" +
                "  }) {\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "    PHONE\n" +
                "  }\n" +
                "}";
        List<String> create_result = executeSQL(create_query);

        String create_expected_mutation = "insert into CUSTOMER (\n" +
                "  SSN,\n" +
                "  FIRSTNAME,\n" +
                "  LASTNAME,\n" +
                "  PHONE\n" +
                ")\n" +
                "values (\n" +
                "  'CST01040', \n" +
                "  'Gorge', \n" +
                "  'Corners', \n" +
                "  '(651)590-9023'\n" +
                ")";

        String create_expected_sql = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"PHONE\" \"PHONE\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where \"g0\".\"SSN\" = 'CST01040'\n" +
                "order by \"g0\".\"SSN\"";

        Assertions.assertEquals(create_expected_mutation,create_result.get(1));
        Assertions.assertEquals(create_expected_sql, create_result.get(0));
    }

    @Test
    public void UpdateMutation() throws Exception {
        String update_query = "mutation {\n" +
                "  updateCustomer(input: {\n" +
                "    FIRSTNAME: \"James\"\n" +
                "  }, filter: {\n" +
                "    SSN: {\n" +
                "      eq: \"CST01002\"\n" +
                "    }\n" +
                "  }) {\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "    PHONE\n" +
                "  }\n" +
                "}";

        List<String> update_result = executeSQL(update_query);

        String update_expected_mutation = "update CUSTOMER\n" +
                "set\n" +
                "  FIRSTNAME = 'James'\n" +
                "where SSN = 'CST01002'";

        String update_expected_sql = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"PHONE\" \"PHONE\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where \"g0\".\"SSN\" = 'CST01002'\n" +
                "order by \"g0\".\"SSN\"";

        Assertions.assertEquals(update_expected_mutation,update_result.get(1));
        Assertions.assertEquals(update_expected_sql, update_result.get(0));
    }

    @Test
    public void DeleteMutation() throws Exception {
        String delete_query = "mutation {\n" +
                "  deleteCustomer (filter: {\n" +
                "    SSN: {\n" +
                "      eq: \"CST01002\"\n" +
                "    }\n" +
                "  }) {\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "    PHONE\n" +
                "  }\n" +
                "}";
        List<String> delete_result = executeSQL(delete_query);

        String delete_expected_mutation = "delete from CUSTOMER\n" +
                "where SSN = 'CST01002'";

        String delete_expected_sql = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"PHONE\" \"PHONE\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where \"g0\".\"SSN\" = 'CST01002'\n" +
                "order by \"g0\".\"SSN\"";

        Assertions.assertEquals(delete_expected_mutation,delete_result.get(1));
        Assertions.assertEquals(delete_expected_sql, delete_result.get(0));
    }

    @Test
    public List<String> executeSQL(String query) throws Exception {
        List<String> sql = new ArrayList<>();
        ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
                .query(query);

        try (SQLContext ctx = new SQLContext(this.datasource.getConnection())) {
            executionInput.context(ctx);
            ctx.setDialect("DEFAULT");

            GraphQL graphQL = GraphQL
                    .newGraphQL(this.graphQLSchema)
                    .build();

            ExecutionResult executionResult = graphQL.execute(executionInput.build());
            Assertions.assertNotNull(executionResult);

            sql.add(ctx.getSQL());
            sql.add(ctx.getSqlMutation());
            Assertions.assertNotNull(sql);
        }
        return sql;
    }
}