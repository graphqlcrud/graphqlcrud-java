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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import graphql.language.*;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONEntry;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jooq.impl.DSL.*;

/**
 * This is root SQL builder, it is assumed any special cases are added as extensions to this class by extending it
 */
public class SQLQueryBuilderVisitor implements QueryVisitor{
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLQueryBuilderVisitor.class);

    protected AtomicInteger inc = new AtomicInteger(0);
    protected SQLContext ctx;
    protected DSLContext create = null;

    private static class VisitorContext {
        String alias;
        SelectSelectStep<Record> selectClause;
        Map<String, org.jooq.Field<?>> selectedColumns = new LinkedHashMap<>();
        Map<String, Field> selectedFields = new LinkedHashMap<>();
        org.jooq.Condition condition;
        Boolean orCounter = false;
        Integer size = 0;
        Integer level = 0;
    }

    private Stack<VisitorContext> stack = new Stack<>();

    public SQLQueryBuilderVisitor(SQLContext ctx) {
        this.ctx = ctx;
        this.create = DSL.using(SQLDialect.valueOf(ctx.getDialect()));
    }

    @Override
    public void visitScalar(Field field, GraphQLFieldDefinition definition, GraphQLType type) {
        VisitorContext vctx = this.stack.peek();
        boolean found = false;
        for (String column: vctx.selectedColumns.keySet()) {
            if (column.equals(field.getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            vctx.selectedColumns.put(field.getName(), field(name(vctx.alias, field.getName())));
            vctx.selectedFields.put(field.getName(), field);
        }
    }

    static String fieldName(Field field) {
        if (field.getAlias() != null) {
            return field.getAlias();
        }
        return field.getName();
    }

    @Override
    public void startVisitObject(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type) {
        SQLDirective sqlDirective = SQLDirective.find(definition.getDirectives());
        VisitorContext vctx = this.stack.peek();
        String aliasLeft = vctx.alias;

        // add current object field as selected
        vctx.selectedFields.put(field.getName(), field);

        if (sqlDirective != null && sqlDirective.getPrimaryFields() != null) {
            String aliasRight = alias(this.inc.getAndIncrement());
            Table<Record> right = buildTable(definition, aliasRight);

            SelectSelectStep<Record> select = this.create.select();

            // add from
            select.from(right);

            // build where clause based on join
            Condition where = null;
            for (int key = 0; key < sqlDirective.getPrimaryFields().size(); key++) {
                Condition cond = field(name(aliasLeft, sqlDirective.getPrimaryFields().get(key)))
                        .eq(field(name(aliasRight, sqlDirective.getForeignFields().get(key))));
                if (key == 0) {
                    where = cond;
                } else {
                    where = where.and(cond);
                }
            }
            select.where(where);

            // next level deep as context
            vctx = new VisitorContext();
            vctx.alias = aliasRight;
            vctx.selectClause = select;

            this.stack.push(vctx);
        } else {
            throw new RuntimeException("@sql directive missing on " + field.getName());
        }
    }

    @Override
    public void endVisitObject(Field field, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        VisitorContext vctx = this.stack.pop();

        // add orderby
        //List<String> identityColumns = getIdentityColumns(type);
        //addOrderBy(vctx, identityColumns);

        // add where
        if (vctx.condition != null) {
            vctx.selectClause.where(vctx.condition);
        }

        // build the nested json object
        List<JSONEntry<?>> list = new ArrayList<>();
        for (Map.Entry<String, Field> entry: vctx.selectedFields.entrySet()) {
            list.add(jsonEntry(val(fieldName(entry.getValue()), String.class),
                    vctx.selectedColumns.get(entry.getKey())));
        }

        org.jooq.Field<?> json = vctx.selectClause.select(jsonbArrayAgg(jsonObject(list))).asField();

        // add the above as a field to parent query
        this.stack.peek().selectedColumns.put(field.getName(), json);
    }

    private void addOrderBy(VisitorContext vctx, List<String> identityColumns) {
        List<org.jooq.Field<?>> orderby = new ArrayList<>();
        identityColumns.stream().forEach(col -> {
            org.jooq.Field<?> f = field(name(vctx.alias, col));
            orderby.add(f);
        });
        vctx.selectClause.orderBy(orderby);
    }

    @Override
    public void startVisitRootObject(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        SQLDirective sqlDirective = SQLDirective.find(type.getDirectives());
        if (sqlDirective == null) {
            throw new RuntimeException("No SQL Directive found on field " + rootField.getName());
        }

        String alias = alias(this.inc.getAndIncrement());
        Table<Record> table = table(sqlDirective.getTableName()).as(alias);

        SelectSelectStep<Record> select = this.create.select();

        // add from
        select.from(table);

        // put the current table on stack
        VisitorContext vctx = new VisitorContext();
        vctx.alias = alias;
        vctx.selectClause = select;

        this.stack.push(vctx);
    }

    @Override
    public void endVisitRootObject(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        VisitorContext vctx = this.stack.peek();

        // add orderby
        List<String> identityColumns = getIdentityColumns(type);
        addOrderBy(vctx, identityColumns);

        // add where
        if (vctx.condition != null) {
            vctx.selectClause.where(vctx.condition);
        }

        List<org.jooq.Field<?>> projected = new ArrayList<>();
        vctx.selectedColumns.forEach((k,v) -> {
            projected.add(v.as(fieldName(vctx.selectedFields.get(k))));
        });
        vctx.selectClause.select(projected);
    }

    private String alias(int i) {
        return "g"+i;
    }

    public static List<String> getIdentityColumns(GraphQLObjectType type) {
        ArrayList<String> names = new ArrayList<>();
        for (GraphQLFieldDefinition fd:type.getFieldDefinitions()) {
            GraphQLType fieldType = fd.getType();
            if (fieldType instanceof GraphQLModifiedType) {
                fieldType = ((GraphQLModifiedType) fieldType).getWrappedType();
                if (GraphQLTypeUtil.isScalar(fieldType)) {
                    if (((GraphQLScalarType)fieldType).getName().equals("ID")) {
                        names.add(fd.getName());
                    }
                }
            }
        }
        return names;
    }

    private Table<Record> buildTable(GraphQLFieldDefinition definition, String alias) {
        SQLDirective parentDirective = null;
        if (definition.getType() instanceof GraphQLModifiedType) {
            GraphQLObjectType type = (GraphQLObjectType)((GraphQLModifiedType)definition.getType()).getWrappedType();
            parentDirective = SQLDirective.find(type.getDirectives());
        } else {
            GraphQLObjectType type = (GraphQLObjectType)definition.getType();
            parentDirective = SQLDirective.find(type.getDirectives());
        }

        // Build the main table
        Table<Record> table = table(parentDirective.getTableName()).as(alias);
        return table;
    }

    public String getSQL() {
        VisitorContext vctx = this.stack.peek();
        return vctx.selectClause.toString();
    }

    @Override
    public void visitArgument(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type, Argument arg) {
        VisitorContext vctx = this.stack.peek();

        String argName = arg.getName();
        Value<?> argValue = arg.getValue();

        if(argName.equals("page")) {
            //TODO : Walk through page results
        } else if(argName.equals("filter")) {
            LOGGER.debug("Walk through filter results");
            if(!argValue.getChildren().isEmpty()) {
                visitFilterInputs(argValue, vctx, null);
            }
            else
                throw new RuntimeException("Cannot fetch filter results on " + argValue);
        } else {
            org.jooq.Field<Object> left = field(name(vctx.alias, arg.getName()));
            Condition condition = visitValueType(argValue, left, "eq");
            visitCondition(null,condition, 0);
        }
    }

    public Condition visitStringValue(String value, org.jooq.Field left, String conditionName) {
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
            default:
                throw new RuntimeException("Unexpected value: " + conditionName);
        }
        return c;
    }

    public Condition visitBooleanValue(Boolean value, org.jooq.Field left, String conditionName) {
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

    public Condition visitIntValue(BigInteger value, org.jooq.Field left, String conditionName) {
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

    public Condition visitFloatValue(BigDecimal value, org.jooq.Field left, String conditionName) {
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


    public Condition visitArrayValue(List<Value> v, org.jooq.Field left, String conditionName) {
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
                    for (Value value : v) {
                        list.add(((StringValue) value).getValue());
                    }
                    c = left.in(list);
                } else if (v.get(0) instanceof FloatValue) {
                    List<BigDecimal> list = new ArrayList<>();
                    for (Value value : v) {
                        list.add(((FloatValue) value).getValue());
                    }
                    c = left.in(list);
                } else if (v.get(0) instanceof IntValue) {
                    List<BigInteger> list = new ArrayList<>();
                    for (Value value : v) {
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

    public Condition visitValueType(Value v, org.jooq.Field left, String conditionName) {
        Condition c = null;
        if (v instanceof StringValue) {
            c = visitStringValue(((StringValue) v).getValue(),left,conditionName);
        } else if (v instanceof BooleanValue) {
            c = visitBooleanValue(((BooleanValue) v).isValue(),left,conditionName);
        } else if (v instanceof IntValue) {
            c = visitIntValue(((IntValue) v).getValue(),left,conditionName);
        } else if (v instanceof FloatValue) {
            c = visitFloatValue(((FloatValue) v).getValue(),left,conditionName);
        } else if(v instanceof ArrayValue) {
            c = visitArrayValue(((ArrayValue) v).getValues(),left,conditionName);
        }
        return c;
    }


    public void visitFilterInputs(Value argumentValue, VisitorContext visitorContext, String conditionName) {
        org.jooq.Field<Object> left = null;
        for (ObjectField field : ((ObjectValue) argumentValue).getObjectFields()) {
            if(!field.getValue().getChildren().isEmpty()) {
                if(field.getName().equals("and")) {
                    conditionName = field.getName();
                    visitorContext.size = field.getValue().getChildren().size();
                } else if(field.getName().equals("or")) {
                    if(field.getValue().getChildren().size() > 1) {
                        visitorContext.level = 0;
                    }
                    conditionName = field.getName();
                    visitorContext.size = field.getValue().getChildren().size();
                } else {
                    left = field(name(visitorContext.alias, field.getName()));
                    org.jooq.Field<Object> finalLeft = left;
                    String finalConditionName = conditionName;
                    if( conditionName != null && conditionName.equals("or")) {
                        if (visitorContext.size > visitorContext.level) {
                            visitorContext.level++;
                        } else if(visitorContext.level.equals(visitorContext.size)) {
                            finalConditionName = null;
                        }
                    }
                    for(Object object : field.getValue().getChildren()) {
                        if(object instanceof ObjectField) {
                            Condition condition = visitValueType(((ObjectField) object).getValue(), finalLeft, ((ObjectField) object).getName());
                            visitCondition(finalConditionName, condition, visitorContext.size);
                        }
                    }
                }
                if(field.getValue() instanceof ObjectValue)
                    visitFilterInputs(field.getValue(), visitorContext, conditionName);
            }
        }
    }

    public void visitCondition(String conditionName, Condition conditionValue, int size) {
        VisitorContext visitorContext = this.stack.peek();

        if(visitorContext.condition == null) {
            visitorContext.condition = conditionValue;
            if(conditionName != null && conditionName.equals("or"))
                visitorContext.orCounter = true;
        }
        else {
            if(conditionName == null) {
                if(visitorContext.orCounter) {
                    visitOR(conditionValue);
                    visitorContext.orCounter = false;
                } else {
                    visitAND(conditionValue);
                }
            } else if(conditionName.equals("and")) {
                if(visitorContext.orCounter) {
                    visitOR(conditionValue);
                    visitorContext.orCounter = false;
                } else {
                    visitAND(conditionValue);
                }
            } else if (conditionName.equals("or")) {
                if(!visitorContext.orCounter) {
                    visitOR(conditionValue);
                    visitorContext.orCounter = true;
                }
                else if(size > 1 && visitorContext.orCounter) {
                    visitAND(conditionValue);
                }
                else {
                    visitOR(conditionValue);
                }
            }
        }
    }

    public void visitOR (Condition conditionValue) {
        VisitorContext visitorContext = this.stack.peek();
        visitorContext.condition = visitorContext.condition.or(conditionValue);
    }

    public void visitAND (Condition conditionValue) {
        VisitorContext visitorContext = this.stack.peek();
        visitorContext.condition = visitorContext.condition.and(conditionValue);
    }
}
