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

import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLOutputType;

import java.sql.Types;

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
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                typeString = Scalars.GraphQLString;
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
                typeString = ExtendedScalars.Object;
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                typeString = ExtendedScalars.Object;
                break;
            default:
                typeString = Scalars.GraphQLString;
                break;
        }
        return typeString;
    }
}
