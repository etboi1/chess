package dataaccess;

import javax.xml.crypto.Data;
import java.sql.SQLException;

public class BaseMySqlDAO {

    private static boolean isInitialized = false;

    protected BaseMySqlDAO() throws DataAccessException {
        initializeDatabase();
    }

    private static synchronized void initializeDatabase() throws DataAccessException {
        // I'm including this (the conditional check) because I need this initializer to be able to throw DataAccessExceptions,
        // which a static block can't do unfortunately, BUT I don't want this constructor to run every time
        // I create an instance of one of the children
        if (!isInitialized) {
            String[] createStatements = {
                    """
                    CREATE TABLE IF NOT EXISTS pet (
                      `id` int NOT NULL AUTO_INCREMENT,
                      `name` varchar(256) NOT NULL,
                      `type` ENUM('CAT', 'DOG', 'FISH', 'FROG', 'ROCK') DEFAULT 'CAT',
                      `json` TEXT DEFAULT NULL,
                      PRIMARY KEY (`id`),
                      INDEX(type),
                      INDEX(name)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                    """
            };

            try {
                DatabaseManager.createDatabase();
                try (var conn = DatabaseManager.getConnection()) {
                    for (var statement : createStatements) {
                        try (var preparedStatement = conn.prepareStatement(statement)) {
                            preparedStatement.executeUpdate();
                        }
                    }
                }
                isInitialized = true;
            } catch (SQLException ex) {
                throw new DataAccessException("Unable to configure database: " + ex.getMessage());
            }
        }
    }
}
