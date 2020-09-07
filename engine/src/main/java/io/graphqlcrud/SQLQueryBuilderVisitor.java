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

    private static class VistorContext {
        String alias;
        SelectSelectStep<Record> selectClause;
        Map<String, org.jooq.Field<?>> selectedColumns = new LinkedHashMap<>();
        Map<String, Field> selectedFields = new LinkedHashMap<>();
        org.jooq.Condition condition;
    }

    private Stack<VistorContext> stack = new Stack<>();

    public SQLQueryBuilderVisitor(SQLContext ctx) {
        this.ctx = ctx;
        this.create = DSL.using(SQLDialect.valueOf(ctx.getDialect()));
    }

    @Override
    public void visitScalar(Field field, GraphQLFieldDefinition definition, GraphQLType type) {
        VistorContext vctx = this.stack.peek();
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
        VistorContext vctx = this.stack.peek();
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
            vctx = new VistorContext();
            vctx.alias = aliasRight;
            vctx.selectClause = select;

            this.stack.push(vctx);
        } else {
            throw new RuntimeException("@sql directive missing on " + field.getName());
        }
    }

    @Override
    public void endVisitObject(Field field, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        VistorContext vctx = this.stack.pop();

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

    private void addOrderBy(VistorContext vctx, List<String> identityColumns) {
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
        VistorContext vctx = new VistorContext();
        vctx.alias = alias;
        vctx.selectClause = select;

        this.stack.push(vctx);
    }

    @Override
    public void endVisitRootObject(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        VistorContext vctx = this.stack.peek();

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
        VistorContext vctx = this.stack.peek();
        return vctx.selectClause.toString();
    }

    @Override
    public void visitArgument(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type, Argument arg) {
        VistorContext vctx = this.stack.peek();

        String argName = arg.getName();
        Value<?> argValue = arg.getValue();

        if(argName.equals("page")) {
            //TODO : Walk through page results
        } else if(argName.equals("filter")) {
            LOGGER.debug("Walk through filter results");
            if(!argValue.getChildren().isEmpty())
                visitFilterInputs(argValue, vctx, null);
            else
                throw new RuntimeException("Cannot fetch filter results on " + argValue);
        } else {
            org.jooq.Field<Object> left = field(name(vctx.alias, arg.getName()));
            Condition condition = visitValueType(argValue, left, "eq");
            visitCondition(null,condition);
        }
    }

    public Condition visitStringValue(Value v, org.jooq.Field left, String conditionName) {
        Condition c = null;
        if(conditionName.equals("eq"))
            c = left.eq(((StringValue)v).getValue());
        else if(conditionName.equals("ne"))
            c = left.ne(((StringValue) v).getValue());
        else if(conditionName.equals("lt"))
            c = left.lt(((StringValue) v).getValue());
        else if(conditionName.equals("le"))
            c = left.le(((StringValue) v).getValue());
        else if(conditionName.equals("gt"))
            c = left.gt(((StringValue) v).getValue());
        else if(conditionName.equals("ge"))
            c = left.in(((StringValue) v).getValue());
        else if(conditionName.equals("contains"))
            c = left.contains(((StringValue) v).getValue());
        else if(conditionName.equals("startsWith"))
            c = left.startsWith(((StringValue) v).getValue());
        else if(conditionName.equals("endsWith"))
            c = left.endsWith(((StringValue) v).getValue());
        return c;
    }

    public Condition visitBooleanValue(Value v, org.jooq.Field left, String conditionName) {
        Condition c = null;
        if(conditionName.equals("eq"))
            c = left.eq(((BooleanValue)v).isValue());
        else if(conditionName.equals("ne"))
            c = left.ne(((BooleanValue) v).isValue());
        return c;
    }

    public Condition visitIntValue(Value v, org.jooq.Field left, String conditionName) {
        Condition c = null;
        if(conditionName.equals("eq"))
            c = left.eq(((IntValue)v).getValue());
        else if(conditionName.equals("ne"))
            c = left.ne(((IntValue) v).getValue());
        else if(conditionName.equals("lt"))
            c = left.lt(((IntValue) v).getValue());
        else if(conditionName.equals("le"))
            c = left.le(((IntValue) v).getValue());
        else if(conditionName.equals("gt"))
            c = left.gt(((IntValue) v).getValue());
        else if(conditionName.equals("ge"))
            c = left.ge(((IntValue) v).getValue());
        return c;
    }

    public Condition visitFloatValue(Value v, org.jooq.Field left, String conditionName) {
        Condition c = null;
        if(conditionName.equals("eq"))
            c = left.eq(((FloatValue)v).getValue());
        else if(conditionName.equals("ne"))
            c = left.ne(((FloatValue) v).getValue());
        else if(conditionName.equals("lt"))
            c = left.lt(((FloatValue) v).getValue());
        else if(conditionName.equals("le"))
            c = left.le(((FloatValue) v).getValue());
        else if(conditionName.equals("gt"))
            c = left.gt(((FloatValue) v).getValue());
        else if(conditionName.equals("ge"))
            c = left.ge(((FloatValue) v).getValue());
        return c;
    }

    public Condition visitValueType(Value v, org.jooq.Field left, String conditionName) {
        Condition c = null;
        if (v instanceof StringValue) {
            c = visitStringValue(v,left,conditionName);
        } else if (v instanceof BooleanValue) {
            c = visitBooleanValue(v,left,conditionName);
        } else if (v instanceof IntValue) {
            c = visitIntValue(v,left,conditionName);
        } else if (v instanceof FloatValue) {
            c = visitFloatValue(v,left,conditionName);
        }
        return c;
    }


    public void visitFilterInputs(Value argumentValue, VistorContext vistorContext, String conditionName) {
        org.jooq.Field<Object> left = null;
        for (ObjectField field : ((ObjectValue) argumentValue).getObjectFields()) {
            if(!field.getValue().getChildren().isEmpty()) {
                if(field.getName().equals("and")) {
                    conditionName = field.getName();
                } else if(field.getName().equals("or")) {
                    conditionName = field.getName();
                } else {
                    left = field(name(vistorContext.alias, field.getName()));
                    org.jooq.Field<Object> finalLeft = left;
                    String finalConditionName = conditionName;
                    field.getValue().getChildren().forEach(child -> {
                        Condition condition = visitValueType(((ObjectField) child).getValue(), finalLeft, ((ObjectField) child).getName());
                        visitCondition(finalConditionName, condition);
                    });
                }
                visitFilterInputs(field.getValue(), vistorContext, conditionName);
            }
        }
    }

    public void visitCondition(String conditionName, Condition conditionValue) {
        VistorContext vistorContext = this.stack.peek();
        if(vistorContext.condition == null) {
            vistorContext.condition = conditionValue;
        }
        else {
            if(conditionName.equals("and")) {
                    vistorContext.condition = vistorContext.condition.and(conditionValue);
            } else if (conditionName.equals("or")) {
                vistorContext.condition = vistorContext.condition.or(conditionValue);
            }
        }
    }
}
