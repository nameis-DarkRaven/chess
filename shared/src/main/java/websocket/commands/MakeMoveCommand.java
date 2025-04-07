package websocket.commands;

public class MakeMoveCommand extends UserGameCommand {
    private CommandType type;
    private String authToken;
    private Integer gameID;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
