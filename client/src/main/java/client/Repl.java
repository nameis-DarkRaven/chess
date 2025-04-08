package client;

import java.util.Scanner;

public class Repl {
    private final Client client;

    public Repl(String serverURL) {
        client=new Client(serverURL);
    }

    public void run() {
        System.out.println((EscapeSequences.SET_TEXT_COLOR_CROWN + "♕ ") +
                (EscapeSequences.RESET_TEXT_COLOR + "Welcome to Chess") +
                (EscapeSequences.SET_TEXT_COLOR_CROWN + " ♕"));
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.print(client.help());

        Scanner scanner=new Scanner(System.in);
        var result="";
        while (!result.equals("quit")) {
            printPrompt();
            String line=scanner.nextLine();
            try {
                result=client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg=e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + ">>> ");
    }
}