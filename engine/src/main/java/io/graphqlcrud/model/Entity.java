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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Entity {
    private String name;
    private Map<String, Attribute> attributes = new TreeMap<>();
    private List<Relation> relations = new ArrayList<Relation>();
    private List<String> pks = new ArrayList<>();
    
    public Entity(String name) {
        this.name = name;
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.put(attribute.getName(), attribute);
    }

    public String getName() {
        return this.name;
    }
    
    // TODO: this needs to built for GraphQLCRUD 
    public String getDescription() {
        return "Enitity " + name;
    }

    public List<Attribute> getAttributes() {
        return new ArrayList<Attribute>(attributes.values());
    }

    public Attribute getAttribute(String name) {
        return attributes.get(name);
    }
    
    public boolean isPartOfPrimaryKey(String name) {
        return this.pks.contains(name);
    }    
    
    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
    
    public List<String> getPrimaryKeys() {
        return pks;
    }

    public void setPrimaryKeys(List<String> pks) {
        this.pks = pks;
    }

    @Override
    public String toString() {
        return "Entity [name=" + name + ", attributes=" + attributes + ", relations="
                + relations + ", pks=" + pks + "]";
    }


}
