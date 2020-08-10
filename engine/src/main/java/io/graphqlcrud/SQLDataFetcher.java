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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.language.AndOr;
import org.teiid.language.ColumnReference;
import org.teiid.language.Comparison;
import org.teiid.language.Condition;
import org.teiid.language.DerivedColumn;
import org.teiid.language.Expression;
import org.teiid.language.Join;
import org.teiid.language.Literal;
import org.teiid.language.NamedTable;
import org.teiid.language.Select;
import org.teiid.language.TableReference;
import org.teiid.language.visitor.SQLStringVisitor;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Selection;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;

// This must be thread safe, as it will be called by multiple threads at same time
// This is very simple naively written class, will require more structure here
public class SQLDataFetcher implements DataFetcher<ResultSetList>{
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLDataFetcher.class);

    @Override
    public ResultSetList get(DataFetchingEnvironment environment) throws Exception {
        SQLContext ctx = environment.getContext();

        String sql = buildSQL(environment);
        ctx.setSQL(sql);
        LOGGER.info("SQL Executed:" + sql);

        ResultSet rs = null;
        Connection c = ctx.getConnection();
        CallableStatement stmt = c.prepareCall(sql);
        boolean hasResults = stmt.execute();
        if (hasResults) {
            rs = stmt.getResultSet();
        }
        ctx.setResultSet(rs);
        return new ResultSetList(rs);
    }

    private String alias(int i) {
        return "g"+i;
    }

    private String buildSQL(DataFetchingEnvironment environment) {
        AtomicInteger inc = new AtomicInteger(0);
        Select select = new Select(new ArrayList<DerivedColumn>(), false, new ArrayList<TableReference>(), null, null,
                null, null);

        // the parent query like "customers" definition
        Field rootFeild = environment.getField();
        GraphQLFieldDefinition rootDefinition = environment.getFieldDefinition();
        NamedTable table = buildTable(rootDefinition, inc);

        // add select
        TableReference from = buildSelect(environment, select, table, rootFeild, rootDefinition, null, inc);

        // add from
        select.getFrom().add(from);

        // condition
        select.setWhere(buildWhere(table, environment.getField()));
        return SQLStringVisitor.getSQLString(select);
    }

    private TableReference buildSelect(DataFetchingEnvironment environment, Select select, TableReference left,
            Field rootFeild, GraphQLFieldDefinition rootFieldDefinition, String fqn, AtomicInteger inc) {

        NamedTable table = null;
        if (left instanceof NamedTable) {
            table = (NamedTable)left;
        } else if (left instanceof Join) {
            table = (NamedTable)((Join)left).getRightItem();
        }

        // SQL directive on the relation
        SQLDirective rootSqlDirective = SQLDirective.find(rootFieldDefinition.getDirectives());
        GraphQLType rootType = rootFieldDefinition.getType();
        if (rootType instanceof GraphQLModifiedType) {
            rootType = ((GraphQLModifiedType) rootType).getWrappedType();
        }

        if (GraphQLTypeUtil.isScalar(rootType)) {
            select.getDerivedColumns().add(new DerivedColumn(null, new ColumnReference(table, rootFeild.getName(), null, null)));
        } else if (rootType instanceof GraphQLObjectType) {
            SQLDirective sqlDirective = SQLDirective.find(((GraphQLObjectType)rootType).getDirectives());
            if (rootSqlDirective != null && rootSqlDirective.getPrimaryFields() != null) {
                NamedTable right = buildTable(rootFieldDefinition, inc);
                Condition c = null;
                for (int key = 0; key < rootSqlDirective.getPrimaryFields().size(); key++) {
                    Comparison comp = new Comparison(
                            new ColumnReference(table, rootSqlDirective.getPrimaryFields().get(key), null, null),
                            new ColumnReference(right, rootSqlDirective.getForeignFields().get(key), null, null),
                            Comparison.Operator.EQ);
                    if (key == 0) {
                        c = comp;
                    } else {
                        c = new AndOr(c, comp, AndOr.Operator.AND);
                    }
                }
                Join join = new Join(left, right, Join.JoinType.LEFT_OUTER_JOIN, c);
                left = join;
            }

            // since this is object type loop through and selected fields
            List<Selection> fields = rootFeild.getSelectionSet().getSelections();
            for (int i = 0; i < fields.size(); i++) {
                Field f = (Field)fields.get(i);
                String fieldName = fqn == null ? f.getName() : fqn+"/"+f.getName();
                SelectedField field = environment.getSelectionSet().getField(fieldName);
                GraphQLFieldDefinition definition = field.getFieldDefinition();
                left =  buildSelect(environment, select, left, f, definition, fieldName, inc);
            }
        }
        return left;
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

    private Condition buildWhere(NamedTable table,  Field f) {
        Condition c = null;
        List<Argument> args = f.getArguments();
        if (args != null && !args.isEmpty()) {
            for (int i = 0; i < args.size(); i++) {
                Argument arg = args.get(i);
                // TODO: Need to handle all other types of values
                // TODO: May be this can be place holders like "?" then values in prepared statement
                Expression left = new ColumnReference(table, arg.getName(), null, null);
                Expression right = null;
                Value v = arg.getValue();
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
                if (c ==  null) {
                    c = compare;
                } else {
                    c = new AndOr(c, compare, AndOr.Operator.AND);
                }
            }
        }
        return c;
    }
}
