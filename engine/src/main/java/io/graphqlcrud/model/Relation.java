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
package io.graphqlcrud.model;

import java.util.TreeMap;

public class Relation {
    private String name;
    private Entity foreignEntity;
    private TreeMap<Short, String> keyColumns = new TreeMap<Short, String>();
    private TreeMap<Short, String> referencedKeyColumns = new TreeMap<Short, String>();
    private Cardinality cardinality;
    private boolean nullable;

    public Relation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Entity getForeignEntity() {
        return foreignEntity;
    }
    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setForeignEntity(Entity foreignEntity) {
        this.foreignEntity = foreignEntity;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }
    
    public TreeMap<Short, String> getKeyColumns() {
        return keyColumns;
    }
    
    public void setKeyColumns(TreeMap<Short, String> keyColumns) {
        this.keyColumns = keyColumns;
    }
    
    public TreeMap<Short, String> getReferencedKeyColumns() {
        return referencedKeyColumns;
    }
    
    public void setReferencedKeyColumns(TreeMap<Short, String> referencedKeyColumns) {
        this.referencedKeyColumns = referencedKeyColumns;
    }      
    
    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
    
    @Override
    public String toString() {
        return "Relation{" +
                "foreignEntity=" + (foreignEntity != null ? foreignEntity.getName() : "null") +
                ", keyColumns=" + getKeyColumns() +
                ", referencedKeyColumns=" + getReferencedKeyColumns() +
                ", cardinality=" + (cardinality != null ? cardinality.toString() : "null") +
                '}';
    }  
}
