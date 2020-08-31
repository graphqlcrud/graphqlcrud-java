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
package io.graphqlcrud.app;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.agroal.api.AgroalDataSource;
import io.graphqlcrud.DatabaseSchemaBuilder;
import io.graphqlcrud.GraphQLSchemaBuilder;
import io.graphqlcrud.SQLContext;
import io.graphqlcrud.model.Schema;

@Path("/graphql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GraphQLResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLResource.class);

    @Inject
    private AgroalDataSource datasource;

    private GraphQLSchema schema;

    @ConfigProperty(name = "graphqlcrud.datasource.schema")
    private String dbSchemaName;

    @ConfigProperty(name = "graphqlcrud.datasource.dialect")
    private String dialect;

    @POST
    public Map<String, Object> graphql(String query) throws Exception {
        GraphQLSchema schema = buildSchema();

        QueryParameters qp = QueryParameters.from(query);

        ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
                .query(qp.getQuery())
                .operationName(qp.getOperationName())
                .variables(qp.getVariables());

        // pass the datasource around
        try(SQLContext ctx = new SQLContext(this.datasource.getConnection())){
            ctx.setDialect(this.dialect);
            executionInput.context(ctx);

            GraphQL graphQL = GraphQL
                    .newGraphQL(schema)
                    //.instrumentation(connectionInstrumentation)
                    .build();

            ExecutionResult executionResult = graphQL.execute(executionInput.build());
            return executionResult.toSpecification();
        }
    }

    private GraphQLSchema buildSchema() throws SQLException {
        if (this.schema == null) {
            try(Connection conn = this.datasource.getConnection()){
                Schema dbSchema = DatabaseSchemaBuilder.getSchema(conn, this.dbSchemaName);
               this.schema = GraphQLSchemaBuilder.getSchema(dbSchema);
               SchemaPrinter sp = new SchemaPrinter();
               LOGGER.info(sp.print(this.schema));
            }
        }
        return this.schema;
    }
}