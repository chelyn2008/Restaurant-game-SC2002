package restaurant.control;

/** Small helpers shared by the command parsers. */
final class CommandParsing {
    private CommandParsing() {
    }

    static String[] words(String text) {
        String trimmed = text == null ? "" : text.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Please type a command.");
        }
        return trimmed.split("\\s+");
    }

    /** Accepts "C2", "c2" or "2" and returns "C2". */
    static String customerId(String word) {
        String digits = word.startsWith("c") ? word.substring(1) : word;
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("'" + word + "' is not a customer id like C1.");
        }
        return "C" + Integer.parseInt(digits);
    }
}
