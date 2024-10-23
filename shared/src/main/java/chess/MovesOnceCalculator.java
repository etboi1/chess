package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MovesOnceCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        Collection<ChessMove> moves = new HashSet<>();

        int[] xDimension = new int[0];
        int[] yDimension = new int[0];
        switch (piece.getPieceType()) {
            case ChessPiece.PieceType.KING -> {
                xDimension = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
                yDimension = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};
            }
            case ChessPiece.PieceType.KNIGHT -> {
                xDimension = new int[]{-2, -2, -1, -1, 1, 1, 2, 2};
                yDimension = new int[]{-1, 1, -2, 2, -2, 2, -1, 1};
            }
        }

        for (int i = 0; i < xDimension.length; i++) {
            int col = position.getColumn();
            int row = position.getRow();

            col += xDimension[i];
            row += yDimension[i];

            if (1 <= row && row <= 8 && 1 <= col && col <= 8) {
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece checkSquare = board.getPiece(newPosition);

                if (checkSquare != null) {
                    if (checkSquare.getTeamColor() != piece.getTeamColor()) {
                        //Can capture enemy piece
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                } else {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }
}
