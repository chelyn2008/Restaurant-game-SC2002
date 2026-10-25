package restaurant.control;

import java.util.List;
import java.util.Optional;

import restaurant.domain.Customer;
import restaurant.domain.RegularCustomer;

/** Creates the customer who arrives on a given turn. Customers are named C1, C2, ... in arrival order. */
public class ArrivalSchedule {
    private final List<Integer> arrivalTurns;
    private int nextCustomerNumber = 1;

    public ArrivalSchedule(List<Integer> arrivalTurns) {
        this.arrivalTurns = List.copyOf(arrivalTurns);
    }

    public Optional<Customer> customerArrivingOn(int turn) {
        if (!arrivalTurns.contains(turn)) {
            return Optional.empty();
        }
        return Optional.of(new RegularCustomer("C" + nextCustomerNumber++, turn));
    }
}
