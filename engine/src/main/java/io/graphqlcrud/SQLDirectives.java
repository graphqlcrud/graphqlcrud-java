package io.graphqlcrud;

import graphql.Scalars;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;

public class SQLDirectives {


    public static GraphQLDirective directive(String tableName, String kind, String primaryField, String foreignField) {
        return GraphQLDirective.newDirective()
                .name("sql")
                .argument(GraphQLArgument.newArgument().name("kind").type(Scalars.GraphQLString).value(kind))
                .argument(GraphQLArgument.newArgument().name("primaryField").type(Scalars.GraphQLString).value(primaryField))
                .argument(GraphQLArgument.newArgument().name("foreignField").type(Scalars.GraphQLString).value(foreignField))
                .argument(GraphQLArgument.newArgument().name("tablename").type(Scalars.GraphQLString).value(tableName))
                .validLocations(Introspection.DirectiveLocation.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION)
                .build();
    }

}
