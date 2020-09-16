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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

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
        Statement stmt = c.createStatement();
        boolean hasResults = stmt.execute(sql);
        if (hasResults) {
            rs = stmt.getResultSet();
        }
        ctx.setStmt(stmt);
        ctx.setResultSet(rs);
        return new ResultSetList(rs, true);
    }

    private String buildSQL(DataFetchingEnvironment environment) {
        SQLQueryBuilderVisitor visitor = new SQLQueryBuilderVisitor(environment.getContext());
        QueryScanner scanner = new QueryScanner(environment, visitor);
        scanner.scan(environment.getField(), environment.getFieldDefinition(), null, true);
        return visitor.getSQL();
    }
}
