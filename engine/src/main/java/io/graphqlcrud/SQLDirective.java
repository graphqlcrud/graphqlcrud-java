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

import graphql.Scalars;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLList;

public class SQLDirective {
    private String tableName;
    private List<String> primaryFields;
    private List<String> foreignFields;

    public static SQLDirective find(List<GraphQLDirective> directives) {
        if (directives == null) {
            return null;
        }
        for (GraphQLDirective d : directives) {
            if (d.getName().equals("sql")) {
                return SQLDirective.newDirective(d);
            }
        }
        return null;
    }

    public static SQLDirective newDirective(GraphQLDirective d) {
        SQLDirective sql = new SQLDirective();

        if (d.getArgument("table") != null) {
            sql.tableName = d.getArgument("table").getValue().toString();
        }

        if (d.getArgument("keys") != null) {
            sql.primaryFields = (List<String>)d.getArgument("keys").getValue();
        }

        if (d.getArgument("reference_keys") != null) {
            sql.foreignFields = (List<String>)d.getArgument("reference_keys").getValue();
        }

        return sql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getPrimaryFields() {
        return primaryFields;
    }

    public void setPrimaryFields(List<String> primaryFields) {
        this.primaryFields = primaryFields;
    }

    public List<String> getForeignFields() {
        return foreignFields;
    }

    public void setForeignFields(List<String> foreignFields) {
        this.foreignFields = foreignFields;
    }

    public static Builder newDirective() {
        return new Builder();
    }

    public static class Builder {
        private String tableName;
        private List<String> primaryFields;
        private List<String> foreignFields;

        public Builder() {
        }

        public Builder tablename(String name) {
            this.tableName = name;
            return this;
        }

        public Builder primaryFields(List<String> fields) {
            this.primaryFields = fields;
            return this;
        }

        public Builder foreignFields(List<String> fields) {
            this.foreignFields = fields;
            return this;
        }

        public GraphQLDirective build() {
            GraphQLDirective.Builder b = GraphQLDirective.newDirective().name("sql");
            if (this.tableName != null) {
                b.argument(GraphQLArgument.newArgument().name("table").type(GraphQLList.list(Scalars.GraphQLString)).value(tableName));
            }
            if (this.primaryFields != null) {
                b.argument(GraphQLArgument.newArgument().name("keys").type(Scalars.GraphQLString)
                        .value(this.primaryFields));
            }
            if (this.foreignFields != null) {
                b.argument(GraphQLArgument.newArgument().name("reference_keys")
                        .type(GraphQLList.list(Scalars.GraphQLString)).value(this.foreignFields));
            }
            b.validLocations(Introspection.DirectiveLocation.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION);
            return b.build();
        }
    }
}
