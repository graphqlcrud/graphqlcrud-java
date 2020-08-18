package io.graphqlcrud;

import graphql.Scalars;
import graphql.schema.*;

public class Filters {

    public static GraphQLInputObjectType.Builder pageInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("PageRequest")
                .field(GraphQLInputObjectField.newInputObjectField().name("limit").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("offset").type(Scalars.GraphQLInt));
    }

    public static GraphQLInputObjectType.Builder stringInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("StringInput")
                .field(GraphQLInputObjectField.newInputObjectField().name("ne").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("eq").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("le").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("lt").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("ge").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("gt").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("in").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLString))))
                .field(GraphQLInputObjectField.newInputObjectField().name("contains").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("startsWith").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("endsWith").type(Scalars.GraphQLString));

    }

    public static GraphQLInputObjectType.Builder idInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("IDInput")
                .field(GraphQLInputObjectField.newInputObjectField().name("ne").type(Scalars.GraphQLID))
                .field(GraphQLInputObjectField.newInputObjectField().name("eq").type(Scalars.GraphQLID))
                .field(GraphQLInputObjectField.newInputObjectField().name("in").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLID))));

    }

    public static GraphQLInputObjectType.Builder booleanInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("BooleanInput")
                .field(GraphQLInputObjectField.newInputObjectField().name("ne").type(Scalars.GraphQLBoolean))
                .field(GraphQLInputObjectField.newInputObjectField().name("eq").type(Scalars.GraphQLBoolean));
    }

    public static GraphQLInputObjectType.Builder floatInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("FloatInput")
                .field(GraphQLInputObjectField.newInputObjectField().name("ne").type(Scalars.GraphQLFloat))
                .field(GraphQLInputObjectField.newInputObjectField().name("eq").type(Scalars.GraphQLFloat))
                .field(GraphQLInputObjectField.newInputObjectField().name("le").type(Scalars.GraphQLFloat))
                .field(GraphQLInputObjectField.newInputObjectField().name("lt").type(Scalars.GraphQLFloat))
                .field(GraphQLInputObjectField.newInputObjectField().name("ge").type(Scalars.GraphQLFloat))
                .field(GraphQLInputObjectField.newInputObjectField().name("gt").type(Scalars.GraphQLFloat))
                .field(GraphQLInputObjectField.newInputObjectField().name("in").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLFloat))));
    }

    public static GraphQLInputObjectType.Builder intInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("IntInput")
                .field(GraphQLInputObjectField.newInputObjectField().name("ne").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("eq").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("le").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("lt").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("ge").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("gt").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("in").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLInt))));
    }

    public static GraphQLInputObjectType.Builder orderByInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("OrderByInput")
                .field(GraphQLInputObjectField.newInputObjectField().name("field").type(GraphQLNonNull.nonNull(Scalars.GraphQLString)))
                .field(GraphQLInputObjectField.newInputObjectField().name("order").type(GraphQLTypeReference.typeRef("SortDirectionEnum")).defaultValue("ASC"));
    }

    public static GraphQLEnumType.Builder sortDirectionEnumBuilder() {
        return GraphQLEnumType.newEnum().name("SortDirectionEnum")
                .value("ASC")
                .value("DESC");
    }
}
