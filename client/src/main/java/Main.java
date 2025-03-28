import chess.*;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        String port = "8080";
        if (args.length == 1) {
            port = args[0];
        }
        var serverUrl = String.format("http://localhost:%s", port);
        new Repl(serverUrl).run();
    }
}