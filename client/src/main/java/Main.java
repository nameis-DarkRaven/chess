import chess.*;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        var serverUrl = String.format("http://localhost:%s", 0);
        if (args.length == 1) {
            serverUrl = args[0];
        }

        new Repl(serverUrl).run();
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
    }
}