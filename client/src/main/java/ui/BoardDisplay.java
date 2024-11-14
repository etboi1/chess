//package ui;
//
//import static ui.EscapeSequences.*;
//import chess.ChessBoard;
//import chess.ChessGame;
//import chess.ChessPiece;
//import chess.ChessPosition;
//
//public class BoardDisplay {
//
//    public static void displayBoard(ChessBoard board) {
//        System.out.print(ERASE_SCREEN);
//
//        for (int row = 7; row >= 0; row--) {
//            for (int col = 0; col < 8; col++) {
//                if ((row + col) % 2 == 1) {
//                    System.out.print(SET_BG_COLOR_LIGHT_BROWN);
//                } else {
//                    System.out.print(SET_BG_COLOR_BROWN);
//                }
//
//                ChessPosition position = new ChessPosition(row + 1, col + 1);
//                ChessPiece piece = board.getPiece(position);
//
//                if (piece == null) {
//                    System.out.print(EMPTY);
//                } else {
//                    System.out.print(SET_TEXT_COLOR_BLACK + convertToUnicode(piece));
//                }
//
//                System.out.print(RESET_BG_COLOR);
//            }
//            System.out.println();
//        }
//
//        System.out.print(RESET_TEXT_COLOR);
//    }
//
//    private static String convertToUnicode(ChessPiece piece) {
//        return switch (piece.getPieceType()) {
//            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
//            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
//            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
//            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
//            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
//            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
//        };
//    }
//
//    public static void main(String[] args) {
//        ChessBoard board = new ChessBoard();
//        board.resetBoard();
//        displayBoard(board);
//    }
//}

package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class BoardDisplay {

    // Prints the board in the standard orientation with White at the bottom
    public static void printStandardOrientation(ChessBoard board) {
        printBoardWithOrientation(board, true);
    }

    // Prints the board in the rotated orientation with Black at the bottom
    public static void printRotatedOrientation(ChessBoard board) {
        printBoardWithOrientation(board, false);
    }

    // Helper function to print with specified orientation
    private static void printBoardWithOrientation(ChessBoard board, boolean whiteBottom) {
        // Print column letters with background color
        System.out.print(SET_BG_COLOR_LIGHT_BLUE + "   ");
        for (char col = 'a'; col <= 'h'; col++) {
            System.out.print(" " + col + " ");
        }
        System.out.println(RESET_BG_COLOR);

        int rowStart = whiteBottom ? 8 : 1;
        int rowEnd = whiteBottom ? 1 : 8;
        int rowStep = whiteBottom ? -1 : 1;

        // Print each row of the board
        for (int row = rowStart; whiteBottom ? row >= rowEnd : row <= rowEnd; row += rowStep) {
            System.out.print(SET_BG_COLOR_LIGHT_BLUE + row + " "); // Row number on the left
            for (int col = 1; col <= 8; col++) {
                // Alternate colors for the board squares
                if ((row + col) % 2 == 1) {
                    System.out.print(SET_BG_COLOR_LIGHT_BROWN);
                } else {
                    System.out.print(SET_BG_COLOR_BROWN);
                }

                // Print the piece or empty space
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    System.out.print(EMPTY);
                } else {
                    System.out.print(SET_TEXT_COLOR_BLACK + convertToUnicode(piece));
                }
            }
            System.out.println(SET_BG_COLOR_LIGHT_BLUE + " " + row + RESET_BG_COLOR); // Row number on the right
        }

        // Print column letters again at the bottom
        System.out.print(SET_BG_COLOR_LIGHT_BLUE + "   ");
        for (char col = 'a'; col <= 'h'; col++) {
            System.out.print(" " + col + " ");
        }
        System.out.println(RESET_BG_COLOR);
    }

    // Returns the symbol for each chess piece based on color and type
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

    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        System.out.println("Standard Orientation:");
        BoardDisplay.printStandardOrientation(board);

        System.out.println("\nRotated Orientation:");
        BoardDisplay.printRotatedOrientation(board);
    }
}