package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private PieceType type;
    private ChessGame.TeamColor pieceColor;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        this.pieceColor = pieceColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return 53 * Objects.hashCode(type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var myPiece = board.getPiece(myPosition);
        Collection<ChessMove> moves = new HashSet<>();

        if (myPiece.getPieceType() == PieceType.BISHOP) {
            int[] directionX = {-1, 1, -1, 1}; //We're going to do two arrays of four so we don't have to do a nested loop
            int[] directionY = {-1, -1, 1, 1}; //So it's diagonal left-down, diagonal right-down, diagonal left-up, diagonal right-up

            for (int i = 0; i < directionX.length; i++) {
                int col = myPosition.getColumn();
                int row = myPosition.getRow();

                while (true) {
                    col += directionX[i];
                    row += directionY[i];

                    if (col < 1 || col > 8 || row < 1 || row > 8) {
                        break;
                    }

                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece checkSquare = board.getPiece(newPosition);

                    //Check what's in the square we're trying to move to
                    if (checkSquare != null) {
                        //If the piece is an enemy piece, it's still a valid move because we can capture it
                        if (checkSquare.getTeamColor() != getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        //If there was a piece, whoever it belonged to, we can't move past it
                        break;
                    }
                    else {
                        //If the square is empty, we add it and keep going
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
        return moves;
    }

//    @Override
//    public String toString() {
//        return "ChessPiece{" +
//                "type = " + type +
//                ", teamColor = " + getTeamColor() +
//                '}';
//    }
}
