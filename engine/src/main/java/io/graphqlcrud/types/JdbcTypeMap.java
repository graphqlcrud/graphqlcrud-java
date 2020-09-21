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
package io.graphqlcrud.types;

import java.sql.Types;

import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

public class JdbcTypeMap implements TypeMap {

    @Override
    public GraphQLOutputType getAsGraphQLTypeString(int dataType) {
        GraphQLOutputType typeString;
        switch (dataType) {
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.SMALLINT:
                typeString = Scalars.GraphQLInt;
                break;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
            case Types.DECIMAL:
                typeString = Scalars.GraphQLFloat;
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIME:
                typeString = ExtendedScalars.DateTime;
                break;
            case Types.BIT:
                typeString = Scalars.GraphQLBoolean;
                break;
            case Types.OTHER:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                typeString = ExtendedScalars.Object;
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            default:
                typeString = Scalars.GraphQLString;
                break;
        }
        return typeString;
    }

    @Override
    public GraphQLInputType getAsGraphQLTypeForInput(int dataType, String type) {
        GraphQLInputType typeString = null;
        switch (dataType) {
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.SMALLINT:
                if(type.equals("mutation")) {
                    typeString = Scalars.GraphQLInt;
                } else if(type.equals("filter")) {
                    typeString = GraphQLTypeReference.typeRef("IntInput");
                }
                break;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
            case Types.DECIMAL:
                if(type.equals("mutation")) {
                    typeString = Scalars.GraphQLFloat;
                } else if(type.equals("filter")) {
                    typeString = GraphQLTypeReference.typeRef("FloatInput");
                }
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIME:
                typeString = ExtendedScalars.DateTime;
                break;
            case Types.BIT:
                if(type.equals("mutation")) {
                    typeString = Scalars.GraphQLBoolean;
                } else if(type.equals("filter")) {
                    typeString = GraphQLTypeReference.typeRef("BooleanInput");
                }
                break;
            case Types.OTHER:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                typeString = ExtendedScalars.Object;
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            default:
                if(type.equals("mutation")) {
                    typeString = Scalars.GraphQLString;
                } else if(type.equals("filter")) {
                    typeString = GraphQLTypeReference.typeRef("StringInput");
                }
                break;
        }
        return typeString;
    }
}
