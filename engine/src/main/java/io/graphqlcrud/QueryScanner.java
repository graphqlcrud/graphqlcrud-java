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

import java.util.List;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;

public class QueryScanner {
    private QueryVisitor visitor;
    private DataFetchingEnvironment environment;

    public QueryScanner(DataFetchingEnvironment environment, QueryVisitor visitor) {
        this.environment = environment;
        this.visitor = visitor;
    }

    public void scan(Field field, GraphQLFieldDefinition definition, String fqn, boolean root) {

        GraphQLType type = definition.getType();
        if (type instanceof GraphQLModifiedType) {
            type = ((GraphQLModifiedType) type).getWrappedType();
        }

        if (GraphQLTypeUtil.isScalar(type)) {
            this.visitor.visitScalar(field, definition, type);
            scanArguments(field, definition, type);
        } else if (type instanceof GraphQLObjectType) {

            if (root) {
                this.visitor.startVisitRootObject(field, definition, (GraphQLObjectType)type);
            } else {
                this.visitor.startVisitObject(field, definition, (GraphQLObjectType)type);
            }

            // walk the selected fields
            List<Selection> fields = field.getSelectionSet().getSelections();
            for (int i = 0; i < fields.size(); i++) {
                Field f = (Field)fields.get(i);
                String name = f.getAlias() != null ? f.getAlias() : f.getName();
                String fieldName = fqn == null ? name : fqn+"/"+name;
                SelectedField childField = this.environment.getSelectionSet().getField(fieldName);
                GraphQLFieldDefinition childDefinition = childField.getFieldDefinition();
                scan(f, childDefinition, fieldName, false);
            }

            // now walk the arguments, maybe specific ones later
            scanArguments(field, definition, type);

            if (root) {
                this.visitor.endVisitRootObject(field, definition, (GraphQLObjectType)type);
            } else {
                this.visitor.endVisitObject(field, definition, (GraphQLObjectType)type);
            }
        }
    }

    private void scanArguments(Field field, GraphQLFieldDefinition definition, GraphQLType type) {
        List<Argument> args = field.getArguments();
        if (args != null && !args.isEmpty()) {
            for (int i = 0; i < args.size(); i++) {
                Argument arg = args.get(i);
                this.visitor.visitArgument(field, definition, (GraphQLObjectType)type, arg);
            }
        }
    }
}
