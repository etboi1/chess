package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class QueenMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        Collection<ChessMove> moves = new HashSet<>();

        int[] directionX = {-1, -1, -1, 0, 0, 1, 1, 1}; //Queens can move in a straight line in any direction
        int[] directionY = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < directionX.length; i++) {
            int col = position.getColumn();
            int row = position.getRow();

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
                    if (checkSquare.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                    //If there was a piece, whoever it belonged to, we can't move past it
                    break;
                }
                else {
                    //If the square is empty, we add it and keep going
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }
}
