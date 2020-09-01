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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.graphqlcrud.model.Attribute;
import io.graphqlcrud.model.Cardinality;
import io.graphqlcrud.model.Entity;
import io.graphqlcrud.model.Relation;
import io.graphqlcrud.model.Schema;

public class DatabaseSchemaBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSchemaBuilder.class);
    private final DatabaseMetaData databaseMetaData;
    private final String schema;

    protected DatabaseSchemaBuilder(Connection connection, String schema) throws SQLException {
        this.databaseMetaData = connection.getMetaData();
        this.schema = schema;
    }

    public static Schema getSchema(Connection connection, String schema) throws SQLException {
        DatabaseSchemaBuilder analyzer = new DatabaseSchemaBuilder(connection, schema);
        return analyzer.buildSchema();
    }

    protected Schema buildSchema() throws SQLException {
        Schema s = new Schema(this.schema);
        Map<String, Entity> entityMap = new TreeMap<String, Entity>();
        try (ResultSet results = this.databaseMetaData.getTables(null, this.schema, null,
                new String[] { "TABLE", "VIEW" })) {
            while (results.next()) {
                String tableName = results.getString("TABLE_NAME");
                if (tableName.equals("flyway_schema_history")) {
                    continue;
                }
                Entity entity = buildEntity(tableName);
                entity.setParent(s);
                entityMap.put(tableName, entity);
                LOGGER.debug(entity.toString());
            }
        }
        // build relations
        buildRelations(entityMap);

        // fill the schema
        s.setEntities(new ArrayList<Entity>(entityMap.values()));
        return s;
    }

    protected Entity buildEntity(String tableName) throws SQLException {
        Entity entity = new Entity(tableName);
        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet results = this.databaseMetaData.getPrimaryKeys(null, this.schema, tableName)) {
            LOGGER.debug("Loading primary keys for table: " + tableName);
            while (results.next()) {
                String name = results.getString("COLUMN_NAME");
                primaryKeys.add(name);
            }
        }
        entity.setPrimaryKeys(primaryKeys);

        try (ResultSet results = this.databaseMetaData.getColumns(null, this.schema, tableName, "%")) {
            LOGGER.debug("Loading columns for table: " + tableName);
            while (results.next()) {
                String name = results.getString("COLUMN_NAME");
                int dataType = results.getInt("DATA_TYPE");
                boolean isNullable = results.getInt("NULLABLE") == 1;

                Attribute attribute = new Attribute(name, dataType, isNullable);
                entity.addAttribute(attribute);
            }
        }
        return entity;
    }

    private short safeGetShort(ResultSet rs, String pos) throws SQLException {
        short val;
        try {
            val = rs.getShort(pos);
        } catch (SQLException e) {
            int valInt = rs.getInt(pos);
            if (valInt > Short.MAX_VALUE) {
                throw new SQLException("invalid short value " + valInt); //$NON-NLS-1$
            }
            val = (short) valInt;
        }
        return val;
    }

    private void buildRelations(Map<String, Entity> entityMap) throws SQLException {
        for (Entity entity : entityMap.values()) {
            HashMap<String, Relation> allRelations = new HashMap<String, Relation>();

            ResultSet metaDataExportedKeys = this.databaseMetaData.getExportedKeys(null,this.schema,entity.getName());
            setDatabaseMetaData(metaDataExportedKeys, entityMap, allRelations, true);

            ResultSet metaDataImportedKeys = this.databaseMetaData.getImportedKeys(null, this.schema, entity.getName());
            setDatabaseMetaData(metaDataImportedKeys, entityMap, allRelations, false);

            entity.setRelations(new ArrayList<Relation>(allRelations.values()));
        }
    }

    public void setDatabaseMetaData(ResultSet resultSet, Map<String, Entity> entityMap, HashMap<String, Relation> allRelations, boolean ctr) throws SQLException {
        while (resultSet.next()) {
            String pkTable = resultSet.getString("PKTABLE_NAME");
            String pkColumn = resultSet.getString("PKCOLUMN_NAME");
            String fkTable = resultSet.getString("FKTABLE_NAME");
            String fkColumn = resultSet.getString("FKCOLUMN_NAME");
            short seqNumber = safeGetShort(resultSet,"KEY_SEQ");

            Entity pkEntity = entityMap.get(pkTable);
            if (pkEntity == null) {
                continue;
            }

            Entity fkEntity = entityMap.get(fkTable);
            if (fkEntity == null) {
                continue;
            }

            Relation key;

            if(ctr) {
                key = allRelations.get(fkTable);
                if (key == null) {
                    key = new Relation(pkTable.toLowerCase());
                    key.setForeignEntity(fkEntity);
                    allRelations.put(fkTable, key);
                }
                key.getKeyColumns().put(seqNumber, fkColumn);
                key.getReferencedKeyColumns().put(seqNumber, pkColumn);
                key.setExportedKey(true);
            } else {
                key = allRelations.get(pkTable);
                if (key == null) {
                    key = new Relation(StringUtil.plural(fkTable).toLowerCase());
                    key.setForeignEntity(pkEntity);
                    allRelations.put(pkTable, key);
                }
                key.getKeyColumns().put(seqNumber, pkColumn);
                key.getReferencedKeyColumns().put(seqNumber, fkColumn);
            }

            if (pkEntity.isPartOfPrimaryKey(pkColumn) && fkEntity.isPartOfPrimaryKey(fkColumn)) {
                key.setCardinality(Cardinality.ONE_TO_ONE);
            } else {
                key.setNullable(fkEntity.getAttribute(fkColumn).isNullable());
                key.setCardinality(Cardinality.ONE_TO_MANY);
            }
        }
    }
}