package restaurant;

import restaurant.domain.Customer;
import restaurant.domain.FoodChoiceStrategy;
import restaurant.domain.menu.Menu;
import restaurant.domain.menu.MenuItem;

/** Test strategy: every customer orders the same dish. */
public class FixedFoodChoice implements FoodChoiceStrategy {
    private final String dishName;

    public FixedFoodChoice(String dishName) {
        this.dishName = dishName;
    }

    @Override
    public MenuItem choose(Customer customer, Menu menu) {
        return menu.findItem(dishName);
    }
}
