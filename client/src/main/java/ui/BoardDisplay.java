package ui;

import static ui.EscapeSequences.*;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardDisplay {

    public static void displayBoard(ChessBoard board) {
        System.out.print(EscapeSequences.ERASE_SCREEN);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 0) {
                    System.out.print(SET_BG_COLOR_LIGHT_BROWN);
                } else {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BROWN);
                }

                ChessPosition position = new ChessPosition(row + 1, col + 1);
                ChessPiece piece = board.getPiece(position);

                if (piece == null) {
                    System.out.print(EMPTY);
                } else {
                    System.out.print(convertToUnicode(piece));
                }

                System.out.print(RESET_BG_COLOR);
            }
            System.out.println();
        }

        // Reset text color
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private static String convertToUnicode(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        displayBoard(board);
    }
}
