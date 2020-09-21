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
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g1\"\n" +
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
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ADDRESS\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "    ) \"g1\"\n" +
                "  ) \"addreses\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(key 'id' value \"g2\".\"ACCOUNT_ID\"))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g2\"\n" +
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
                "        from (\n" +
                "          select *\n" +
                "          from PUBLIC.HOLDINGS\n" +
                "          where \"g1\".\"ACCOUNT_ID\" = \"ACCOUNT_ID\"\n" +
                "          order by \"TRANSACTION_ID\"\n" +
                "        ) \"g2\"\n" +
                "      )\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g1\"\n" +
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
    }

    @Test
    public void simpleFilterQuery1() throws Exception {
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
    }

    @Test
    public void simpleFilterQuery3() throws Exception {
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
    public void simpleFilterQuery4() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "   FIRSTNAME: {\n" +
                "    matchesPattern: \"James\"\n" +
                "  }, \n" +
                "    not: {\n" +
                "      LASTNAME:{\n" +
                "        matchesPattern: \"Corby\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    LASTNAME\n" +
                "    FIRSTNAME\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  (\"g0\".\"FIRSTNAME\" like_regex 'James')\n" +
                "  and not ((\"g0\".\"LASTNAME\" like_regex 'Corby'))\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
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
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g1\"\n" +
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
    public void AndOrFilterQuery() throws Exception {
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
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ADDRESS\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "    ) \"g1\"\n" +
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
   public void AndOrFilterQuery2() throws Exception {
        String query1 = "{\n" +
                "  customers (filter: {\n" +
                "    or: {\n" +
                "      LASTNAME: {\n" +
                "        eq: \"Doe\"\n" +
                "      },\n" +
                "      FIRSTNAME: {\n" +
                "        eq: \"John\"\n" +
                "      }\n" +
                "    },\n" +
                "    and: {\n" +
                "      SSN: {\n" +
                "        eq: \"CST01002\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    LASTNAME\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
                "  }\n" +
                "}";
        String result1 = executeSQL(query1);
        String expected1 = "select\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  \"g0\".\"SSN\" = 'CST01002'\n" +
                "  or (\n" +
                "    \"g0\".\"LASTNAME\" = 'Doe'\n" +
                "    and \"g0\".\"FIRSTNAME\" = 'John'\n" +
                "  )\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected1,result1);
  }

  @Test
  public void testAndOrFilterQuery4() throws Exception {
      String query1 = "{\n" +
              "  customers (filter: {\n" +
              "    and: {\n" +
              "      SSN: {\n" +
              "        eq: \"CST01002\"\n" +
              "      }\n" +
              "    },\n" +
              "    or: {\n" +
              "      LASTNAME: {\n" +
              "        eq: \"Doe\"\n" +
              "      },\n" +
              "      FIRSTNAME: {\n" +
              "        eq: \"John\"\n" +
              "      }\n" +
              "    }\n" +
              "  }) {\n" +
              "    LASTNAME\n" +
              "    SSN\n" +
              "    FIRSTNAME\n" +
              "  }\n" +
              "}";
      String result1 = executeSQL(query1);
      String expected1 = "select\n" +
              "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
              "  \"g0\".\"SSN\" \"SSN\",\n" +
              "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\"\n" +
              "from PUBLIC.CUSTOMER \"g0\"\n" +
              "where (\n" +
              "  \"g0\".\"SSN\" = 'CST01002'\n" +
              "  or (\n" +
              "    \"g0\".\"LASTNAME\" = 'Doe'\n" +
              "    and \"g0\".\"FIRSTNAME\" = 'John'\n" +
              "  )\n" +
              ")\n" +
              "order by \"g0\".\"SSN\"";
      Assertions.assertEquals(expected1,result1);
  }

  @Test
  public void AndOrFilterQuery3() throws Exception {
        String query2 = "{\n" +
                "    customers (filter: {\n" +
                "    or: {\n" +
                "      LASTNAME: {\n" +
                "        eq: \"Doe\"\n" +
                "      },\n" +
                "      FIRSTNAME: {\n" +
                "        eq: \"John\"\n" +
                "      }\n" +
                "    },\n" +
                "    SSN: {\n" +
                "        eq: \"CST01002\"\n" +
                "      }\n" +
                "  }) {\n" +
                "    LASTNAME\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
                "  }\n" +
                "}";
        String result2 = executeSQL(query2);
        // since we parse the naked values first those conditions always show up first
        String expected2 = "select\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  \"g0\".\"SSN\" = 'CST01002'\n" +
                "  or (\n" +
                "    \"g0\".\"LASTNAME\" = 'Doe'\n" +
                "    and \"g0\".\"FIRSTNAME\" = 'John'\n" +
                "  )\n" +

                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected2,result2);
    }

    @Test
    public void MultipleAndOrFilterQuery() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    and: {\n" +
                "    LASTNAME: {\n" +
                "      ne: \"Smith\"\n" +
                "    },\n" +
                "    SSN: {\n" +
                "      eq: \"CST01002\"\n" +
                "    }\n" +
                "    }\n" +
                "    or: {\n" +
                "      LASTNAME: {\n" +
                "        ne: \"Aire\"\n" +
                "      },\n" +
                "      FIRSTNAME: {\n" +
                "        ne: \"Jane\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "    SSN\n" +
                "    accounts  {\n" +
                "      ACCOUNT_ID\n" +
                "      STATUS\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'ACCOUNT_ID' value \"g1\".\"ACCOUNT_ID\",\n" +
                "      key 'STATUS' value \"g1\".\"STATUS\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g1\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  (\n" +
                "    \"g0\".\"LASTNAME\" <> 'Smith'\n" +
                "    and \"g0\".\"SSN\" = 'CST01002'\n" +
                "  )\n" +
                "  or (\n" +
                "    \"g0\".\"LASTNAME\" <> 'Aire'\n" +
                "    and \"g0\".\"FIRSTNAME\" <> 'Jane'\n" +
                "  )\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void MultipleAndOrFilterQuery2() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    not: {\n" +
                "      LASTNAME: {\n" +
                "        eq: \"Aire\"\n" +
                "      }\n" +
                "    },\n" +
                "    SSN: {\n" +
                "      eq:\"CST01002\"\n" +
                "    },\n" +
                "    or: {\n" +
                "      LASTNAME: {\n" +
                "        eq: \"Smith\"\n" +
                "      },\n" +
                "      FIRSTNAME: {\n" +
                "        eq: \"Herbert\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    LASTNAME\n" +
                "    FIRSTNAME\n" +
                "    SSN\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"SSN\" \"SSN\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  (\n" +
                "    \"g0\".\"SSN\" = 'CST01002'\n" +
                "    and not (\"g0\".\"LASTNAME\" = 'Aire')\n" +
                "  )\n" +
                "  or (\n" +
                "    \"g0\".\"LASTNAME\" = 'Smith'\n" +
                "    and \"g0\".\"FIRSTNAME\" = 'Herbert'\n" +
                "  )\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void MultipleAndOrFilterQuery3() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    LASTNAME: {\n" +
                "       eq: \"Aire\"\n" +
                "    }\n" +
                "    SSN: {\n" +
                "      eq:\"CST01002\"\n" +
                "    },\n" +
                "    or: {\n" +
                "      LASTNAME: {\n" +
                "        eq: \"Smith\"\n" +
                "      },\n" +
                "      FIRSTNAME: {\n" +
                "        eq: \"Herbert\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    LASTNAME\n" +
                "    FIRSTNAME\n" +
                "    SSN\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"SSN\" \"SSN\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  (\n" +
                "    \"g0\".\"LASTNAME\" = 'Aire'\n" +
                "    and \"g0\".\"SSN\" = 'CST01002'\n" +
                "  )\n" +
                "  or (\n" +
                "    \"g0\".\"LASTNAME\" = 'Smith'\n" +
                "    and \"g0\".\"FIRSTNAME\" = 'Herbert'\n" +
                "  )\n" +
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
                "        eq: \"Doe\"\n" +
                "      },\n" +
                "      FIRSTNAME: {\n" +
                "        eq: \"John\"\n" +
                "      }\n" +
                "    },\n" +
                "    and: {\n" +
                "      SSN: {\n" +
                "        eq: \"CST01002\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    LASTNAME\n" +
                "    SSN\n" +
                "    FIRSTNAME\n" +
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
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(key 'ACCOUNT_ID' value \"g1\".\"ACCOUNT_ID\"))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where (\n" +
                "        \"g0\".\"SSN\" = \"SSN\"\n" +
                "        and \"g1\".\"ACCOUNT_ID\" = 19980001\n" +
                "      )\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g1\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  \"g0\".\"SSN\" = 'CST01002'\n" +
                "  or (\n" +
                "    \"g0\".\"LASTNAME\" = 'Doe'\n" +
                "    and \"g0\".\"FIRSTNAME\" = 'John'\n" +
                "  )\n" +

                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void deepNestedFilterQuery2() throws Exception {
        String query = "{\n" +
                "  accounts (filter: {\n" +
                "    ACCOUNT_ID: {\n" +
                "      eq: 19980002\n" +
                "    },\n" +
                "    and: {\n" +
                "      STATUS: {\n" +
                "        eq: \"Personal\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    ACCOUNT_ID\n" +
                "    STATUS\n" +
                "    TYPE\n" +
                "    SSN\n" +
                "    holdinges (filter: {\n" +
                "      or: {\n" +
                "        TRANSACTION_ID: {\n" +
                "          eq: 2\n" +
                "        },\n" +
                "        SHARES_COUNT: {\n" +
                "          eq: 25\n" +
                "        }\n" +
                "      }\n" +
                "    }) {\n" +
                "      TRANSACTION_ID\n" +
                "      SHARES_COUNT\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"ACCOUNT_ID\" \"ACCOUNT_ID\",\n" +
                "  \"g0\".\"STATUS\" \"STATUS\",\n" +
                "  \"g0\".\"TYPE\" \"TYPE\",\n" +
                "  \"g0\".\"SSN\" \"SSN\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'TRANSACTION_ID' value \"g1\".\"TRANSACTION_ID\",\n" +
                "      key 'SHARES_COUNT' value \"g1\".\"SHARES_COUNT\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.HOLDINGS\n" +
                "      where (\n" +
                "        \"g0\".\"ACCOUNT_ID\" = \"ACCOUNT_ID\"\n" +
                "        and \"g1\".\"TRANSACTION_ID\" = 2\n" +
                "        and \"g1\".\"SHARES_COUNT\" = 25\n" +
                "      )\n" +
                "      order by \"TRANSACTION_ID\"\n" +
                "    ) \"g1\"\n" +
                "  ) \"holdinges\"\n" +
                "from PUBLIC.ACCOUNT \"g0\"\n" +
                "where (\n" +
                "  \"g0\".\"ACCOUNT_ID\" = 19980002\n" +
                "  and \"g0\".\"STATUS\" = 'Personal'\n" +
                ")\n" +
                "order by \"g0\".\"ACCOUNT_ID\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void deepNestedFilterQuery3() throws Exception {
        String query = "{\n" +
                "  customers {\n" +
                "    addreses (filter: {\n" +
                "      or: {\n" +
                "        STATE : {\n" +
                "          eq: \"Ohio\"\n" +
                "        },\n" +
                "        ZIPCODE: {\n" +
                "          eq: \"45232\"\n" +
                "        }\n" +
                "      },\n" +
                "     and: {\n" +
                "       STATE: {\n" +
                "        eq: \"New York\"\n" +
                "      }\n" +
                "    }\n" +
                "    })  {\n" +
                "      STATE\n" +
                "      ST_ADDRESS\n" +
                "      ZIPCODE\n" +
                "    }\n" +
                "    accounts (filter: {\n" +
                "      TYPE: {\n" +
                "        eq: \"Active\"\n" +
                "      }\n" +
                "    }) {\n" +
                "      ACCOUNT_ID\n" +
                "      TYPE\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'STATE' value \"g1\".\"STATE\",\n" +
                "      key 'ST_ADDRESS' value \"g1\".\"ST_ADDRESS\",\n" +
                "      key 'ZIPCODE' value \"g1\".\"ZIPCODE\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ADDRESS\n" +
                "      where (\n" +
                "        \"g0\".\"SSN\" = \"SSN\"\n" +
                "        and (\n" +
                "          \"g1\".\"STATE\" = 'New York'\n" +
                "          or (\n" +
                "            \"g1\".\"STATE\" = 'Ohio'\n" +
                "            and \"g1\".\"ZIPCODE\" = '45232'\n" +
                "          )\n" +
                "        )\n" +
                "      )\n" +
                "    ) \"g1\"\n" +
                "  ) \"addreses\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'ACCOUNT_ID' value \"g2\".\"ACCOUNT_ID\",\n" +
                "      key 'TYPE' value \"g2\".\"TYPE\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where (\n" +
                "        \"g0\".\"SSN\" = \"SSN\"\n" +
                "        and \"g2\".\"TYPE\" = 'Active'\n" +
                "      )\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g2\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void deepNestedFilterQuery4() throws Exception {
        String query = "{\n" +
                "  products (filter: {\n" +
                "   SYMBOL: {\n" +
                "    startsWith: \"D\"\n" +
                "  }\n" +
                "  }) {\n" +
                "    holdinges (filter: {\n" +
                "      PRODUCT_ID: {\n" +
                "        eq: 1008\n" +
                "      },\n" +
                "      and: {\n" +
                "        TRANSACTION_ID: {\n" +
                "          eq:10\n" +
                "        }\n" +
                "      },\n" +
                "      or: {\n" +
                "        TRANSACTION_ID: {\n" +
                "          eq: 33\n" +
                "        }\n" +
                "      }\n" +
                "    }) {\n" +
                "      PRODUCT_ID\n" +
                "      ACCOUNT_ID\n" +
                "      SHARES_COUNT\n" +
                "      TRANSACTION_ID\n" +
                "    }\n" +
                "    COMPANY_NAME\n" +
                "    SYMBOL\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'PRODUCT_ID' value \"g1\".\"PRODUCT_ID\",\n" +
                "      key 'ACCOUNT_ID' value \"g1\".\"ACCOUNT_ID\",\n" +
                "      key 'SHARES_COUNT' value \"g1\".\"SHARES_COUNT\",\n" +
                "      key 'TRANSACTION_ID' value \"g1\".\"TRANSACTION_ID\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.HOLDINGS\n" +
                "      where (\n" +
                "        \"g0\".\"ID\" = \"PRODUCT_ID\"\n" +
                "        and (\n" +
                "          (\n" +
                "            \"g1\".\"PRODUCT_ID\" = 1008\n" +
                "            and \"g1\".\"TRANSACTION_ID\" = 10\n" +
                "          )\n" +
                "          or \"g1\".\"TRANSACTION_ID\" = 33\n" +
                "        )\n" +
                "      )\n" +
                "      order by \"TRANSACTION_ID\"\n" +
                "    ) \"g1\"\n" +
                "  ) \"holdinges\",\n" +
                "  \"g0\".\"COMPANY_NAME\" \"COMPANY_NAME\",\n" +
                "  \"g0\".\"SYMBOL\" \"SYMBOL\"\n" +
                "from PUBLIC.PRODUCT \"g0\"\n" +
                "where \"g0\".\"SYMBOL\" like (replace(\n" +
                "  replace(\n" +
                "    replace(\n" +
                "      'D',\n" +
                "      '!',\n" +
                "      '!!'\n" +
                "    ),\n" +
                "    '%',\n" +
                "    '!%'\n" +
                "  ),\n" +
                "  '_',\n" +
                "  '!_'\n" +
                ") || '%') escape '!'\n" +
                "order by \"g0\".\"ID\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void deepNestedFilterQuery5() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    FIRSTNAME: {\n" +
                "      startsWith: \"J\"\n" +
                "    },\n" +
                "    not: {\n" +
                "      LASTNAME: {\n" +
                "        eq: \"Drew\"\n" +
                "      }\n" +
                "    }\n" +
                "  }) {\n" +
                "    addreses (filter: {\n" +
                "      STATE: {\n" +
                "        ne: \"Texas\"\n" +
                "      },\n" +
                "      or: {\n" +
                "        STATE: {\n" +
                "          eq: \"New York\"\n" +
                "        }\n" +
                "      },\n" +
                "      not: {\n" +
                "        ZIPCODE: {\n" +
                "          eq: \"19154\"\n" +
                "        }\n" +
                "      }\n" +
                "    }) {\n" +
                "      STATE\n" +
                "      ST_ADDRESS\n" +
                "      ZIPCODE\n" +
                "    }\n" +
                "    accounts (filter: {\n" +
                "      TYPE: {\n" +
                "        eq: \"Active\"\n" +
                "      }\n" +
                "    }) {\n" +
                "      ACCOUNT_ID\n" +
                "      TYPE\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'STATE' value \"g1\".\"STATE\",\n" +
                "      key 'ST_ADDRESS' value \"g1\".\"ST_ADDRESS\",\n" +
                "      key 'ZIPCODE' value \"g1\".\"ZIPCODE\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ADDRESS\n" +
                "      where (\n" +
                "        \"g0\".\"SSN\" = \"SSN\"\n" +
                "        and (\n" +
                "          (\n" +
                "            \"g1\".\"STATE\" <> 'Texas'\n" +
                "            and not (\"g1\".\"ZIPCODE\" = '19154')\n" +
                "          )\n" +
                "          or \"g1\".\"STATE\" = 'New York'\n" +
                "        )\n" +
                "      )\n" +
                "    ) \"g1\"\n" +
                "  ) \"addreses\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'ACCOUNT_ID' value \"g2\".\"ACCOUNT_ID\",\n" +
                "      key 'TYPE' value \"g2\".\"TYPE\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where (\n" +
                "        \"g0\".\"SSN\" = \"SSN\"\n" +
                "        and \"g2\".\"TYPE\" = 'Active'\n" +
                "      )\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g2\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "where (\n" +
                "  \"g0\".\"FIRSTNAME\" like (replace(\n" +
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
                "  and not (\"g0\".\"LASTNAME\" = 'Drew')\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void deepNestedFilterQuery6() throws Exception {
        String query = "{\n" +
                "  customers {\n" +
                "    addreses (filter: {\n" +
                "      or: {\n" +
                "        STATE : {\n" +
                "          eq: \"Ohio\"\n" +
                "        },\n" +
                "        ZIPCODE: {\n" +
                "          eq: \"45232\"\n" +
                "        }\n" +
                "      },\n" +
                "      STATE: {\n" +
                "        eq: \"New York\"\n" +
                "      }\n" +
                "    })  {\n" +
                "      STATE\n" +
                "      ST_ADDRESS\n" +
                "      ZIPCODE\n" +
                "    }\n" +
                "    accounts (filter: {\n" +
                "      TYPE: {\n" +
                "        eq: \"Active\"\n" +
                "      }\n" +
                "    }) {\n" +
                "      ACCOUNT_ID\n" +
                "      TYPE\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'STATE' value \"g1\".\"STATE\",\n" +
                "      key 'ST_ADDRESS' value \"g1\".\"ST_ADDRESS\",\n" +
                "      key 'ZIPCODE' value \"g1\".\"ZIPCODE\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ADDRESS\n" +
                "      where (\n" +
                "        \"g0\".\"SSN\" = \"SSN\"\n" +
                "        and (\n" +
                "          \"g1\".\"STATE\" = 'New York'\n" +
                "          or (\n" +
                "            \"g1\".\"STATE\" = 'Ohio'\n" +
                "            and \"g1\".\"ZIPCODE\" = '45232'\n" +
                "          )\n" +
                "        )\n" +
                "      )\n" +
                "    ) \"g1\"\n" +
                "  ) \"addreses\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'ACCOUNT_ID' value \"g2\".\"ACCOUNT_ID\",\n" +
                "      key 'TYPE' value \"g2\".\"TYPE\"\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where (\n" +
                "        \"g0\".\"SSN\" = \"SSN\"\n" +
                "        and \"g2\".\"TYPE\" = 'Active'\n" +
                "      )\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "    ) \"g2\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void notQuery() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    not: {\n" +
                "      SSN: {\n" +
                "        ne: \"CST01002\"\n" +
                "      }\n" +
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
                "where not (\"g0\".\"SSN\" <> 'CST01002')\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void notQuery2() throws Exception {
        String query = "{\n" +
                "  customers (filter: {\n" +
                "    not: {\n" +
                "      SSN: {\n" +
                "        eq: \"CST01002\"\n" +
                "      }\n" +
                "    }\n" +
                "    SSN: {\n" +
                "      eq: \"CST01004\"\n" +
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
                "where (\n" +
                "  \"g0\".\"SSN\" = 'CST01004'\n" +
                "  and not (\"g0\".\"SSN\" = 'CST01002')\n" +
                ")\n" +
                "order by \"g0\".\"SSN\"";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void page() throws Exception {
        String query = "{\n" +
                "  customers (page: {limit: 1, offset: 2}) {\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"\n" +
                "limit 1\n" +
                "offset 2";
        Assertions.assertEquals(expected,result);
    }

    @Test
    public void pageNested() throws Exception {
        String query = "{\n" +
                "  customers (page: {limit: 5}) {\n" +
                "    FIRSTNAME\n" +
                "    LASTNAME\n" +
                "    accounts (page: {limit:1}) {\n" +
                "      ACCOUNT_ID\n" +
                "      holdinges(page: {limit:1}) {\n" +
                "        SHARES_COUNT\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String result = executeSQL(query);
        String expected = "select\n" +
                "  \"g0\".\"FIRSTNAME\" \"FIRSTNAME\",\n" +
                "  \"g0\".\"LASTNAME\" \"LASTNAME\",\n" +
                "  (\n" +
                "    select json_arrayagg(json_object(\n" +
                "      key 'ACCOUNT_ID' value \"g1\".\"ACCOUNT_ID\",\n" +
                "      key 'holdinges' value (\n" +
                "        select json_arrayagg(json_object(key 'SHARES_COUNT' value \"g2\".\"SHARES_COUNT\"))\n" +
                "        from (\n" +
                "          select *\n" +
                "          from PUBLIC.HOLDINGS\n" +
                "          where \"g1\".\"ACCOUNT_ID\" = \"ACCOUNT_ID\"\n" +
                "          order by \"TRANSACTION_ID\"\n" +
                "          limit 1\n" +
                "        ) \"g2\"\n" +
                "      )\n" +
                "    ))\n" +
                "    from (\n" +
                "      select *\n" +
                "      from PUBLIC.ACCOUNT\n" +
                "      where \"g0\".\"SSN\" = \"SSN\"\n" +
                "      order by \"ACCOUNT_ID\"\n" +
                "      limit 1\n" +
                "    ) \"g1\"\n" +
                "  ) \"accounts\"\n" +
                "from PUBLIC.CUSTOMER \"g0\"\n" +
                "order by \"g0\".\"SSN\"\n" +
                "limit 5";
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




