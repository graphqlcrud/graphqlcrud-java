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
import graphql.schema.idl.SchemaPrinter;
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
    public void testSimpleQuery() throws Exception {
        String query1 = "{\n" +
                "  customers {\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
                "  }\n" +
                "}";
        String result1 = executeSQL(query1);
        String expected =
                "select\n"
                + "  \"g0\".\"SSN\" \"SSN\",\n"
                + "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected, result1);

        String query2 = "{\n" +
                "  accounts {\n" +
                "    id: ACCOUNT_ID\n" +
                "  }\n" +
                "}";
        String result2 = executeSQL(query2);
        expected = "select \"g0\".\"ACCOUNT_ID\" \"id\"\n" +
                "from PUBLIC.ACCOUNT \"g0\"\n" +
                "order by \"g0\".\"ACCOUNT_ID\"";
        Assertions.assertEquals(expected, result2);
    }

    @Test
    public void testQueryWithID()  throws Exception {
        String query3 = "{\n" +
                "  customer(SSN : \"CST01002\") {\n" +
                "    SSN\n" +
                "  }\n" +
                "}";
        String result3 = executeSQL(query3);
        String expected = "select \"g0\".\"SSN\" \"SSN\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where \"g0\".\"SSN\" = 'CST01002'\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected, result3);
    }

    @Test
    public void testNestedQuery() throws Exception {
        String query4 = "{\n" +
          "  customers {\n" +
          "    SSN\n" +
          "    accounts {\n" +
          "      id: ACCOUNT_ID\n" +
          "    }\n" +
          "  }\n" +
          "}";
        String result4 = executeSQL(query4);
        String expected = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(key 'id' value \"g1\".\"ACCOUNT_ID\"))\n" +
                "    from PUBLIC.ACCOUNT \"g1\"\n" +
                "    where \"g0\".\"SSN\" = \"g1\".\"SSN\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected, result4);
    }

    @Test
    public void testNestedSiblingQuery() throws Exception {
        String query4 = "{\n" +
          "  customers {\n" +
          "    SSN\n" +
          "    addreses {\n" +
          "      CITY,\n" +
          "      STATE,\n" +
          "    },\n" +
          "    accounts {\n" +
          "      id: ACCOUNT_ID\n" +
          "    }\n" +
          "  }\n" +
          "}";
        String result4 = executeSQL(query4);
        String expected = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'CITY' value \"g1\".\"CITY\",\n" +
                "      key 'STATE' value \"g1\".\"STATE\"\n" +
                "    ))\n" +
                "    from PUBLIC.ADDRESS \"g1\"\n" +
                "    where \"g0\".\"SSN\" = \"g1\".\"SSN\"\n" +
                "  ) \"addreses\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(key 'id' value \"g2\".\"ACCOUNT_ID\"))\n" +
                "    from PUBLIC.ACCOUNT \"g2\"\n" +
                "    where \"g0\".\"SSN\" = \"g2\".\"SSN\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected, result4);
    }

    @Test
    public void testDeepNestedQuery() throws Exception {
        String query4 = "{\n" +
          "  customers {\n" +
          "    FIRSTNAME\n" +
          "    accounts {\n" +
          "      id: ACCOUNT_ID\n" +
          "      holdinges {\n" +
          "        id: PRODUCT_ID,\n" +
          "        SHARES_COUNT\n" +
          "      }\n" +
          "    }\n" +
          "  }\n" +
          "}";
        String result4 = executeSQL(query4);
        String expected = "select\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'id' value \"g1\".\"ACCOUNT_ID\",\n" +
                "      key 'holdinges' value (\n" +
                "        select json_arrayagg(json_object(\n" +
                "          key 'id' value \"g2\".\"PRODUCT_ID\",\n" +
                "          key 'SHARES_COUNT' value \"g2\".\"SHARES_COUNT\"\n" +
                "        ))\n" +
                "        from PUBLIC.HOLDINGS \"g2\"\n" +
                "        where \"g1\".\"ACCOUNT_ID\" = \"g2\".\"ACCOUNT_ID\"\n" +
                "      )\n" +
                "    ))\n" +
                "    from PUBLIC.ACCOUNT \"g1\"\n" +
                "    where \"g0\".\"SSN\" = \"g1\".\"SSN\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result4);
    }

    @Test
    public void simpleFilterQuery() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    SSN: {\n" +
                "      ne: \"CST01002\"\n" +
                "    }\n" +
                "  }) {\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where \"g0\".\"SSN\" <> 'CST01002'\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);

        String query1 = "{\n" +
                "  accounts (filter: {\n" +
                "    ACCOUNT_ID: {\n" +
                "      in: [19980001,19980005,19990009]\n" +
                "    },\n" +
                "    and: {\n" +
                "      STATUS: {\n" +
                "        eq: \"Personal\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    ACCOUNT_ID\n" +
                "    SSN\n" +
                "    STATUS\n" +
                "  }\n" +
                "}";
        String result1 = executeSQL(query1);
        String expected1 = "select\n" +
                "  \"g0\".\"ACCOUNT_ID\" \"ACCOUNT_ID\",\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"STATUS\" \"STATUS\"\n" +
                "from PUBLIC.ACCOUNT \"g0\"\n" +
                "where (\n" +
                "  \"g0\".\"ACCOUNT_ID\" in (\n" +
                "    19980001, 19980005, 19990009\n" +
                "  )\n" +
                "  and \"g0\".\"STATUS\" = 'Personal'\n" +
                ")\n" +
                "order by \"g0\".\"ACCOUNT_ID\"";
        Assertions.assertEquals(expected1,result1);

        String query2 = "{\n" +
                "  accounts (filter: {\n" +
                "    ACCOUNT_ID: {\n" +
                "      between: [19980001,19980005]\n" +
                "    }\n" +
                "  }) {\n" +
                "    ACCOUNT_ID\n" +
                "    STATUS\n" +
                "  }\n" +
                "}";
        String result2 = executeSQL(query2);
        String expected2 = "select\n" +
                "  \"g0\".\"ACCOUNT_ID\" \"ACCOUNT_ID\",\n" +
                "  \"g0\".\"STATUS\" \"STATUS\"\n" +
                "from PUBLIC.ACCOUNT \"g0\"\n" +
                "where \"g0\".\"ACCOUNT_ID\" between 19980001 and 19980005\n" +
                "order by \"g0\".\"ACCOUNT_ID\"";
        Assertions.assertEquals(expected2,result2);
    }

    @Test
    public void nestedFilterQuery() throws Exception{
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    LASTNAME: {\n" +
                "      contains: \"J\"\n" +
                "    },\n" +
                "    and: {\n" +
                "      FIRSTNAME: {\n" +
                "        startsWith: \"C\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    SSN\n" +
                "    accounts {\n" +
                "      ACCOUNT_ID\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(key 'ACCOUNT_ID' value \"g1\".\"ACCOUNT_ID\"))\n" +
                "    from PUBLIC.ACCOUNT \"g1\"\n" +
                "    where \"g0\".\"SSN\" = \"g1\".\"SSN\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  \"g0\".\"LASTNAME\" like ('%' || replace(\n" +
                "    replace(\n" +
                "      replace(\n" +
                "        'J',\n" +
                "        '!',\n" +
                "        '!!'\n" +
                "      ),\n" +
                "      '%',\n" +
                "      '!%'\n" +
                "    ),\n" +
                "    '_',\n" +
                "    '!_'\n" +
                "  ) || '%') escape '!'\n" +
                "  and \"g0\".\"FIRSTNAME\" like (replace(\n" +
                "    replace(\n" +
                "      replace(\n" +
                "        'C',\n" +
                "        '!',\n" +
                "        '!!'\n" +
                "      ),\n" +
                "      '%',\n" +
                "      '!%'\n" +
                "    ),\n" +
                "    '_',\n" +
                "    '!_'\n" +
                "  ) || '%') escape '!'\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void simpleAndOrFilterQuery() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    LASTNAME: {\n" +
                "      eq: \"Smith\"\n" +
                "    },\n" +
                "    FIRSTNAME: {\n" +
                "      eq: \"John\"\n" +
                "    },\n" +
                "    or: {\n" +
                "      SSN: {\n" +
                "        eq: \"CST01002\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "    addreses {\n" +
                "      STATE\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected ="select\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(key 'STATE' value \"g1\".\"STATE\"))\n" +
                "    from PUBLIC.ADDRESS \"g1\"\n" +
                "    where \"g0\".\"SSN\" = \"g1\".\"SSN\"\n" +
                "  ) \"addreses\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  (\n" +
                "    \"g0\".\"LASTNAME\" = 'Smith'\n" +
                "    and \"g0\".\"FIRSTNAME\" = 'John'\n" +
                "  )\n" +
                "  or \"g0\".\"SSN\" = 'CST01002'\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void deepNestedFilterQuery() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    or: {\n" +
                "      LASTNAME: {\n" +
                "        eq: \"Smith\"\n" +
                "      },\n" +
                "      FIRSTNAME: {\n" +
                "        eq: \"John\"\n" +
                "      },\n" +
                "      and: {\n" +
                "        SSN: {\n" +
                "          eq: \"CST01002\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    SSN\n" +
                "    LASTNAME\n" +
                "    accounts (filter: {\n" +
                "      and: {\n" +
                "        ACCOUNT_ID: {\n" +
                "          eq: 19980001\n" +
                "        }\n" +
                "      }\n" +
                "    }) {\n" +
                "      ACCOUNT_ID\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(key 'ACCOUNT_ID' value \"g1\".\"ACCOUNT_ID\"))\n" +
                "    from PUBLIC.ACCOUNT \"g1\"\n" +
                "    where (\n" +
                "      \"g0\".\"SSN\" = \"g1\".\"SSN\"\n" +
                "      and \"g1\".\"ACCOUNT_ID\" = 19980001\n" +
                "    )\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  (\n" +
                "    \"g0\".\"LASTNAME\" = 'Smith'\n" +
                "    or \"g0\".\"FIRSTNAME\" = 'John'\n" +
                "  )\n" +
                "  and \"g0\".\"SSN\" = 'CST01002'\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);

    }

    @Test
    public String executeSQL(String query) throws Exception {
        String sql;
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
            sql = ctx.getSQL();
            Assertions.assertNotNull(sql);
        }
        return sql;
    }
}




