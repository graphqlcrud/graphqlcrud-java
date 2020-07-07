package org.graphqlcrudjava.model;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import java.util.List;
import java.util.Objects;

public class Attribute extends GraphQLFieldDefinition implements Comparable<Attribute> {

    private final String name;
    private final int position;
    private final boolean isPrimaryKey;
    private final boolean isNullable;
    private Relation foreignKey;
    private final GraphQLOutputType type;

    public Attribute(String name, String description, GraphQLOutputType type, DataFetcher<?> dataFetcher, List<GraphQLArgument> arguments, String deprecationReason, int position, boolean isPrimaryKey, boolean isNullable) {
        super(name, description, type, dataFetcher, arguments, deprecationReason);
        this.name = name;
        this.type = type;
        this.position = position;
        this.isPrimaryKey = isPrimaryKey;
        this.isNullable = isNullable;
    }


    public int getPosition() {
        return position;
    }

    //To be customized for adding comments
    @Override
    public String getDescription() {
        return getDescription();
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public Relation getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(Relation foreignKey) {
        this.foreignKey = foreignKey;
    }

    @Override
    public int compareTo(Attribute that) {
        return Integer.compare(this.position, that.position);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", position=" + position +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isNullable=" + isNullable +
                ", foreignKey=" + foreignKey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Attribute attribute = (Attribute) o;
        return position == attribute.position &&
                isPrimaryKey == attribute.isPrimaryKey &&
                isNullable == attribute.isNullable &&
                Objects.equals(name, attribute.name) &&
                Objects.equals(type, attribute.type) &&
                Objects.equals(foreignKey, attribute.foreignKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, position, type, isPrimaryKey, isNullable, foreignKey);
    }
}
