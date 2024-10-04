package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard gameBoard = new ChessBoard();
    private TeamColor currentTeam = TeamColor.WHITE;

    public ChessGame() {
        gameBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return TeamColor.WHITE;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeam = team;
    }

    private void nextTurn() {
        if (currentTeam == TeamColor.WHITE) {
            currentTeam = TeamColor.BLACK;
        } else {
            currentTeam = TeamColor.WHITE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(gameBoard, chessGame.gameBoard) && currentTeam == chessGame.currentTeam;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(gameBoard, currentTeam);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = gameBoard.getPiece(startPosition);

        if (piece != null) {
            Collection<ChessMove> moves = piece.pieceMoves(gameBoard, startPosition);
            Iterator<ChessMove> iterator = moves.iterator();
            while (iterator.hasNext()) {
                //Make each possible move and see if it results in putting own king in check
                ChessMove move = iterator.next();

                gameBoard.removePiece(move.getStartPosition());
                //Check to see if there's an enemy piece in the end position
                ChessPiece placeholder = null;
                if (gameBoard.getPiece(move.getEndPosition()) != null) {
                    placeholder = gameBoard.getPiece(move.getEndPosition());
                }
                gameBoard.addPiece(move.getEndPosition(), piece);

                //Check if move puts own king in check
                if (isInCheck(piece.getTeamColor())) {
                    iterator.remove();
                }

                //Reset game board
                gameBoard.addPiece(move.getStartPosition(), piece);
                gameBoard.removePiece(move.getEndPosition());
                if (placeholder != null) {
                    gameBoard.addPiece(move.getEndPosition(), placeholder);
                }
            }
            return moves;
        }
        return null;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = gameBoard.getPiece(startPosition);
        Collection<ChessMove> allValid = validMoves(startPosition);

        if (piece != null && currentTeam == piece.getTeamColor()) {
        //Only make the move if it's a valid move
            if (allValid.contains(move)) {
                //Check for promotion
                if (move.getPromotionPiece() != null) {
                    piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                }

                gameBoard.addPiece(endPosition, piece);
                gameBoard.removePiece(startPosition);
                nextTurn();
                return;
            }
        }
        throw new InvalidMoveException();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition currentKing = null;
        //Find the current king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition square = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(square);
                if (piece != null && piece.getTeamColor() == teamColor && 
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    currentKing = square;
                }
            }
        };
        
        //Loop through each square and see if any of the opposite team can capture current king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition square = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(square);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    //Check if each enemy piece can capture king
                    Collection<ChessMove> possibleMoves = piece.pieceMoves(gameBoard, square);
                    for (ChessMove move : possibleMoves) {
                        if (move.getEndPosition().equals(currentKing)) {
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        };
        return Boolean.FALSE;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition square = new ChessPosition(row, col);
                    ChessPiece piece = gameBoard.getPiece(square);
                    if (piece != null && piece.getTeamColor() == teamColor) {
                        Collection<ChessMove> possibleMoves = validMoves(square);
                        if (possibleMoves != null && !possibleMoves.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //Should be the exact same as checkmate, except we just don't check if the team is in check at the beginning
        //We'll include a check to make sure the team isn't in checkmate
        if (!isInCheckmate(teamColor)) {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition square = new ChessPosition(row, col);
                    ChessPiece piece = gameBoard.getPiece(square);
                    if (piece != null && piece.getTeamColor() == teamColor) {
                        Collection<ChessMove> possibleMoves = validMoves(square);
                        if (possibleMoves != null && !possibleMoves.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}
