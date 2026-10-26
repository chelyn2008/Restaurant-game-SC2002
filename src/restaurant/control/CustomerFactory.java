package restaurant.control;

import restaurant.domain.Customer;

/** Creates a customer; lets ArrivalSchedule store {@code RegularCustomer::new} etc. and spawn any type without type checks */
@FunctionalInterface
public interface CustomerFactory {
    Customer create(String id, int arrivalTurn);
}
