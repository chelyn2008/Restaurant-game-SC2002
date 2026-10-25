package restaurant.domain;

/** How a customer picks a dish. The manager never chooses the food. */
public interface FoodChoiceStrategy {
    MenuItem choose(Customer customer, Menu menu);
}
