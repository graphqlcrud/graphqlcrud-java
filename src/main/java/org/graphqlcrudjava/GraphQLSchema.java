package org.graphqlcrudjava;

import org.graphqlcrudjava.model.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class GraphQLSchema {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLSchema.class);

    public static void main(String[] args) throws Exception {

        List<Entity> entities;
        try(DatabaseAnalyzer analyzer = initializeDatabaseAnalyzer()) {
            entities = analyzer.initializeEntities();
            LOGGER.debug("Entities: "+ entities);
        }
    }

    private static DatabaseAnalyzer initializeDatabaseAnalyzer() throws IOException {

        DatabaseAnalyzer analyzer = null;
        try {
            Connection connection = DatabaseConnection.connect();
            analyzer = new DatabaseAnalyzer(connection);
        } catch (SQLException e) {
            LOGGER.error("Unable to configure database metadata.", e);
        }
        return analyzer;
    }

}
