package org.graphqlcrudjava;

import java.sql.*;

public class DatabaseConnection {

        //Any Data Source Connection
        public static Connection connect(String jdbcUrl, String driver, String username, String password) throws SQLException {
            try {
            Class.forName(driver).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(jdbcUrl, username, password);
        }

        //Connection to Teiid
        public static Connection connect() throws SQLException {
            try {
            Driver teiidDriver = new org.teiid.jdbc.TeiidDriver();
            DriverManager.registerDriver(teiidDriver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection("jdbc:teiid:customer@mm://localhost:31000", "sa", "sa");

        }
}
