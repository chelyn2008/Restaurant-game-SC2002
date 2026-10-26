package restaurant.control;

import restaurant.domain.menu.Menu;


/**
 * The settings for one game. Every stage uses the base setting (18 turns, 2 tables,
 * six arrivals, win with 4 paid, $45 and average 60); later stages switch on more features.
 */
public record GameConfig(int stage,
                         int totalTurns,
                         int tableCount,
                         ArrivalSchedule arrivalSchedule,
                         Menu menu,
                         boolean hostEnabled,
                         boolean happyHourEnabled,
                         int minPaidCustomers,
                         int minRevenueCents,
                         int minAverageSatisfaction,
                         long seed) {

    public static final long DEFAULT_SEED = 2002L;

    /** Stage 1: six Regular customers, base menu, no Host, no Happy Hour */
    public static GameConfig baseSetting(long seed) {
        return new GameConfig(1, 18, 2, ArrivalSchedule.allRegular(), Menu.standard(), false, false, 4, 4500, 60, seed);
    }

    /** Stage 2 rules with the given arrivals: base menu, no Host, no Happy Hour */
    public static GameConfig baseSetting(long seed, ArrivalSchedule arrivalSchedule) {
        return new GameConfig(2, 18, 2, arrivalSchedule, Menu.standard(), false, false, 4, 4500, 60, seed);
    }

    /** Stage 3: the Stage 2 arrivals, the menu with combos, a Host and Happy Hour */
    public static GameConfig stageThree(long seed) {
        return stageThree(seed, ArrivalSchedule.stageTwoMix());
    }

    public static GameConfig stageThree(long seed, ArrivalSchedule arrivalSchedule) {
        return new GameConfig(3, 18, 2, arrivalSchedule, Menu.withCombos(), true, true, 4, 4500, 60, seed);
    }

    /** The game for stage 1, 2 or 3, as selected with --stage */
    public static GameConfig forStage(int stage, long seed) {
        return switch (stage) {
            case 1 -> baseSetting(seed);
            case 2 -> baseSetting(seed, ArrivalSchedule.stageTwoMix());
            case 3 -> stageThree(seed);
            default -> throw new IllegalArgumentException("There is no stage " + stage + "; choose 1, 2 or 3.");
        };
    }
}
