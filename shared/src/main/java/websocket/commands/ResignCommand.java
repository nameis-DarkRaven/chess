package websocket.commands;

public class ResignCommand extends UserGameCommand {
    private CommandType type;
    private String authToken;
    private Integer gameID;

    public ResignCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
