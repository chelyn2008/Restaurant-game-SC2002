package restaurant.domain;

/**
 * The outcome of a staff action. A failed result carries the reason and
 * does not use up the staff member's task for the turn.
 */
public record ActionResult(boolean success, String message) {

    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult fail(String reason) {
        return new ActionResult(false, reason);
    }
}
