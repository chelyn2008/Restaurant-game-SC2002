package restaurant.domain;

import java.util.List;
import java.util.Random;

/**
 * Picks a menu item uniformly at random using a fixed seed, so the same seed
 * and the same manager inputs always give the same choices.
 */
public class SeededRandomFoodChoice implements FoodChoiceStrategy {
    private final Random random;

    public SeededRandomFoodChoice(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public MenuItem choose(Customer customer, Menu menu) {
        List<MenuItem> items = menu.getItems();
        return items.get(random.nextInt(items.size()));
    }
}
