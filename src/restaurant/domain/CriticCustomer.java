package restaurant.domain;

import java.util.Comparator;
import restaurant.domain.menu.Menu;
import restaurant.domain.menu.MenuItem;

/**
 * A food critic: loses 12 satisfaction per waiting turn and pays full price.
 * Special behaviour : the Critic always orders the most expensive food
                       Pasta in the base menu instead of using the food-choice rule
 */
public class CriticCustomer extends Customer {
    private static final int SATISFACTION_LOSS = 12;

    public CriticCustomer(String id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    /** The most expensive dish; on a price tie, the one listed first. */
    @Override
    public MenuItem chooseDish(Menu menu, FoodChoiceStrategy defaultChoice) {
        return menu.getItems().stream()
                .max(Comparator.comparingInt(MenuItem::getPriceCents))
                .orElseThrow();
    }
}
