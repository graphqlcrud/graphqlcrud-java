package org.graphqlcrudjava.types;

import graphql.schema.GraphQLOutputType;

public interface TypeMap {
    GraphQLOutputType getAsGraphQLTypeString(int dataType);
}
