package restaurant.control;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import restaurant.domain.Money;
import restaurant.domain.Restaurant;

/** Checks the three targets after the last turn: paid customers, revenue and average satisfaction */
public class VictoryCondition {
    private final int minPaidCustomers;
    private final int minRevenueCents;
    private final int minAverageSatisfaction;

    public VictoryCondition(GameConfig config) {
        this.minPaidCustomers = config.minPaidCustomers();
        this.minRevenueCents = config.minRevenueCents();
        this.minAverageSatisfaction = config.minAverageSatisfaction();
    }

    public GameResult evaluate(Restaurant restaurant) {
        return evaluate(restaurant.getPaidCount(), restaurant.getRevenueCents(),
                restaurant.getAverageSatisfactionOfPaid());
    }

    public GameResult evaluate(int paidCustomers, int revenueCents, OptionalDouble averageSatisfaction) {
        boolean enoughPaid = paidCustomers >= minPaidCustomers;
        boolean enoughRevenue = revenueCents >= minRevenueCents;
        boolean happyEnough = averageSatisfaction.isPresent()
                && averageSatisfaction.getAsDouble() >= minAverageSatisfaction;

        List<String> lines = new ArrayList<>();
        lines.add(line("Paid customers", String.valueOf(paidCustomers), ">= " + minPaidCustomers, enoughPaid));
        lines.add(line("Revenue", Money.format(revenueCents), ">= " + Money.format(minRevenueCents), enoughRevenue));
        String average = averageSatisfaction.isPresent()
                ? String.format("%.1f", averageSatisfaction.getAsDouble()) : "N/A";
        lines.add(line("Average satisfaction of paid customers", average, ">= " + minAverageSatisfaction, happyEnough));
        return new GameResult(enoughPaid && enoughRevenue && happyEnough, lines);
    }

    private static String line(String name, String value, String target, boolean met) {
        return name + ": " + value + " (target " + target + ") " + (met ? "MET" : "NOT MET");
    }
}
