package org.graphqlcrudjava;

import graphql.Scalars;
import graphql.schema.*;
import graphql.schema.idl.SchemaPrinter;
import org.graphqlcrudjava.model.Attribute;
import org.graphqlcrudjava.model.Cardinality;
import org.graphqlcrudjava.model.Entity;
import org.graphqlcrudjava.model.Relation;
import org.graphqlcrudjava.types.TypeMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class DatabaseAnalyzer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAnalyzer.class);

    private final DatabaseMetaData databaseMetaData;

    private final String catalog;
    private final String schema;
    private final TypeMapImpl typeMap = new TypeMapImpl();

    public DatabaseAnalyzer(Connection connection) throws SQLException {
        this.databaseMetaData = connection.getMetaData();
        this.schema = connection.getSchema();
        this.catalog = connection.getCatalog();
    }

    @Override
    public void close() {
        if (this.databaseMetaData != null) {
            Connection connection;
            try {
                connection = this.databaseMetaData.getConnection();
                connection.close();
            } catch (SQLException e) {
                LOGGER.error("Unable to close database connection", e);
            }
        }
    }

    public List<Entity> initializeEntities() throws SQLException {
        List<Entity> entities = new ArrayList<>();
        try (ResultSet results = this.databaseMetaData
                .getTables(this.catalog, this.schema, null, new String[]{"TABLE", "VIEW"})) {
            while (results.next()) {
                String tableName = results.getString("TABLE_NAME");
                Entity entity = initializeEntity(tableName);
                if (entity != null) {
                    LOGGER.debug(entity.toString());
                    entities.add(entity);
                }
            }
        }

        Map<String, Entity> entityMap = initializeEntityMap(entities);
        initializeRelations(entityMap);

        buildNewSchema(entities);

        return entities;
    }

    public Entity initializeEntity(String table) throws SQLException {
        List<GraphQLNamedOutputType> graphQLNamedOutputTypes = Collections.emptyList();
        List<GraphQLFieldDefinition> graphQLFieldDefinitions = Collections.emptyList();
        Entity entity = new Entity(table,null,graphQLFieldDefinitions,graphQLNamedOutputTypes );
        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet results = this.databaseMetaData.getPrimaryKeys(catalog, schema, table)) {
            LOGGER.debug("Loading primary keys for table: " + table);
            while (results.next()) {
                String name = results.getString("COLUMN_NAME");
                primaryKeys.add(name);
            }
        }

        try (ResultSet results = this.databaseMetaData.getColumns(catalog, schema, table, "%")) {
            LOGGER.debug("Loading columns for table: " + table);
            while (results.next()) {
                Attribute attribute = initializeAttribute(results, primaryKeys);
                entity.addAttribute(attribute);
            }
        }
        return entity;
    }

    public Attribute initializeAttribute(ResultSet results, List<String> primaryKeys) throws SQLException {
        List<GraphQLArgument> graphQLArguments = new ArrayList<>();
        String name = results.getString("COLUMN_NAME");
        int position = results.getInt("ORDINAL_POSITION");
        int dataType = results.getInt("DATA_TYPE");
        GraphQLOutputType type =  typeMap.getAsGraphQLTypeString(dataType);
        boolean isNullable = results.getInt("NULLABLE") == 1;
        boolean isPrimaryKey = primaryKeys.contains(name);

        return new Attribute(name, null, type, null, graphQLArguments, null, position, isPrimaryKey, isNullable);
    }

    private void initializeRelations(Map<String, Entity> entityMap) throws SQLException {
        List<GraphQLArgument> graphQLArguments = new ArrayList<>();
        for (Entity e : entityMap.values()) {
            try (ResultSet results = this.databaseMetaData.getImportedKeys(catalog, schema, e.getName())) {
                LOGGER.debug("Loading imported keys for table: " + e.getName());
                while (results.next()) {
                    String pktable = results.getString("PKTABLE_NAME");
                    String pkColumn = results.getString("PKCOLUMN_NAME");
                    String fkTable = results.getString("FKTABLE_NAME");
                    String fkColumn = results.getString("FKCOLUMN_NAME");

                    Entity pkEntity = entityMap.get(pktable);
                    Attribute pkAttribute = getMatchingAttribute(pkEntity.getAttributes(), pkColumn);

                    Entity fkEntity = entityMap.get(fkTable);
                    Attribute fkAttribute = getMatchingAttribute(fkEntity.getAttributes(), fkColumn);

                    LOGGER.debug(String.format("Relation: %s.%s (1) <- %s.%s (M)", pktable, pkColumn, fkTable, fkColumn));
                    {
                        GraphQLOutputType type = GraphQLList.list(GraphQLTypeReference.typeRef(fkEntity.getName()));
                        Attribute newAttribute = new Attribute(
                                fkEntity.getName(),
                                null,
                                type,
                                null,
                                graphQLArguments,
                                null,
                                pkEntity.maxPosition(),
                                false,
                                fkAttribute.isNullable());
                        Relation newRelation = new Relation(fkEntity, fkAttribute, Cardinality.MANY);
                        newAttribute.setForeignKey(newRelation);
                        pkEntity.addAttribute(newAttribute);
                    }

                    for (Attribute a : e.getAttributes()) {
                        if (a.getName().equals(fkColumn)) {
                            Relation newRelation = new Relation(pkEntity, pkAttribute, Cardinality.ONE);
                            a.setForeignKey(newRelation);
                        }
                    }
                }
            }
        }
    }

    private Attribute getMatchingAttribute(Set<Attribute> attributes, String column) {
        for(Attribute a : attributes) {
            if(a.getName().equals(column)) {
                return a;
            }
        }
        throw new IllegalStateException("Searching for unknown column: " + column);
    }

    private Map<String, Entity> initializeEntityMap(List<Entity> entities) {
        Map<String, Entity> entityMap = new HashMap<>();
        for(Entity e : entities) {
            entityMap.put(e.getName(), e);
        }
        return entityMap;
    }

    private void buildNewSchema(List<Entity> entities) {
        SchemaPrinter schemaPrinter = new SchemaPrinter();
        List<GraphQLObjectType.Builder> builderList = new ArrayList<>();
        Set<Attribute> attributes;
        for(Entity entity : entities) {
            GraphQLObjectType.Builder graphQLObjectType = new GraphQLObjectType.Builder();
            attributes = entity.getAttributes();
            for(Attribute attribute : attributes) {
                graphQLObjectType
                        .name(entity.getName())
                        .field(f -> f.name(formatName(attribute))
                            .type(formatAttribute(attribute)))
                        .description(attribute.getDescription())
                        .build();
            }
            builderList.add(graphQLObjectType);
            }
            String printer;
            for(GraphQLObjectType.Builder o : builderList) {
                printer = schemaPrinter.print(o.build());
                System.out.println(printer);
        }
    }

    private GraphQLOutputType formatAttribute(Attribute attribute) {
        GraphQLOutputType type = attribute.getType();

        if(!attribute.isNullable()) {
            type = GraphQLNonNull.nonNull(attribute.getType());
        }

        if(attribute.isPrimaryKey()) {
            return formatIDType(attribute);
        }

        if(attribute.getForeignKey() != null) {
            return formatRelationType(attribute);
        }

        return type;
    }

    private GraphQLOutputType formatRelationType(Attribute attribute) {
        Relation relation = attribute.getForeignKey();
        GraphQLOutputType type = attribute.getType();

        if(!attribute.isNullable()) {
            type = GraphQLNonNull.nonNull(attribute.getType());
        }

        if(relation.getCardinality() == Cardinality.MANY) {
            if(!relation.getForeignAttribute().isNullable()) {
                type = GraphQLNonNull.nonNull(attribute.getType());
            }
        }
        return type;
    }

    private GraphQLOutputType formatIDType(Attribute attribute) {
        GraphQLOutputType type = Scalars.GraphQLID;

        if(!attribute.isNullable()) {
            type = GraphQLNonNull.nonNull(Scalars.GraphQLID);
        }

        return  type;
    }

    private String formatName(Attribute attribute) {
        String name = attribute.getName();

        if(attribute.getForeignKey() != null) {
            Relation relation = attribute.getForeignKey();
            String type = relation.getForeignEntity().getName();
            if(relation.getCardinality() == Cardinality.MANY) {
                name = type;
            }
        }
        return name;
    }

}