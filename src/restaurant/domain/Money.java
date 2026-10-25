package restaurant.domain;

/** Formats money stored as integer cents, e.g. 1800 -> "$18.00". */
public final class Money {
    private Money() {
    }

    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
