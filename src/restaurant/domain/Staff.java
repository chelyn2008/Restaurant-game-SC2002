package restaurant.domain;

/** A staff member who can complete at most one task per turn. */
public abstract class Staff {
    private final String role;
    private boolean taskDone;

    protected Staff(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public boolean isAvailable() {
        return !taskDone;
    }

    /** Called at the start of every turn so the staff member can act again. */
    public void resetForNewTurn() {
        taskDone = false;
    }

    /** Choosing to do nothing this turn. */
    public ActionResult waitThisTurn() {
        if (!isAvailable()) {
            return ActionResult.fail(busyReason());
        }
        completeTask();
        return ActionResult.ok("waits this turn.");
    }

    protected void completeTask() {
        taskDone = true;
    }

    protected String busyReason() {
        return "The " + role + " has already completed a task this turn.";
    }
}
