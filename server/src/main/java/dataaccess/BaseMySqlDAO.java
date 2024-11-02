package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.UserData;

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
        // I'm including this (the conditional check) because I need this initializer to be able to throw DataAccessExceptions,
        // which a static block can't do unfortunately, BUT I don't want this constructor to run every time
        // I create an instance of one of the children
        if (!isInitialized) {
            String[] createStatements = {
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
