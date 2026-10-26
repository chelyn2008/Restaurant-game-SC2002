package restaurant.domain;

import restaurant.domain.menu.Menu;
import restaurant.domain.menu.MenuItem;

/** How a customer picks a dish. The manager never chooses the food. */
public interface FoodChoiceStrategy {
    MenuItem choose(Customer customer, Menu menu);
}
