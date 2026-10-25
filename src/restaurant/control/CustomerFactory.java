package restaurant.control;

import restaurant.domain.Customer;

/**
 * Creates one kind of customer. A schedule stores constructor references such as
 * {@code VIPCustomer::new}, so arrivals can create any type without type checks.
 */
@FunctionalInterface
public interface CustomerFactory {
    Customer create(String id, int arrivalTurn);
}
