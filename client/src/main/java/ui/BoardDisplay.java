package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

import static ui.EscapeSequences.*;

public class BoardDisplay {

    public static void printBoard(ChessGame game, boolean flip, ChessPosition...squareOfInterest) {

        ChessBoard board = game.getBoard();

        int start = flip ? 1 : 8;
        int end = flip ? 8 : 1;
        int step = flip ? 1 : -1;

        int colStart = flip ? 8 : 1;
        int colEnd = flip ? 1 : 8;
        int colStep = flip ? -1 : 1;

        printColumnLabels(flip, colStart, colEnd, colStep);

        ChessPosition startSquare = squareOfInterest.length > 0 ? squareOfInterest[0] : null;
        Collection<ChessPosition> validEndPositions = new HashSet<>();
        if (startSquare != null) {
            Collection<ChessMove> validMoves = game.validMoves(startSquare);
            for (ChessMove move : validMoves) {
                validEndPositions.add(move.getEndPosition());
            }
        }

        for (int row = start; flip ? row <= end : row >= end; row += step) {
            System.out.print(SET_BG_COLOR_LIGHT_BLUE + SET_TEXT_COLOR_BLACK + " " + row + " ");
            for (int col = colStart; flip ? col >= colEnd : col <= colEnd; col += colStep) {
                if (startSquare != null && row == startSquare.getRow() && col == startSquare.getColumn()) {
                    System.out.print(SET_BG_COLOR_YELLOW);
                }
                else if ((row + col) % 2 == 1) {
                    if (validEndPositions.contains(new ChessPosition(row, col))) {
                        System.out.print(SET_BG_COLOR_GREEN);
                    } else {
                        System.out.print(SET_BG_COLOR_LIGHT_BROWN);
                    }
                } else {
                    if (validEndPositions.contains(new ChessPosition(row, col))) {
                        System.out.print(SET_BG_COLOR_DARK_GREEN);
                    } else {
                        System.out.print(SET_BG_COLOR_BROWN);
                    }
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