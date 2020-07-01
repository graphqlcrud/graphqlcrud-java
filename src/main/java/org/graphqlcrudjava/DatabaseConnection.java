package org.graphqlcrudjava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

    //Any Data Source Connection
        public static Connection connect(String jdbcUrl, String driver, String username, String password) throws SQLException {
            try {
            Class.forName(driver).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.error("Unable to find suitable driver.", e);
            }

        return DriverManager.getConnection(jdbcUrl, username, password);
        }

        //Connection to Teiid
        public static Connection connect() throws SQLException {
            try {
            Driver teiidDriver = new org.teiid.jdbc.TeiidDriver();
            DriverManager.registerDriver(teiidDriver);
        } catch (Exception e) {
                LOGGER.error("Unable to find suitable driver.", e);
            }
        return DriverManager.getConnection("jdbc:teiid:customer@mm://localhost:31000", "sa", "sa");

        }
}
