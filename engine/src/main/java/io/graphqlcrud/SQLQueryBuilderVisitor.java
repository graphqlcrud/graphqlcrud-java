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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.teiid.language.AndOr;
import org.teiid.language.ColumnReference;
import org.teiid.language.Comparison;
import org.teiid.language.Condition;
import org.teiid.language.DerivedColumn;
import org.teiid.language.Expression;
import org.teiid.language.Join;
import org.teiid.language.Literal;
import org.teiid.language.NamedTable;
import org.teiid.language.OrderBy;
import org.teiid.language.Select;
import org.teiid.language.SortSpecification;
import org.teiid.language.TableReference;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
/**
 * This is root SQL builder, it is assumed any special cases are added as extensions to this class by extending it
 */
public class SQLQueryBuilderVisitor implements QueryVisitor{

    protected AtomicInteger inc = new AtomicInteger(0);
    protected Select select = new Select(new ArrayList<DerivedColumn>(), false, new ArrayList<TableReference>(), null,
            null, null, new OrderBy(new ArrayList<SortSpecification>()));
    protected TableReference from;
    protected Condition condition = null;
    protected SQLContext ctx;
    private Stack<NamedTable> tableStack = new Stack<>();
    private Stack<List<String>> identityColumnsStack = new Stack<>();

    public SQLQueryBuilderVisitor(SQLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void visitScalar(Field field, GraphQLFieldDefinition definition, GraphQLType type) {
        NamedTable currentTable = this.tableStack.peek();
        boolean found = false;
        for (DerivedColumn col: this.select.getDerivedColumns()) {
            // when computed fields are supported this will be an issue like UPPER(foo)
            ColumnReference column = (ColumnReference)col.getExpression();
            if (column.getName().equals(field.getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.select.getDerivedColumns()
                    .add(new DerivedColumn(null, new ColumnReference(currentTable, field.getName(), null, null)));
        }
    }

    @Override
    public void startVisitObject(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type) {
        SQLDirective sqlDirective = SQLDirective.find(definition.getDirectives());
        NamedTable left = this.tableStack.peek();

        if (sqlDirective != null && sqlDirective.getPrimaryFields() != null) {
            NamedTable right = buildTable(definition, this.inc);

            // the criteria to check to make sure results under same parent
            this.ctx.addKeyColumns(definition.getName(), this.identityColumnsStack.peek());

            // Add default sorts for this table, so that we know the predictive order of the results
            List<String> sortNames = buildSortBy(type, right);

            // build Join clause
            Condition c = null;
            for (int key = 0; key < sqlDirective.getPrimaryFields().size(); key++) {
                Comparison comp = new Comparison(
                        new ColumnReference(left, sqlDirective.getPrimaryFields().get(key), null, null),
                        new ColumnReference(right, sqlDirective.getForeignFields().get(key), null, null),
                        Comparison.Operator.EQ);
                if (key == 0) {
                    c = comp;
                } else {
                    c = new AndOr(c, comp, AndOr.Operator.AND);
                }
            }
            this.from = new Join(this.from, right, Join.JoinType.LEFT_OUTER_JOIN, c);
            this.tableStack.push(right);
            this.identityColumnsStack.push(sortNames);
        }
    }

    @Override
    public void endVisitObject(Field rootFeild, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        this.tableStack.pop();
        this.identityColumnsStack.pop();
    }

    @Override
    public void startVisitRootObject(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        SQLDirective sqlDirective = SQLDirective.find(type.getDirectives());
        if (sqlDirective == null) {
            throw new RuntimeException("No SQL Directive found on field " + rootField.getName());
        }
        NamedTable table = new NamedTable(sqlDirective.getTableName(), alias(this.inc.getAndIncrement()), null);
        this.from = table;

        List<String> sortNames = buildSortBy(type, table);

        // put the current table on stack
        this.tableStack.push(table);
        this.identityColumnsStack.push(sortNames);
    }

    private List<String> buildSortBy(GraphQLObjectType type, NamedTable table) {
        // by default add identity columns as default sort columns
        List<String> sortNames = getIdentityColumns(type);
        sortNames.stream().forEach(name -> {
            ColumnReference col = new ColumnReference(table, name, null, null);
            this.select.getOrderBy().getSortSpecifications().add(new SortSpecification(SortSpecification.Ordering.ASC, col));

            // make sure above columns are part of the select
            this.select.getDerivedColumns()
                .add(new DerivedColumn(null, new ColumnReference(table, name, null, null)));
        });
        return sortNames;
    }

    @Override
    public void endVisitRootObject(Field rootFeild, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {
        this.tableStack.pop();
        this.identityColumnsStack.pop();
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

    private NamedTable buildTable(GraphQLFieldDefinition definition, AtomicInteger inc) {
        SQLDirective parentDirective = null;
        if (definition.getType() instanceof GraphQLModifiedType) {
            GraphQLObjectType type = (GraphQLObjectType)((GraphQLModifiedType)definition.getType()).getWrappedType();
            parentDirective = SQLDirective.find(type.getDirectives());
        } else {
            GraphQLObjectType type = (GraphQLObjectType)definition.getType();
            parentDirective = SQLDirective.find(type.getDirectives());
        }

        // Build the main table
        NamedTable table = new NamedTable(parentDirective.getTableName(), alias(inc.getAndIncrement()), null);
        return table;
    }

    public Select getSelect() {
        return this.select;
    }

    @Override
    public void visitArgument(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type, Argument arg) {
        NamedTable currentTable = this.tableStack.peek();
        Expression left = new ColumnReference(currentTable, arg.getName(), null, null);
        Expression right = null;
        Value<?> v = arg.getValue();
        if (v instanceof StringValue) {
            right  = new Literal(((StringValue) v).getValue(), String.class);
        } else if (v instanceof BooleanValue) {
            right  = new Literal(((BooleanValue) v).isValue(), Boolean.class);
        } else if (v instanceof IntValue) {
            right  = new Literal(((IntValue) v).getValue(), Integer.class);
        } else if (v instanceof FloatValue) {
            right  = new Literal(((FloatValue) v).getValue(), Float.class);
        }
        Comparison compare = new Comparison(left, right, Comparison.Operator.EQ);
        if (this.condition ==  null) {
            this.condition = compare;
        } else {
            this.condition = new AndOr(this.condition, compare, AndOr.Operator.AND);
        }
    }

    @Override
    public void onComplete() {
        this.select.getFrom().add(this.from);
        if (this.condition != null) {
            this.select.setWhere(this.condition);
        }
    }
}
