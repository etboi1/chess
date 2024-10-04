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
                ChessMove move = iterator.next();
                gameBoard.removePiece(move.getStartPosition());
                gameBoard.addPiece(move.getEndPosition(), piece);
                if (isInCheck(piece.getTeamColor())) {
                    iterator.remove();
                }
                gameBoard.addPiece(move.getStartPosition(), piece);
                gameBoard.removePiece(move.getEndPosition());
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
                    if (canPieceThreaten(piece, square, currentKing)) {
                        return Boolean.TRUE;
                    }
                }
            }
        };
        return Boolean.FALSE;
    }

    private boolean canPieceThreaten(ChessPiece piece, ChessPosition startPosition, ChessPosition endPosition) {
        Collection<ChessMove> possibleMoves = piece.pieceMoves(gameBoard, startPosition);

        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().equals(endPosition)) {
                //For each move a piece can make, check if it would put its own king in check
                //Do this by making and then reversing move
                gameBoard.removePiece(startPosition);
                gameBoard.addPiece(endPosition, piece);
                boolean resultsInCheck = isInCheck(piece.getTeamColor());
                gameBoard.addPiece(startPosition, piece);
                gameBoard.removePiece(endPosition);

                if (!resultsInCheck) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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
