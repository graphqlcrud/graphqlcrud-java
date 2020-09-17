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

import graphql.Scalars;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLTypeReference;

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
                .field(GraphQLInputObjectField.newInputObjectField().name("endsWith").type(Scalars.GraphQLString))
                .field(GraphQLInputObjectField.newInputObjectField().name("matchesPattern").type(Scalars.GraphQLString));
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
                .field(GraphQLInputObjectField.newInputObjectField().name("in").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLFloat))))
                .field(GraphQLInputObjectField.newInputObjectField().name("between").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLFloat))));
    }

    public static GraphQLInputObjectType.Builder intInputBuilder() {
        return GraphQLInputObjectType.newInputObject().name("IntInput")
                .field(GraphQLInputObjectField.newInputObjectField().name("ne").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("eq").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("le").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("lt").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("ge").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("gt").type(Scalars.GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField().name("in").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLInt))))
                .field(GraphQLInputObjectField.newInputObjectField().name("between").type(GraphQLList.list(GraphQLNonNull.nonNull(Scalars.GraphQLInt))));
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
