package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        Collection<ChessMove> moves = new HashSet<>();

        //I will need to implement the following characteristics:
        //  DONE 1. White pawns can only move up the 2D array, black pawns can only move down
        //  DONE 2. On the initial move, pawns can move 2 spaces
        //      a. white pawns start in row 3, black pawns in row 6
        //  DONE 3. Pawns are blocked by a piece of ANY color directly in front of them
        //  DONE 4. Pawns can capture an enemy piece to the diagonal
        //      a. this means they can only move diagonally when it is an enemy piece to the diagonal
        //  5. When a pawn reaches the opposite side, they may promote to a new piece
        //      a. the opposite side is row 8 for white, row 1 for black
        //      b. the user should be able to pick which piece they can promote to

        ChessGame.TeamColor pawnColor = piece.getTeamColor();
        int[] possibleMoves = new int[2];

        //Orient the direction the pawn will be moving
        if (pawnColor == ChessGame.TeamColor.WHITE) {
            possibleMoves[0] = 1;
            possibleMoves[1] = 2;
        }
        else {
            possibleMoves[0] = -1;
            possibleMoves[1] = -2;
        }

        int row = position.getRow();
        int col = position.getColumn();
        row += possibleMoves[0];

        //Check to see if we'll need to be promoting any pieces.
        //There will never be a case in which we're in the initial position & need to promote, so we don't need to
        //worry about that conflicting.
        ArrayList<ChessPiece.PieceType> possiblePromotions = new ArrayList<>();
        if (requiresPromotion(position, pawnColor)) {
            possiblePromotions.add(ChessPiece.PieceType.BISHOP);
            possiblePromotions.add(ChessPiece.PieceType.KNIGHT);
            possiblePromotions.add(ChessPiece.PieceType.QUEEN);
            possiblePromotions.add(ChessPiece.PieceType.ROOK);
        }

        //Check to see if the space in front is occupied (a pawn in any position can do this, so we do this first)
        ChessPosition newPosition = new ChessPosition(row, col);
        ChessPiece checkSquare = board.getPiece(newPosition);

        if (checkSquare == null) {
            if (!possiblePromotions.isEmpty()) {
                for (ChessPiece.PieceType pieceType : possiblePromotions) {
                    moves.add(new ChessMove(position, newPosition, pieceType));
                }
            } else {
                moves.add(new ChessMove(position, newPosition, null));
            }
            //Check for and handle initial move - we do this here because if there's a piece at first square, we still can't move to second
            if (isInitialMove(position, pawnColor)) {
                row -= possibleMoves[0];
                row += possibleMoves[1];

                //Check to see if the space two spaces away is occupied
                newPosition = new ChessPosition(row, col);
                checkSquare = board.getPiece(newPosition);

                if (checkSquare == null) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                row -= possibleMoves[1];
                row += possibleMoves[0];
            }
        }

        //Check for diagonal attack moves (we will do this second because a pawn in any position is eligible for this move)
        if (0 <= col - 1) {
            ChessPosition leftDiagonal = new ChessPosition(row, col - 1);
            if (checkDiagonal(leftDiagonal, board, pawnColor)) {
                if (!possiblePromotions.isEmpty()) {
                    for (ChessPiece.PieceType pieceType : possiblePromotions) {
                        moves.add(new ChessMove(position, leftDiagonal, pieceType));
                    }
                } else {
                    moves.add(new ChessMove(position, leftDiagonal, null));
                }
            }
        }
        if (8 >= col + 1) {
            ChessPosition rightDiagonal = new ChessPosition(row, col + 1);
            if (checkDiagonal(rightDiagonal, board, pawnColor)) {
                if (!possiblePromotions.isEmpty()) {
                    for (ChessPiece.PieceType pieceType : possiblePromotions) {
                        moves.add(new ChessMove(position, rightDiagonal, pieceType));
                    }
                } else {
                    moves.add(new ChessMove(position, rightDiagonal, null));
                }
            }
        }
        return moves;
    }

    private Boolean isInitialMove(ChessPosition position, ChessGame.TeamColor pawnColor) {
        if ((pawnColor == ChessGame.TeamColor.WHITE && position.getRow() == 2) ||
                (pawnColor == ChessGame.TeamColor.BLACK && position.getRow() == 7)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private Boolean checkDiagonal(ChessPosition diagonal, ChessBoard board, ChessGame.TeamColor pawnColor) {
        ChessPiece enemyUnit = board.getPiece(diagonal);
        if (enemyUnit != null) {
            if (enemyUnit.getTeamColor() != pawnColor) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private Boolean requiresPromotion(ChessPosition position, ChessGame.TeamColor pawnColor) {
        if ((pawnColor == ChessGame.TeamColor.WHITE && position.getRow() == 7) ||
                (pawnColor == ChessGame.TeamColor.BLACK && position.getRow() == 2)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
