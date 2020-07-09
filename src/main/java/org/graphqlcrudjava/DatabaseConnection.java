package org.graphqlcrudjava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class DatabaseConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

    private static  final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    //Any Data Source Connection
        public static Connection connect() throws SQLException, IOException {
            System.out.println("Enter JDBC URL : ");
            String jdbcUrl = bufferedReader.readLine();
            System.out.println("Enter Suitable Driver : ");
            String driver = bufferedReader.readLine();
            System.out.println("Enter your Username : ");
            String username = bufferedReader.readLine();
            System.out.println("Enter your Password : ");
            String password = bufferedReader.readLine();

            try {
            Class.forName(driver).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.error("Unable to find suitable driver.", e);
            }

        return DriverManager.getConnection(jdbcUrl, username, password);
        }
}
