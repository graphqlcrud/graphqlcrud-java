package org.graphqlcrudjava.types;

import java.sql.Types;

public class TypeMapImpl implements TypeMap {

    @Override
    public String getAsGraphQLTypeString(int dataType) {
        String typeString;
        switch (dataType) {
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
                typeString = "Int";
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                typeString = "String";
                break;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
            case Types.DECIMAL:
                typeString = "Float";
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIME:
                typeString = "Date";
                break;
            case Types.BIT:
                typeString = "Boolean";
                break;
            case Types.OTHER:
                typeString = "Object";
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                typeString = "Object";
                break;
            default:
                typeString = "Object";
                break;
        }
        return typeString;
    }
}
