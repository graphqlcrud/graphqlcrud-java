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
            try (ResultSet results = this.databaseMetaData.getImportedKeys(null, this.schema, entity.getName())) {
                LOGGER.debug("Loading imported keys for table: " + entity.getName());
                while (results.next()) {
                    String pktable = results.getString("PKTABLE_NAME"); // 3
                    String pkColumn = results.getString("PKCOLUMN_NAME"); // 4
                    String fktable = results.getString("FKTABLE_NAME"); // 7
                    String fkColumn = results.getString("FKCOLUMN_NAME"); // 8
                    short seqNum = safeGetShort(results, "KEY_SEQ");

                    Entity pkEntity = entityMap.get(pktable);
                    if (pkEntity == null) {
                        continue;
                    }
                    
                    Entity fkEntity = entityMap.get(fktable);
                    if (fkEntity == null) {
                        continue;
                    }
                    /*
                    String fkName = results.getString("FK_NAME");
                    if (fkName == null) {
                        fkName = pktable; //$NON-NLS-1$
                    }
                    */
                    
                    Relation fkInfo = allRelations.get(fktable);
                    if (fkInfo == null) {
                        // make name of the relationship more meaningful
                        fkInfo = new Relation(StringUtil.plural(fktable).toLowerCase());
                        fkInfo.setForeignEntity(pkEntity);
                        allRelations.put(fktable, fkInfo);
                    }
                    fkInfo.getKeyColumns().put(seqNum, fkColumn);
                    fkInfo.getReferencedKeyColumns().put(seqNum, pkColumn);
                    
                    if (pkEntity.isPartOfPrimaryKey(pkColumn) && fkEntity.isPartOfPrimaryKey(fkColumn)) {
                        fkInfo.setCardinality(Cardinality.ONE_TO_ONE);
                    } else {
                        fkInfo.setNullable(fkEntity.getAttribute(fkColumn).isNullable());
                        fkInfo.setCardinality(Cardinality.ONE_TO_MANY);
                    }
                    fkInfo.setAnnotations(fkInfo.getCardinality().name(),fkColumn);
                }
            }
            entity.setRelations(new ArrayList<Relation>(allRelations.values()));
        }
    }
}