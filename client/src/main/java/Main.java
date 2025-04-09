import client.Repl;

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        new Repl(serverUrl).run();
    }
}