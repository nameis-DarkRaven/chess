package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class InGameClient {
    //Board dimensions
    private static final int BOARD_SIZE_IN_SQUARES = 10;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 1;


    public void printBoard(ChessGame.TeamColor color) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessGame game = new ChessGame();

        drawHeader(out, color);
        drawBoard(out, game, color);
        drawHeader(out, color);

    }

    /* used to draw the top and bottom borders */
    private void drawHeader(PrintStream out, ChessGame.TeamColor color) {
        setBorderColor(out);
        List<String> headers = Arrays.asList(" a ", "  b ", "  c ", " d ", "  e ", "  f ", " g ", "  h ");
        if (color.equals(ChessGame.TeamColor.BLACK)) {
            headers = headers.reversed();
        }
        out.print("   ");
        for (String c : headers) {
            out.print(c);
        }
        out.print("   ");
        resetColors(out);
        out.println();
    }

    /* draw board */
    private void drawBoard(PrintStream out, ChessGame game, ChessGame.TeamColor color) {
        out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
        List<String> rowNums = Arrays.asList(" 8 ", " 7 ", " 6 ", " 5 ", " 4 ", " 3 ", " 2 ", " 1 ");
        ChessPiece[][] pieces = game.getBoard().getPieces();
        if (color.equals(ChessGame.TeamColor.BLACK)) {
            rowNums = rowNums.reversed();
            int index = 1;
            ChessPiece[][] blackPieces = new ChessPiece[8][8];
            for (ChessPiece[] row : Arrays.stream(pieces).toList()) {
                blackPieces[8 - index] = Arrays.asList(row).reversed().toArray(new ChessPiece[8]);
                index ++;
            }
            pieces = blackPieces;
        }

        int row = 0;
        for (ChessPiece[] pieceRow : pieces) {
            setBorderColor(out);
            out.print(rowNums.get(row));
            int index = 0;
            for (ChessPiece piece : pieceRow) {
                if (row % 2 == 0) {
                    if (index % 2 == 0) {
                        out.print(EscapeSequences.SET_BG_COLOR_TAN);
                    } else {
                        out.print(EscapeSequences.SET_BG_COLOR_BROWN);
                    }
                } else {
                    if (index % 2 == 0) {
                        out.print(EscapeSequences.SET_BG_COLOR_BROWN);
                    } else {
                        out.print(EscapeSequences.SET_BG_COLOR_TAN);
                    }
                }
                if (piece == null) {
                    out.print(EscapeSequences.EMPTY);
                } else {
                    out.print(getPiece(piece));
                }
                index++;
            }
            setBorderColor(out);
            out.print(rowNums.get(row));
            resetColors(out);
            out.println();
            row++;
        }
    }


    private String getPiece(ChessPiece piece) {
        if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
            return switch (piece.getPieceType()) {
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
        } else {
            return switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
        }
    }


    private static void setBorderColor(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_CROWN);
        out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
    }

    private static void resetColors(PrintStream out) {
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.print(EscapeSequences.RESET_TEXT_COLOR);
    }
}
