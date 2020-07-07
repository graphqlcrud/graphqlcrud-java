package org.graphqlcrudjava.types;

import graphql.Scalars;
import graphql.schema.GraphQLOutputType;

import java.sql.Types;

public class TypeMapImpl implements TypeMap {

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
//            case Types.DATE:
//            case Types.TIMESTAMP:
//            case Types.TIME:
//                typeString = "Date";
//                break;
            case Types.BIT:
                typeString = Scalars.GraphQLBoolean;
                break;
//            case Types.OTHER:
//                typeString = "Object";
//                break;
//            case Types.BINARY:
//            case Types.VARBINARY:
//            case Types.LONGVARBINARY:
//                typeString = "Object";
//                break;
            default:
                typeString = Scalars.GraphQLString;
                break;
        }
        return typeString;
    }
}
