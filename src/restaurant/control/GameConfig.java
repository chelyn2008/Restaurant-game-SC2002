package restaurant.control;

import java.util.List;

/** The settings for one game. {@link #baseSetting(long)} is the required base setting. */
public record GameConfig(int totalTurns,
                         int tableCount,
                         List<Integer> arrivalTurns,
                         int minPaidCustomers,
                         int minRevenueCents,
                         int minAverageSatisfaction,
                         long seed) {

    public static final long DEFAULT_SEED = 2002L;

    public GameConfig {
        arrivalTurns = List.copyOf(arrivalTurns);
    }

    /** 18 turns, 2 tables, arrivals on turns 1, 4, 7, 10, 13, 16; win with 4 paid, $45, average 60. */
    public static GameConfig baseSetting(long seed) {
        return new GameConfig(18, 2, List.of(1, 4, 7, 10, 13, 16), 4, 4500, 60, seed);
    }
}
