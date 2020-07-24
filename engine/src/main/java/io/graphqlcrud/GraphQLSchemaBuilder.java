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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.Scalars;
import graphql.language.OperationTypeDefinition;
import graphql.language.SchemaDefinition;
import graphql.language.TypeName;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import io.graphqlcrud.model.Entity;
import io.graphqlcrud.model.Schema;
import io.graphqlcrud.types.JdbcTypeMap;

public class GraphQLSchemaBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLSchemaBuilder.class);
    private static final JdbcTypeMap TYPEMAP = new JdbcTypeMap();
    private static final SQLDataFetcherFactory DEFAULT_DATA_FETCHER_FACTORY = new SQLDataFetcherFactory();
    private static final RowFetcherFactory DEFAULT_ROW_FETCHER_FACTORY = new RowFetcherFactory();

    public static GraphQLSchema getSchema(Schema schema) {
        GraphQLSchemaBuilder b = new GraphQLSchemaBuilder();
        return b.buildSchema(schema);
    }    
    
    public GraphQLSchema buildSchema(Schema schema) {
        LOGGER.debug("Building GraphQL Schema based entity model");
        GraphQLSchema.Builder builder = GraphQLSchema.newSchema();
        
        // add Code Registry
        GraphQLCodeRegistry.Builder codeBuilder = GraphQLCodeRegistry.newCodeRegistry();
        
        GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject();
        queryTypeBuilder.name("QueryType");
        
        schema.getEntities().stream().forEach(entity -> {
            GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject();
            typeBuilder.name(entity.getName());
            
            // Add fields in a Type
            buildTypeFields(entity, schema, codeBuilder).stream().forEach(fieldBuilder -> {
                typeBuilder.field(fieldBuilder.build());    
            });

            // Add Type
            builder.additionalType(typeBuilder.build());
            
            // add to main QueryType
            addQueryOperationsForEntity(entity, queryTypeBuilder, codeBuilder);
        });
        
        // add "QueryType" that we have been building
        builder.query(queryTypeBuilder.build());
        
        // Add Schema Definition
        SchemaDefinition.Builder schemaDefinitionBuilder = SchemaDefinition.newSchemaDefinition();
        OperationTypeDefinition.Builder opBuilder = OperationTypeDefinition.newOperationTypeDefinition();
        opBuilder.name("query").typeName(TypeName.newTypeName("QueryType").build());
        schemaDefinitionBuilder.operationTypeDefinition(opBuilder.build());
        builder.definition(schemaDefinitionBuilder.build());
        
        builder.codeRegistry(codeBuilder.build());
        
        return builder.build();
    }
    
    private GraphQLDirective sqlDirective(Entity entity) {
        return GraphQLDirective.newDirective().name("sql").argument(GraphQLArgument.newArgument().name("from")
                .type(Scalars.GraphQLString).value(entity.getParent().getName() + "." + entity.getName())).build();
    }
    
    private void addQueryOperationsForEntity(Entity entity, Builder queryTypeBuilder, GraphQLCodeRegistry.Builder codeBuilder) {
        // get "findAll" kind of method for the Entity
        {
        String name = StringUtil.plural(entity.getName()).toLowerCase();
        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
        builder.name(name);
        builder.withDirective(sqlDirective(entity));
        builder.type(GraphQLList.list(new GraphQLTypeReference(entity.getName())));
        queryTypeBuilder.field(builder.build());
        codeBuilder.dataFetcher(FieldCoordinates.coordinates("QueryType", name), DEFAULT_DATA_FETCHER_FACTORY);
        }

        // get find(id) like method for entity
        {
        if (!entity.getPrimaryKeys().isEmpty()) {
            String name = entity.getName().toLowerCase();
            GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
            builder.name(name);
            builder.type(new GraphQLTypeReference(entity.getName()));
            builder.withDirective(sqlDirective(entity));
            entity.getPrimaryKeys().stream().forEach(str -> {
                GraphQLArgument.Builder argument = GraphQLArgument.newArgument();
                argument.name(str).type(Scalars.GraphQLID);
                builder.argument(argument.build());
            });
            queryTypeBuilder.field(builder.build());
            codeBuilder.dataFetcher(FieldCoordinates.coordinates("QueryType", name), DEFAULT_DATA_FETCHER_FACTORY);
        }
        }
    }

    protected List<GraphQLFieldDefinition.Builder> buildTypeFields(Entity entity, Schema schema,
            GraphQLCodeRegistry.Builder codeBuilder) {
        ArrayList<GraphQLFieldDefinition.Builder> fields = new ArrayList<GraphQLFieldDefinition.Builder>();
        
        // build fields for every attribute
        entity.getAttributes().stream().forEach(attr -> {
            GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition();
            fieldBuilder.name(attr.getName());
            if (entity.isPartOfPrimaryKey(attr.getName())) {
                fieldBuilder.type(GraphQLNonNull.nonNull(Scalars.GraphQLID));
            } else {
                if (attr.isNullable()) {
                    fieldBuilder.type(TYPEMAP.getAsGraphQLTypeString(attr.getType()));
                } else {
                   fieldBuilder.type(GraphQLNonNull.nonNull(TYPEMAP.getAsGraphQLTypeString(attr.getType())));
                }
            }
            fields.add(fieldBuilder);
            codeBuilder.dataFetcher(FieldCoordinates.coordinates(entity.getName(), attr.getName()), DEFAULT_ROW_FETCHER_FACTORY);
        });
        
        // build fields based on relationships which reference to other types
        schema.getEntities().stream().forEach(e -> {
            e.getRelations().stream().forEach(relation -> {
                if (relation.getForeignEntity().getName().equals(entity.getName())) {
                    GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition();
                    fieldBuilder.name(relation.getName());
                    if (relation.isNullable()) {
                        fieldBuilder.type(GraphQLList.list(new GraphQLTypeReference(e.getName())));
                    } else {
                        fieldBuilder.type(GraphQLNonNull.nonNull(GraphQLList.list(new GraphQLTypeReference(e.getName()))));
                    }
                    fields.add(fieldBuilder);
                }
            });
        });
        return fields;
    }
}
