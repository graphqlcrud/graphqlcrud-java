package io.graphqlcrud;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import io.graphqlcrud.model.Entity;
import io.graphqlcrud.model.Relation;

public class SQLDirectives {

    public static GraphQLDirective sqlDirective(Entity entity) {
        return GraphQLDirective.newDirective().name("sql").argument(GraphQLArgument.newArgument().name("from").type(Scalars.GraphQLString).value(entity.getParent().getName() + "." + entity.getName())).build();
    }

    public static GraphQLDirective relationDirective(Relation relation, Entity entity) {
        return GraphQLDirective.newDirective().name("relation")
                .argument(GraphQLArgument.newArgument().name("kind").type(Scalars.GraphQLString).value(relation.getKind()))
                .argument(GraphQLArgument.newArgument().name("primaryField").type(Scalars.GraphQLString).value(relation.getPrimaryField()))
                .argument(GraphQLArgument.newArgument().name("foreignField").type(Scalars.GraphQLString).value(relation.getForeignField()))
                .argument(GraphQLArgument.newArgument().name("tablename").type(Scalars.GraphQLString).value(entity.getParent().getName() + "." + entity.getName()))
                .build();

    }

    public static GraphQLDirective typeDirective(Entity entity) {
        return GraphQLDirective.newDirective().name("type").argument(GraphQLArgument.newArgument().name("tablename").type(Scalars.GraphQLString).value(entity.getParent().getName() + "." + entity.getName())).build();
    }
}
