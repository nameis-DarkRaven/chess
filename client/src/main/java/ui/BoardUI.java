package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*; // all characters and text/background colors

public class BoardUI {
    //Board dimensions
    private static final int BOARD_SIZE_IN_SQUARES = 10;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 1;

    public static void printBoard() {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

//        out.print(ERASE_SCREEN);
        drawHeader(out);

        drawBoard(out);

    }



    // draw header

    private static void drawHeader(PrintStream out){

    }

    // draw board
    private static void drawBoard(PrintStream out){

    }

    //

}
