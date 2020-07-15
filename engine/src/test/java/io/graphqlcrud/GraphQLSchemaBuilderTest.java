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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

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
            assertNotNull(connection);
            Schema s = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            assertNotNull(s);
            
            // TODO: write more tests validating the schema generated
            GraphQLSchema gqls = GraphQLSchemaBuilder.getSchema(s);
            assertEquals("QueryType", gqls.getQueryType().getName());
        }
    }
}
