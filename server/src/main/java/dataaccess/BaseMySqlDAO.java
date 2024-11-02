package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.UserData;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.function.Function;

import static java.sql.Types.NULL;

public class BaseMySqlDAO {

    private static boolean isInitialized = false;

    protected BaseMySqlDAO() throws DataAccessException {
        initializeDatabase();
    }

    protected int performUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(ps, params);
            ps.executeUpdate();

            var rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected ResultSet performQuery(String statement, Object... params) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(statement);
            setParameters(ps, params);
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (var i = 0; i < params.length; i++) {
            var param = params[i];
            switch (param) {
                case String p -> ps.setString(i + 1, p);
                case Integer p -> ps.setInt(i + 1, p);
                case ChessGame p -> ps.setString(i + 1, new Gson().toJson(p));
                case null -> ps.setNull(i + 1, NULL);
                default -> {
                }
            }
        }
    }

    private static synchronized void initializeDatabase() throws DataAccessException {
        if (isInitialized) {
            return;
        }

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new DataAccessException("Unable to configure database: " + e.getMessage());
        }
        createTables();

        isInitialized = true;
    }

    private static void createTables() throws DataAccessException {
        String[] createStatements = getCreateTableStatements();

        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                executeStatement(conn, statement);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error creating tables: " + ex.getMessage());
        }
    }

    private static String[] getCreateTableStatements() {
        return new String[] {
        """
        CREATE TABLE IF NOT EXISTS users (
          `username` varchar(256) NOT NULL,
          `password` varchar(256) NOT NULL,
          `email` varchar(256) NOT NULL,
          PRIMARY KEY (`username`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """,
        """
        CREATE TABLE IF NOT EXISTS games (
          `gameID` int NOT NULL AUTO_INCREMENT,
          `whiteUsername` varchar(256) DEFAULT NULL,
          `blackUsername` varchar(256) DEFAULT NULL,
          `gameName` varchar(256) NOT NULL,
          `game` JSON NOT NULL,
          PRIMARY KEY (`gameID`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """,
        """
        CREATE TABLE IF NOT EXISTS auth (
          `authToken` varchar(256) NOT NULL,
          `username` varchar(256) NOT NULL,
          PRIMARY KEY (`authToken`),
          INDEX (`username`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
        };
    }

    // Extracted method to execute a statement
    private static void executeStatement(Connection conn, String statement) throws SQLException {
        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        }
    }
}
