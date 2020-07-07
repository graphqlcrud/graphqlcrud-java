package org.graphqlcrudjava.model;


import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLObjectType;

import java.util.*;

public class Entity extends GraphQLObjectType {
    private final String name;
    private final Set<Attribute> attributes;

    public Entity(String name, String description, List<GraphQLFieldDefinition> fieldDefinitions, List<GraphQLNamedOutputType> interfaces) {
        super(name, description, fieldDefinitions, interfaces);
        this.name = name;
        this.attributes = new TreeSet<>();
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    public String getName() {
        return name;
    }

    public Set<Attribute> getAttributes() {
        return Collections.unmodifiableSet(attributes);
    }

    public int maxPosition() {
        return attributes.stream()
                .max(Comparator.comparingInt(Attribute::getPosition))
                .orElseThrow(() -> new IllegalStateException("Null position found"))
                .getPosition()+1;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
