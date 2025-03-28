package ui;

import java.util.Scanner;

public class Repl {
    private final Client client;
    private State state;

    public Repl(String serverURL) {
        client=new Client(serverURL);
    }

    public void run() {
        System.out.println("Welcome to Chess. Type \"help\" to begin.");
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