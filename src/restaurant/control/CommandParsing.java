package restaurant.control;

/** Small helpers shared by the command parsers */
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

    /** True for the single word "hint" (extra feature: ask for advice without using the task) */
    static boolean isHint(String[] words) {
        return words.length == 1 && words[0].equals("hint");
    }

    /** Accepts "C2", "c2" or "2" and returns "C2" */
    static String customerId(String word) {
        String digits = word.startsWith("c") ? word.substring(1) : word;
        String notAnId = "'" + word + "' is not a customer id like C1.";
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException(notAnId);
        }
        try {
            return "C" + Integer.parseInt(digits);
        } catch (NumberFormatException e) { // too many digits for an int, e.g. c99999999999
            throw new IllegalArgumentException(notAnId);
        }
    }
}
