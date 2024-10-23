package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MovesOnceCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        Collection<ChessMove> moves = new HashSet<>();

        int[] X = new int[0];
        int[] Y = new int[0];
        switch (piece.getPieceType()) {
            case ChessPiece.PieceType.KING -> {
                X = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
                Y = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};
            }
            case ChessPiece.PieceType.KNIGHT -> {
                X = new int[]{-2, -2, -1, -1, 1, 1, 2, 2};
                Y = new int[]{-1, 1, -2, 2, -2, 2, -1, 1};
            }
        }

        for (int i = 0; i < X.length; i++) {
            int col = position.getColumn();
            int row = position.getRow();

            col += X[i];
            row += Y[i];

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
