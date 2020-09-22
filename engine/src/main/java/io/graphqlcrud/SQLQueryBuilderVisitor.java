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
import static org.jooq.impl.DSL.jsonEntry;
import static org.jooq.impl.DSL.jsonObject;
import static org.jooq.impl.DSL.jsonbArrayAgg;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.val;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONEntry;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.Value;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import io.graphqlcrud.FilterScanner.Clause;

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
        AliasedTable table;
        Page page;
        List<org.jooq.Field<?>> orderby;
    }

    private static class Page {
        BigInteger limit;
        BigInteger offset;
    }

    private class AliasedTable {
        String name;
        String alias;
        public AliasedTable(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }
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
            AliasedTable right = buildTable(definition, aliasRight);

            SelectSelectStep<Record> select = this.create.select();

            // build where clause based on join
            Condition where = null;
            for (int key = 0; key < sqlDirective.getPrimaryFields().size(); key++) {
                Condition cond = field(name(aliasLeft, sqlDirective.getPrimaryFields().get(key)))
                        .eq(field(name(sqlDirective.getForeignFields().get(key))));
                if (key == 0) {
                    where = cond;
                } else {
                    where = where.and(cond);
                }
            }

            // next level deep as context
            vctx = new VisitorContext();
            vctx.table = right;
            vctx.alias = aliasRight;
            vctx.selectClause = select;
            vctx.condition = where;

            this.stack.push(vctx);
        } else {
            throw new RuntimeException("@sql directive missing on " + field.getName());
        }
    }

    @Override
    public void endVisitObject(Field field, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        VisitorContext vctx = this.stack.pop();

        SelectSelectStep<Record> select = this.create.select();
        select.from(vctx.table.name);

        // add orderby
        if (vctx.orderby == null) {
            List<String> identityColumns = getIdentityColumns(type);
            List<org.jooq.Field<?>> orderby = new ArrayList<>();
            identityColumns.stream().forEach(col -> {
                org.jooq.Field<?> f = field(name(col));
                orderby.add(f);
            });
            select.orderBy(orderby);
        } else {
            select.orderBy(vctx.orderby);
        }

        // has limit/offset
        if (vctx.page != null) {
            if (vctx.page.limit != null) {
                select.limit(vctx.page.limit.intValue());
            }
            if (vctx.page.offset != null) {
                select.offset(vctx.page.offset.intValue());
            }
        }

        // has where clause
        if (vctx.condition != null) {
            select.where(vctx.condition);
        }
        vctx.selectClause.from(table(select).as(vctx.table.alias));

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

    private List<org.jooq.Field<?>> buildOrderBy(String alias, List<String> identityColumns) {
        List<org.jooq.Field<?>> orderby = new ArrayList<>();
        identityColumns.stream().forEach(col -> {
            org.jooq.Field<?> f = field(name(alias, col));
            orderby.add(f);
        });
        return orderby;
    }

    @Override
    public void startVisitRootObject(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        VisitorContext vctx = new VisitorContext();
        SQLDirective sqlDirective = SQLDirective.find(type.getDirectives());
        if (sqlDirective == null) {
            throw new RuntimeException("No SQL Directive found on field " + rootField.getName());
        }

        String alias = alias(this.inc.getAndIncrement());
        AliasedTable table = new AliasedTable(sqlDirective.getTableName(), alias);

        SelectSelectStep<Record> select = this.create.select();

        // add from
        vctx.table = table;

        // put the current table on stack
        vctx.alias = alias;
        vctx.selectClause = select;

        this.stack.push(vctx);
    }

    @Override
    public void endVisitRootObject(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        VisitorContext vctx = this.stack.peek();

        // add table
        vctx.selectClause.from(table(vctx.table.name).as(vctx.table.alias));

        // add orderby
        if (vctx.orderby == null) {
            List<String> identityColumns = getIdentityColumns(type);
            List<org.jooq.Field<?>> orderby = buildOrderBy(vctx.alias, identityColumns);
            vctx.selectClause.orderBy(orderby);
        } else {
            vctx.selectClause.orderBy(vctx.orderby);
        }

        // add where
        if (vctx.condition != null) {
            vctx.selectClause.where(vctx.condition);
        }

        List<org.jooq.Field<?>> projected = new ArrayList<>();
        vctx.selectedColumns.forEach((k,v) -> {
            projected.add(v.as(fieldName(vctx.selectedFields.get(k))));
        });
        vctx.selectClause.select(projected);

        // add limit & offset
        if (vctx.page != null) {
            if (vctx.page.limit != null) {
                vctx.selectClause.limit(vctx.page.limit.intValue());
            }
            if (vctx.page.offset != null) {
                vctx.selectClause.offset(vctx.page.offset.intValue());
            }
        }
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

    private AliasedTable buildTable(GraphQLFieldDefinition definition, String alias) {
        SQLDirective parentDirective = null;
        if (definition.getType() instanceof GraphQLModifiedType) {
            GraphQLObjectType type = (GraphQLObjectType)((GraphQLModifiedType)definition.getType()).getWrappedType();
            parentDirective = SQLDirective.find(type.getDirectives());
        } else {
            GraphQLObjectType type = (GraphQLObjectType)definition.getType();
            parentDirective = SQLDirective.find(type.getDirectives());
        }

        // Build the main table
        AliasedTable table = new AliasedTable(parentDirective.getTableName(), alias);
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

        SQLFilterBuilder filterBuilder = new SQLFilterBuilder(vctx.alias);

        if(argName.equals("page")) {
            Page p = new Page();
            ObjectValue v = (ObjectValue)argValue;
            for (ObjectField of: v.getObjectFields()) {
                if (of.getName().equals("limit")) {
                    p.limit = ((IntValue)of.getValue()).getValue();
                } else if (of.getName().equals("offset")) {
                    p.offset = ((IntValue)of.getValue()).getValue();
                }
            }
            vctx.page = p;
        } else if (argName.equals("orderBy")) {
            // handle orderBy
        } else if(argName.equals("filter")) {
            LOGGER.debug("Walk through filter results");
            FilterScanner<Condition> filterScanner = new FilterScanner<>(filterBuilder);
            Condition condition = filterScanner.scan((ObjectValue)argValue, Clause.and).condition;
            if (vctx.condition == null) {
                vctx.condition = condition;
            } else {
                vctx.condition = vctx.condition.and(condition);
            }
        } else {
            Condition condition = filterBuilder.buildCondition(argName, "eq", argValue);
            if (vctx.condition == null) {
                vctx.condition = condition;
            } else {
                vctx.condition = filterBuilder.and(vctx.condition, condition);
            }
        }
    }
}
