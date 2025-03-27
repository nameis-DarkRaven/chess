package ui;

public class PreLogin {
    private final Client client;

    public PreLogin(String serverURL){
        client = new Client(serverURL);
    }

    public void run(){
        System.out.println("Welcome to Chess. Type \"help\" to begin.");
        System.out.print(client.help());
    }
}
