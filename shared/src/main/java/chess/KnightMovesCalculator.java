package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        Collection<ChessMove> moves = new HashSet<>();

        int[] directionX = {-2, -2, -1, -1, 1, 1, 2, 2}; //Honestly, I think this is still a good way to do it
        int[] directionY = {-1, 1, -2, 2, -2, 2, -1, 1}; //The night can move in any L combo

        for (int i = 0; i < directionX.length; i++) {
            int col = position.getColumn();
            int row = position.getRow();

            col += directionX[i];
            row += directionY[i];

            if (1 <= row && row <= 8 && 1 <= col && col <= 8 ) {
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece checkSquare = board.getPiece(newPosition);

                if (checkSquare != null) {
                    if (checkSquare.getTeamColor() != piece.getTeamColor()) {
                        //Can capture enemy piece
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                }
                else {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }
}
