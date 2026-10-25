package restaurant.control;

/** The manager's choice for the Waiter this turn, e.g. "seat C2" or "wait". */
public record WaiterCommand(Action action, String customerId) {

    public enum Action { SEAT, ORDER, SERVE, WAIT }

    public static final WaiterCommand WAIT = new WaiterCommand(Action.WAIT, null);

    /** Parses typed text. Throws IllegalArgumentException with a message if the text is not a valid command. */
    public static WaiterCommand parse(String text) {
        String[] words = CommandParsing.words(text);
        if (words.length == 1 && words[0].equals("wait")) {
            return WAIT;
        }
        String usage = "Waiter commands are: seat <id>, order <id>, serve <id>, wait.";
        if (words.length != 2) {
            throw new IllegalArgumentException(usage);
        }
        Action action = switch (words[0]) {
            case "seat" -> Action.SEAT;
            case "order" -> Action.ORDER;
            case "serve" -> Action.SERVE;
            default -> throw new IllegalArgumentException(usage);
        };
        return new WaiterCommand(action, CommandParsing.customerId(words[1]));
    }
}
