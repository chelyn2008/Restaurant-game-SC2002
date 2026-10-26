package restaurant.control;

public record HostCommand(Action action, String customerId) {

    public enum Action { SEAT, WAIT }

    public static final HostCommand WAIT = new HostCommand(Action.WAIT, null);

    /** Parses typed text. Throws IllegalArgumentException with a message if the text is not a valid command */
    public static HostCommand parse(String text) {
        String[] words = CommandParsing.words(text);
        if (words.length == 1 && words[0].equals("wait")) {
            return WAIT;
        }
        if (words.length != 2 || !words[0].equals("seat")) {
            throw new IllegalArgumentException("Host commands are: seat <id>, wait.");
        }
        return new HostCommand(Action.SEAT, CommandParsing.customerId(words[1]));
    }
}
