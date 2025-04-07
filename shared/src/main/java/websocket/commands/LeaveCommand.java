package websocket.commands;

public class LeaveCommand extends UserGameCommand {
    private CommandType type;
    private String authToken;
    private Integer gameID;

    public LeaveCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
