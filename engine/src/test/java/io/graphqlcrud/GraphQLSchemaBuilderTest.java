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
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.agroal.api.AgroalDataSource;
import io.graphqlcrud.model.Schema;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTestResource(H2DatabaseTestResource.class)
@QuarkusTest
public class GraphQLSchemaBuilderTest {

    @Inject
    private AgroalDataSource datasource;

    @Test
    public void testSchemaPrint() throws Exception {
        try (Connection connection = this.datasource.getConnection()){
            Assertions.assertNotNull(connection);
            Schema s = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            Assertions.assertNotNull(s);

            GraphQLSchema schema = GraphQLSchemaBuilder.getSchema(s);
//            SchemaPrinter sp = new SchemaPrinter();
//            System.out.println(sp.print(schema));

            GraphQLObjectType objectType = schema.getQueryType();
            Assertions.assertNotNull(objectType);
            Assertions.assertEquals("QueryType", objectType.getName());
            Assertions.assertEquals("account",objectType.getFieldDefinition("account").getName());
            Assertions.assertEquals("ACCOUNT_ID",objectType.getFieldDefinition("account").getArguments().get(0).getName());

            GraphQLObjectType customer = schema.getObjectType("CUSTOMER");
            Assertions.assertNotNull(customer);
            Assertions.assertEquals("PUBLIC.CUSTOMER", customer.getDirective("sql").getArgument("table").getValue().toString());
            GraphQLFieldDefinition accountsDef = customer.getFieldDefinition("accounts");
            Assertions.assertNotNull(accountsDef.getDirective("sql"));
            Assertions.assertEquals("[SSN]", accountsDef.getDirective("sql").getArgument("keys").getValue().toString());
            Assertions.assertEquals("[SSN]", accountsDef.getDirective("sql").getArgument("reference_keys").getValue().toString());

            GraphQLObjectType account = schema.getObjectType("ACCOUNT");
            Assertions.assertNotNull(account);
            Assertions.assertEquals("PUBLIC.ACCOUNT", account.getDirective("sql").getArgument("table").getValue().toString());
            GraphQLFieldDefinition holdings = account.getFieldDefinition("holdinges");
            Assertions.assertNotNull(holdings.getDirective("sql"));
            Assertions.assertEquals("[ACCOUNT_ID]", holdings.getDirective("sql").getArgument("keys").getValue().toString());
            Assertions.assertEquals("[ACCOUNT_ID]", holdings.getDirective("sql").getArgument("reference_keys").getValue().toString());

            GraphQLObjectType product = schema.getObjectType("PRODUCT");
            Assertions.assertNotNull(product);
            Assertions.assertEquals("PUBLIC.PRODUCT", product.getDirective("sql").getArgument("table").getValue().toString());
            holdings = product.getFieldDefinition("holdinges");
            Assertions.assertNotNull(holdings.getDirective("sql"));
            Assertions.assertEquals("[ID]", holdings.getDirective("sql").getArgument("keys").getValue().toString());
            Assertions.assertEquals("[PRODUCT_ID]", holdings.getDirective("sql").getArgument("reference_keys").getValue().toString());

            Assertions.assertEquals("page", holdings.getArgument("page").getName());
            Assertions.assertTrue(holdings.getArgument("page").getType().toString().contains("PageRequest"));
            Assertions.assertEquals("orderBy", holdings.getArgument("orderBy").getName());
            Assertions.assertTrue(holdings.getArgument("orderBy").getType().toString().contains("OrderByInput"));
            Assertions.assertEquals("filter", holdings.getArgument("filter").getName());
            Assertions.assertTrue(holdings.getArgument("filter").getType().toString().contains("HoldingsFilterInput"));
        }
   }
}
