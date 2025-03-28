package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class InGameClient {
    //Board dimensions
    private static final int BOARD_SIZE_IN_SQUARES = 10;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 1;

    public String help(){
        return """
                Please choose one of the following options:
                - highlight <position> (ex. f5)
                - move <source> <destination> <optional: promotion> (ex. f5 e4 queen)
                - redrawBoard
                - resign
                - leave
                - help
                """;
    }

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
