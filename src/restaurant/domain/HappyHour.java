package restaurant.domain;

/**
 * Happy Hour: the manager may switch it on once per game, at the start of a turn
 * It lasts for that turn and the next 
 * While active, orders taken get 20% off and
 * at the start of each active turn, customers still waiting for a table or food
 * recover 15 satisfaction (see Restaurant.applyHappyHourRecovery)
 */
public class HappyHour implements Promotion {
    public static final int DURATION_TURNS = 2;
    public static final int DISCOUNT_PERCENT = 20;
    public static final int RECOVERY_POINTS = 15;
    private static final int NOT_YET = -1;

    private int activatedOnTurn = NOT_YET;

    /** True until Happy Hour has been used; it can be activated only once per game */
    public boolean isAvailable() {
        return activatedOnTurn == NOT_YET;
    }

    public ActionResult activate(int turn) {
        if (!isAvailable()) {
            return ActionResult.fail("Happy Hour can be activated only once per game; it was activated on turn "
                    + activatedOnTurn + ".");
        }
        activatedOnTurn = turn;
        return ActionResult.ok("Happy Hour is ON for this turn and the next: " + DISCOUNT_PERCENT
                + "% off orders taken, and waiting customers recover " + RECOVERY_POINTS + " satisfaction at turn start.");
    }

    @Override
    public boolean isActive(int turn) {
        return activatedOnTurn != NOT_YET && turn >= activatedOnTurn && turn < activatedOnTurn + DURATION_TURNS;
    }

    /** Active turns left including this one (2 on the activation turn, 1 on the next), or 0 */
    public int remainingTurns(int turn) {
        return isActive(turn) ? activatedOnTurn + DURATION_TURNS - turn : 0;
    }

    /** The turn it was activated, or -1 if it has not been used */
    public int getActivatedOnTurn() {
        return activatedOnTurn;
    }

    @Override
    public String getName() {
        return "Happy Hour";
    }

    /** 20% off, rounded to the nearest cent (half a cent rounds up) */
    @Override
    public int applyDiscount(int priceCents) {
        return (priceCents * (100 - DISCOUNT_PERCENT) + 50) / 100;
    }
}
