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

import graphql.language.*;
import graphql.language.Field;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.util.*;

import static org.jooq.impl.DSL.*;

public class SQLMutationQueryBuilderVisitor implements QueryVisitor {

    protected SQLContext ctx;
    protected DSLContext create = null;

    public SQLMutationQueryBuilderVisitor(SQLContext ctx) {
        this.ctx = ctx;
        this.create = DSL.using(SQLDialect.valueOf(ctx.getDialect()));
    }

    private static class VisitorContext {
        String mutationType;
        InsertValuesStepN<Record> insertClause;
        UpdateSetFirstStep<Record> updateClause;
        DeleteUsingStep<Record> deleteClause;
        org.jooq.Condition condition;
        Collection<org.jooq.Field<?>> selectedFields = new ArrayList<>();
        Map<String, Object> selectedColumns = new LinkedHashMap<>();
    }

    private Stack<SQLMutationQueryBuilderVisitor.VisitorContext> stack = new Stack<>();

    @Override
    public void startVisitRootObject(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type) {
        VisitorContext visitorContext = new VisitorContext();

        visitorContext.mutationType = field.getName() != null ? field.getName() : field.getAlias();

        List<Selection> fields = field.getSelectionSet().getSelections();
        for(Selection selection : fields) {
            Field f = (Field) selection;
            if(!f.getName().contains("create") || !f.getName().contains("update") || !f.getName().contains("delete")) {
                visitorContext.selectedFields.add(field(f.getName()));
            }
        }
        this.stack.push(visitorContext);
    }

    @Override
    public void endVisitRootObject(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type) {
        VisitorContext visitorContext = this.stack.peek();
        String mutationName = field.getName();
        Table<Record> tableName = table(type.getName());

        if(mutationName.contains("create")) {
            Collection<org.jooq.Field<?>> fields = new ArrayList<>();
            visitorContext.selectedColumns.forEach((key, value) -> fields.add(field(name(key))));

            //build create clause
            visitorContext.insertClause = this.create.insertInto(tableName, fields);

            //insert values
            visitorContext.insertClause.values(visitorContext.selectedColumns.values());
        } else if (mutationName.contains("update")) {
            RowN columnValue = row(visitorContext.selectedColumns.values());
            RowN columnName = row(visitorContext.selectedColumns.keySet());

            //build the update clause
            visitorContext.updateClause = this.create.update(tableName);

            //set condition and add where condition
            if( visitorContext.condition != null) {
                visitorContext.updateClause.set(columnName, columnValue).where(visitorContext.condition);
            } else {
                visitorContext.updateClause.set(columnName, columnValue);
            }
        } else if (mutationName.contains("delete")) {
            //build delete clause
            visitorContext.deleteClause = this.create.delete(tableName);

            //add where condition
            if( visitorContext.condition != null) {
                visitorContext.deleteClause.where(visitorContext.condition);
            } else {
                throw new RuntimeException("Missing condition");
            }
        } else {
            throw new RuntimeException("Unexpected value: " + mutationName);
        }
    }

    @Override
    public void visitArgument(Field field, GraphQLFieldDefinition definition, GraphQLObjectType type, Argument arg) {
        VisitorContext visitorContext = this.stack.peek();
        String argumentName = arg.getName();

        if(argumentName.equals("input")) {

            if(!arg.getValue().getChildren().isEmpty()) {
                arg.getValue().getChildren().forEach(c -> {
                    if (c instanceof ObjectField)
                        visitorContext.selectedColumns.put(((ObjectField) c).getName(), getColumnValues(((ObjectField) c).getValue()));
                });
            }
        } else if (argumentName.equals("filter")) {
            Value<?> argValue = arg.getValue();

            //build condition for update or delete clause
            SQLFilterBuilder filterBuilder = new SQLFilterBuilder(null);
            FilterScanner<Condition> filterScanner = new FilterScanner<>(filterBuilder);

            Condition condition = filterScanner.scan((ObjectValue) argValue, FilterScanner.Clause.and).condition;
            if (visitorContext.condition == null) {
                    visitorContext.condition = condition;
            } else {
                visitorContext.condition = visitorContext.condition.and(condition);
            }
        } else  {
            throw new RuntimeException("Unexpected value: " + argumentName);
        }
    }

    public String getSQL() {
        VisitorContext visitorContext = this.stack.peek();
        if(visitorContext.mutationType.contains("create"))
            return visitorContext.insertClause.toString();
        else if(visitorContext.mutationType.contains("update"))
            return visitorContext.updateClause.toString();
        else if(visitorContext.mutationType.contains("delete"))
            return visitorContext.deleteClause.toString();
        else
            return new RuntimeException("Unexpected value: " + visitorContext.mutationType).toString();
    }

    private Object getColumnValues(Value field) {
        Object fieldValue = null;
        if (field instanceof IntValue) {
            fieldValue = ((IntValue) field).getValue();
        } else if (field instanceof StringValue) {
            fieldValue = ((StringValue) field).getValue();
        } else if (field instanceof FloatValue) {
            fieldValue = ((FloatValue) field).getValue();
        } else if (field instanceof BooleanValue) {
            fieldValue = ((BooleanValue) field).isValue();
        }
        return fieldValue;
    }

    @Override
    public void visitScalar(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLType type) {

    }

    @Override
    public void startVisitObject(Field field, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {

    }

    @Override
    public void endVisitObject(Field rootField, GraphQLFieldDefinition rootDefinition, GraphQLObjectType type) {

    }

}



