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

import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.Scalars;
import graphql.language.OperationTypeDefinition;
import graphql.language.SchemaDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLObjectType.Builder;
import io.graphqlcrud.model.Entity;
import io.graphqlcrud.model.Schema;
import io.graphqlcrud.types.JdbcTypeMap;

public class GraphQLSchemaBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLSchemaBuilder.class);
    private static final JdbcTypeMap TYPE_MAP = new JdbcTypeMap();
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

        //add filters for queries
        builder.additionalType(Filters.pageInputBuilder().build());
        builder.additionalType(Filters.stringInputBuilder().build());
        builder.additionalType(Filters.intInputBuilder().build());
        builder.additionalType(Filters.idInputBuilder().build());
        builder.additionalType(Filters.booleanInputBuilder().build());
        builder.additionalType(Filters.floatInputBuilder().build());
        builder.additionalType(Filters.sortDirectionEnumBuilder().build());
        builder.additionalType(Filters.orderByInputBuilder().build());

        //add FilterInputTypes
        schema.getEntities().stream().forEach(filterEntity -> {
            GraphQLInputObjectType.Builder  entityBuilder = GraphQLInputObjectType.newInputObject();
            String filterName = StringUtil.capitalize(filterEntity.getName().toLowerCase()) + "FilterInput";
            entityBuilder.name(filterName);
            buildFilterFields(filterEntity).stream().forEach(filter -> {
                entityBuilder.field(filter.build());
                entityBuilder.field(GraphQLInputObjectField.newInputObjectField().name("not").type(GraphQLTypeReference.typeRef(filterName)));
                entityBuilder.field(GraphQLInputObjectField.newInputObjectField().name("and").type(GraphQLList.list(GraphQLTypeReference.typeRef(filterName))));
                entityBuilder.field(GraphQLInputObjectField.newInputObjectField().name("or").type(GraphQLList.list(GraphQLTypeReference.typeRef(filterName))));
            });
            builder.additionalType(entityBuilder.build());
        });

        GraphQLObjectType.Builder mutationTypeBuilder = GraphQLObjectType.newObject();
        mutationTypeBuilder.name("MutationType");

        schema.getEntities().forEach(mutation -> {
            GraphQLInputObjectType.Builder mutationBuilder = GraphQLInputObjectType.newInputObject();
            String name = "Mutate" + StringUtil.capitalize(mutation.getName().toLowerCase()) + "Input";
            mutationBuilder.name(name);
            buildMutations(mutation, codeBuilder).forEach(updateField -> {
                mutationBuilder.field(updateField.build());
            });

            builder.additionalType(mutationBuilder.build());

            //add create type to MutationType
            addMutationForEntity(mutation, mutationTypeBuilder, "create", codeBuilder);

            //add update type to MutationType
            addMutationForEntity(mutation, mutationTypeBuilder, "update", codeBuilder);

            //add delete type to MutationType
            addMutationForEntity(mutation, mutationTypeBuilder, "delete", codeBuilder);
        });

        builder.mutation(mutationTypeBuilder.build());

        schema.getEntities().stream().forEach(entity -> {
            GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject();
            typeBuilder.description(entity.getDescription());
            typeBuilder.name(entity.getName());
            typeBuilder.withDirective(SQLDirective.newDirective().tableName(entity.getFullName()).build());

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

        builder.additionalDirectives(SQLDirective.addDirectiveToSchema("sql"));

        builder.codeRegistry(codeBuilder.build());

        return builder.build();
    }

    private void addMutationForEntity(Entity entity, Builder mutationTypeBuilder, String name, GraphQLCodeRegistry.Builder codeBuilder) {
        String argumentType;
        String fieldName;
        GraphQLFieldDefinition.Builder field = GraphQLFieldDefinition.newFieldDefinition();

        switch (name) {
            case "create":
                fieldName = "create" + StringUtil.capitalize(entity.getName().toLowerCase());
                argumentType = "Mutate" + StringUtil.capitalize(entity.getName().toLowerCase()) + "Input";
                field.argument(GraphQLArgument.newArgument().name("input").type(GraphQLNonNull.nonNull(new GraphQLTypeReference(argumentType))).build());
                field.type(new GraphQLTypeReference(entity.getName()));
                break;
            case "update":
                fieldName = "update" + StringUtil.capitalize(entity.getName().toLowerCase());
                argumentType = "Mutate" + StringUtil.capitalize(entity.getName().toLowerCase()) + "Input";
                String conditionalUpdate = StringUtil.capitalize(entity.getName().toLowerCase()) + "FilterInput";
                field.argument(GraphQLArgument.newArgument().name("filter").type(new GraphQLTypeReference(conditionalUpdate)).build());
                field.argument(GraphQLArgument.newArgument().name("input").type(GraphQLNonNull.nonNull(new GraphQLTypeReference(argumentType))).build());
                field.type(GraphQLList.list(new GraphQLTypeReference(entity.getName())));

                break;
            case "delete":
                fieldName = "delete" + StringUtil.capitalize(entity.getName().toLowerCase());
                argumentType = StringUtil.capitalize(entity.getName().toLowerCase()) + "FilterInput";
                field.argument(GraphQLArgument.newArgument().name("filter").type(GraphQLNonNull.nonNull(new GraphQLTypeReference(argumentType))).build());
                field.type(GraphQLList.list(new GraphQLTypeReference(entity.getName())));
                break;
            default:
                throw new RuntimeException("Unexpected value: " + name);
        }

        field.name(fieldName);
        mutationTypeBuilder.field(field.build());
        codeBuilder.dataFetcher(FieldCoordinates.coordinates("MutationType", fieldName), DEFAULT_DATA_FETCHER_FACTORY);
    }

    private void addQueryOperationsForEntity(Entity entity, Builder queryTypeBuilder, GraphQLCodeRegistry.Builder codeBuilder) {
        // get "findAll" kind of method for the Entity
        {
            String name = StringUtil.plural(entity.getName()).toLowerCase();
            GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
            builder.name(name);
            builder.argument(GraphQLArgument.newArgument().name("page").type(GraphQLTypeReference.typeRef("PageRequest")).build());
            builder.argument(GraphQLArgument.newArgument().name("filter").type(GraphQLTypeReference.typeRef(StringUtil.capitalize(entity.getName().toLowerCase()) + "FilterInput")).build());
            builder.argument(GraphQLArgument.newArgument().name("orderBy").type(GraphQLTypeReference.typeRef("OrderByInput")).build());
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
                    fieldBuilder.type(TYPE_MAP.getAsGraphQLTypeString(attr.getType()));
                } else {
                    fieldBuilder.type(GraphQLNonNull.nonNull(TYPE_MAP.getAsGraphQLTypeString(attr.getType())));
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
                    if(relation.isExportedKey()) {
                        fieldBuilder.type(new GraphQLTypeReference(e.getName()));
                    }
                    else {
                        fieldBuilder.argument(GraphQLArgument.newArgument().name("page").type(GraphQLTypeReference.typeRef("PageRequest")).build());
                        fieldBuilder.argument(GraphQLArgument.newArgument().name("filter").type(GraphQLTypeReference.typeRef(StringUtil.capitalize(e.getName().toLowerCase()) + "FilterInput")).build());
                        fieldBuilder.argument(GraphQLArgument.newArgument().name("orderBy").type(GraphQLTypeReference.typeRef("OrderByInput")).build());
                        if (relation.isNullable()) {
                            fieldBuilder.type(GraphQLList.list(new GraphQLTypeReference(e.getName())));
                        } else {
                            fieldBuilder.type(GraphQLNonNull.nonNull(GraphQLList.list(new GraphQLTypeReference(e.getName()))));
                        }
                    }
                    fieldBuilder.withDirective(SQLDirective.newDirective()
                            .primaryFields(new ArrayList<String>(relation.getKeyColumns().values()))
                            .foreignFields(new ArrayList<String>(relation.getReferencedKeyColumns().values())).build());
                    fields.add(fieldBuilder);
                    codeBuilder.dataFetcher(FieldCoordinates.coordinates(entity.getName(), relation.getName()), DEFAULT_ROW_FETCHER_FACTORY);
                }
            });
        });
        return fields;
    }

    protected List<GraphQLInputObjectField.Builder> buildFilterFields(Entity filterEntity) {
        ArrayList<GraphQLInputObjectField.Builder> filterFields = new ArrayList<GraphQLInputObjectField.Builder>();

        filterEntity.getAttributes().stream().forEach(attr -> {
            GraphQLInputObjectField.Builder builder = GraphQLInputObjectField.newInputObjectField();
            builder.name(attr.getName());
            builder.type(TYPE_MAP.getAsGraphQLFilterType(attr.getType()));
            filterFields.add(builder);
        });
        return filterFields;
    }

    protected List<GraphQLInputObjectField.Builder> buildMutations(Entity mutationEntity, GraphQLCodeRegistry.Builder codeBuilder) {
        ArrayList<GraphQLInputObjectField.Builder> mutationFields = new ArrayList<>();

        mutationEntity.getAttributes().forEach(attribute -> {
            GraphQLInputObjectField.Builder builder = GraphQLInputObjectField.newInputObjectField();
            builder.name(attribute.getName());
            if (mutationEntity.isPartOfPrimaryKey(attribute.getName())) {
                builder.type(Scalars.GraphQLID);
            } else {
                builder.type(TYPE_MAP.getAsGraphQLTypeStringForInput(attribute.getType()));
            }
            mutationFields.add(builder);
            codeBuilder.dataFetcher(FieldCoordinates.coordinates(mutationEntity.getName(), attribute.getName()), DEFAULT_DATA_FETCHER_FACTORY);
        });
        return mutationFields;
    }
}
