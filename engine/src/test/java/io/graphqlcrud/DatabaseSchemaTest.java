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
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.agroal.api.AgroalDataSource;
import io.graphqlcrud.model.Attribute;
import io.graphqlcrud.model.Cardinality;
import io.graphqlcrud.model.Entity;
import io.graphqlcrud.model.Relation;
import io.graphqlcrud.model.Schema;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTestResource(H2DatabaseTestResource.class)
@QuarkusTest
public class DatabaseSchemaTest {

    @Inject
    private AgroalDataSource datasource;

    @Test
    public void testSchemaPrint() throws Exception {
        try (Connection connection = this.datasource.getConnection()){
            Assertions.assertNotNull(connection);
            Schema s = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            Assertions.assertNotNull(s);
            List<Entity> entities = s.getEntities();
            Collections.sort(entities);
            Assertions.assertEquals(5, entities.size());

            Assertions.assertEquals("CUSTOMER", entities.get(2).getName());
            List<Attribute> customerAttributes = entities.get(2).getAttributes();
            Assertions.assertEquals(4, customerAttributes.size());
            Assertions.assertEquals("SSN", customerAttributes.get(3).getName());
            Assertions.assertEquals(Types.CHAR, customerAttributes.get(3).getType());

            Assertions.assertEquals("ACCOUNT",entities.get(4).getName());
            List<Relation> accountRelations = entities.get(4).getRelations();
            Assertions.assertEquals(Cardinality.ONE_TO_MANY, accountRelations.get(0).getCardinality());
            Assertions.assertEquals("accounts",accountRelations.get(0).getName());
        }
    }
}
