package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MySqlGameDAO extends BaseMySqlDAO implements GameDAO {
    public MySqlGameDAO() throws DataAccessException {
        super();
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE games";
        super.performUpdate(statement);
    }

    @Override
    public int createGame(GameData newGame) throws Exception {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        return performUpdate(statement, newGame.whiteUsername(), newGame.blackUsername(), newGame.gameName(), newGame.game());
    }

    @Override
    public GameData getGame(Integer gameID) throws Exception {
        var statement = "SELECT * FROM games WHERE gameID=?";
        try (var rs = performQuery(statement, gameID)) {
            if (rs.next()) {
                return readGame(rs);
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return result;
    }

    @Override
    public void updateGame(Integer gameID, GameData gameUpdate) throws Exception {
        var statement = "UPDATE games SET gameID=?, whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
        performUpdate(statement, gameID, gameUpdate.whiteUsername(), gameUpdate.blackUsername(), gameUpdate.gameName(), gameUpdate.game(), gameID);
    }

    protected GameData readGame(ResultSet rs) throws DataAccessException {
        String json;
        int gameID;
        String whiteUsername;
        String blackUsername;
        String gameName;
        try {
            gameID = rs.getInt("gameID");
            whiteUsername = rs.getString("whiteUsername");
            blackUsername = rs.getString("blackUsername");
            gameName = rs.getString("gameName");
            json = rs.getString("game");
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        ChessGame game = new Gson().fromJson(json, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}
