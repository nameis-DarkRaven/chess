package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    private CommandType type;
    private String authToken;
    private Integer gameID;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
