package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class BoardDisplay {

    public static void displayBoard(ChessBoard board) {
        System.out.println();
        printBoard(board, false);
        System.out.print(SET_BG_COLOR_LIGHT_BLUE + SET_TEXT_COLOR_BLACK + "-".repeat(30) + RESET_BG_COLOR);
        System.out.println();
        printBoard(board, true);
    }

    private static void printBoard(ChessBoard board, boolean flip) {

        int start = flip ? 1 : 8;
        int end = flip ? 8 : 1;
        int step = flip ? 1 : -1;

        int colStart = flip ? 8 : 1;
        int colEnd = flip ? 1 : 8;
        int colStep = flip ? -1 : 1;

        printColumnLabels(flip, colStart, colEnd, colStep);

        for (int row = start; flip ? row <= end : row >= end; row += step) {
            System.out.print(SET_BG_COLOR_LIGHT_BLUE + SET_TEXT_COLOR_BLACK + " " + row + " ");
            for (int col = colStart; flip ? col >= colEnd : col <= colEnd; col += colStep) {
                if ((row + col) % 2 == 1) {
                    System.out.print(SET_BG_COLOR_LIGHT_BROWN);
                } else {
                    System.out.print(SET_BG_COLOR_BROWN);
                }

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    System.out.print(EMPTY);
                } else {
                    System.out.print(convertToUnicode(piece) + RESET_TEXT_COLOR);
                }
            }
            System.out.println(SET_BG_COLOR_LIGHT_BLUE + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);
        }

        printColumnLabels(flip, colStart, colEnd, colStep);
    }

    private static void printColumnLabels(boolean flip, Integer colStart, Integer colEnd, Integer colStep) {
        System.out.print(SET_BG_COLOR_LIGHT_BLUE + SET_TEXT_COLOR_BLACK + "   ");

        String[] colNames = "abcdefgh".split("");
        for (int i = colStart; flip ? i >= colEnd : i <= colEnd; i += colStep) {
            System.out.print(" " + colNames[i - 1] + " ");
        }
        System.out.print("   ");
        System.out.println(RESET_BG_COLOR);
    }

    private static String convertToUnicode(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }
}