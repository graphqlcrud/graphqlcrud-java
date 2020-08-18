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
        try (Connection connection = datasource.getConnection()){
            Assertions.assertNotNull(connection);
            Schema s = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            Assertions.assertNotNull(s);

            GraphQLSchema schema = GraphQLSchemaBuilder.getSchema(s);
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

            GraphQLFieldDefinition fieldDefinition = objectType.getFieldDefinition("customers");
            Assertions.assertEquals("page",fieldDefinition.getArguments().get(0).getName());
            Assertions.assertEquals("filter", fieldDefinition.getArguments().get(1).getName());
            if(fieldDefinition.getArgument("filter").getType().toString().contains("CustomerFilterType")) {
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("APT_NUMBER"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("CITY"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("FIRSTNAME"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("LASTNAME"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("ST_ADDRESS"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("ZIPCODE"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("PHONE"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("STATE"));
                Assertions.assertTrue(fieldDefinition.getArgument("filter").getType().getChildren().toString().contains("SSN"));
            }
        }
   }
}
