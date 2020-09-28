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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;

public class SQLFilterBuilder implements FilterBuilder<Condition> {
    private String alias;

    public SQLFilterBuilder(String tableAlias) {
        this.alias = tableAlias;
    }

    @Override
    public Condition buildCondition(String left, String operation, Value<?> v) {
        Condition c = null;
        if (v instanceof StringValue) {
            c = visitStringValue(((StringValue) v).getValue(),left, operation);
        } else if (v instanceof BooleanValue) {
            c = visitBooleanValue(((BooleanValue) v).isValue(),left, operation);
        } else if (v instanceof IntValue) {
            c = visitIntValue(((IntValue) v).getValue(),left, operation);
        } else if (v instanceof FloatValue) {
            c = visitFloatValue(((FloatValue) v).getValue(),left,  operation);
        } else if(v instanceof ArrayValue) {
            c = visitArrayValue(((ArrayValue) v).getValues(),left,operation);
        }
        return c;
    }

    @Override
    public Condition and(Condition left, Condition right) {
        return left.and(right);
    }

    @Override
    public Condition or(Condition left, Condition right) {
        return left.or(right);
    }

    @Override
    public Condition not(Condition left) {
        return left.not();
    }

    Condition visitStringValue(String value, String fieldName, String conditionName) {
        org.jooq.Field<Object> left = this.alias != null ? field(name(this.alias, fieldName)) : field(fieldName);
        Condition c = null;
        switch (conditionName) {
            case "eq":
                c = left.eq(value);
                break;
            case "ne":
                c = left.ne(value);
                break;
            case "lt":
                c = left.lt(value);
                break;
            case "le":
                c = left.le(value);
                break;
            case "gt":
                c = left.gt(value);
                break;
            case "ge":
                c = left.in(value);
                break;
            case "contains":
                c = left.contains(value);
                break;
            case "startsWith":
                c = left.startsWith(value);
                break;
            case "endsWith":
                c = left.endsWith(value);
                break;
            case "matchesPattern":
                c = left.likeRegex(value);
                break;
            default:
                throw new RuntimeException("Unexpected value: " + conditionName);
        }
        return c;
    }

    Condition visitBooleanValue(Boolean value, String fieldName, String conditionName) {
        org.jooq.Field<Object> left = this.alias != null ? field(name(this.alias, fieldName)) : field(fieldName);
        Condition c = null;
        switch (conditionName) {
            case "eq":
                c = left.eq(value);
                break;
            case "ne":
                c = left.ne(value);
                break;
            default:
                throw new RuntimeException("Unexpected value: " + conditionName);
        }
        return c;
    }

    Condition visitIntValue(BigInteger value, String fieldName, String conditionName) {
        org.jooq.Field<Object> left = this.alias != null ? field(name(this.alias, fieldName)) : field(fieldName);
        Condition c = null;
        switch (conditionName) {
            case "eq":
                c = left.eq(value);
                break;
            case "ne":
                c = left.ne(value);
                break;
            case "lt":
                c = left.lt(value);
                break;
            case "le":
                c = left.le(value);
                break;
            case "gt":
                c = left.gt(value);
                break;
            case "ge":
                c = left.ge(value);
                break;
            default:
                throw new RuntimeException("Unexpected value: " + conditionName);
        }
        return c;
    }

    Condition visitFloatValue(BigDecimal value, String fieldName, String conditionName) {
        org.jooq.Field<Object> left = this.alias != null ? field(name(this.alias, fieldName)) : field(fieldName);
        Condition c = null;
        switch (conditionName) {
            case "eq":
                c = left.eq(value);
                break;
            case "ne":
                c = left.ne(value);
                break;
            case "lt":
                c = left.lt(value);
                break;
            case "le":
                c = left.le(value);
                break;
            case "gt":
                c = left.gt(value);
                break;
            case "ge":
                c = left.ge(value);
                break;
            default:
                throw new RuntimeException("Unexpected value: " + conditionName);
        }
        return c;
    }


    Condition visitArrayValue(List<Value> v, String fieldName, String conditionName) {
        org.jooq.Field<Object> left = this.alias != null ? field(name(this.alias, fieldName)) : field(fieldName);
        Condition c = null;
        switch (conditionName) {
            case "between":
                if (v.get(0) instanceof FloatValue) {
                    c = left.between(((FloatValue) v.get(0)).getValue(), ((FloatValue) v.get(1)).getValue());
                } else if (v.get(0) instanceof IntValue) {
                    c = left.between(((IntValue) v.get(0)).getValue(), ((IntValue) v.get(1)).getValue());
                }
                break;
            case "in":
                if (v.get(0) instanceof StringValue) {
                    List<String> list = new ArrayList<>();
                    for (Value<?> value : v) {
                        list.add(((StringValue) value).getValue());
                    }
                    c = left.in(list);
                } else if (v.get(0) instanceof FloatValue) {
                    List<BigDecimal> list = new ArrayList<>();
                    for (Value<?> value : v) {
                        list.add(((FloatValue) value).getValue());
                    }
                    c = left.in(list);
                } else if (v.get(0) instanceof IntValue) {
                    List<BigInteger> list = new ArrayList<>();
                    for (Value<?> value : v) {
                        list.add(((IntValue) value).getValue());
                    }
                    c = left.in(list);
                }
                break;
            default:
                throw new RuntimeException("Unexpected value: " + conditionName);
        }
        return c;
    }

}
