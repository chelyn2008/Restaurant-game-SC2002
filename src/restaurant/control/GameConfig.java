package restaurant.control;

/** The settings for one game. {@link #baseSetting(long, ArrivalSchedule)} is the required base setting. */
public record GameConfig(int totalTurns,
                         int tableCount,
                         ArrivalSchedule arrivalSchedule,
                         int minPaidCustomers,
                         int minRevenueCents,
                         int minAverageSatisfaction,
                         long seed) {

    public static final long DEFAULT_SEED = 2002L;

    /** 18 turns, 2 tables, the given arrivals; win with 4 paid, $45, average 60. */
    public static GameConfig baseSetting(long seed, ArrivalSchedule arrivalSchedule) {
        return new GameConfig(18, 2, arrivalSchedule, 4, 4500, 60, seed);
    }

    /** The base setting with the Stage 1 all-Regular arrivals. */
    public static GameConfig baseSetting(long seed) {
        return baseSetting(seed, ArrivalSchedule.allRegular());
    }
}
