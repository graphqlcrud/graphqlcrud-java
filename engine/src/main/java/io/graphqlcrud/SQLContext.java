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

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLContext implements Closeable{

    private Connection connection;
    private ResultSet rs;
    private String sql;

    public String getSQL() {
        return sql;
    }

    public void setSQL(String sql) {
        this.sql = sql;
    }

    public SQLContext(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public ResultSet getResultSet() {
        return this.rs;
    }
    
    public void setResultSet(ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.rs != null) {
                this.rs.close();
            }
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}