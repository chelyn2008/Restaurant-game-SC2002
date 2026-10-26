package restaurant.control;


public record ChefCommand(Action action, String customerId) {

    public enum Action { COOK, WAIT }

    public static final ChefCommand WAIT = new ChefCommand(Action.WAIT, null);

    /** Parses typed text. Throws IllegalArgumentException with a message if the text is not a valid command. */
    public static ChefCommand parse(String text) {
        String[] words = CommandParsing.words(text);
        if (words.length == 1 && words[0].equals("wait")) {
            return WAIT;
        }
        if (words.length != 2 || !words[0].equals("cook")) {
            throw new IllegalArgumentException("Chef commands are: cook <id>, wait.");
        }
        return new ChefCommand(Action.COOK, CommandParsing.customerId(words[1]));
    }
}
