package restaurant.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * The Host seats at most one queued customer per turn, in addition to the Waiter's task.
 * The Host acts before the Waiter, so the Waiter can take that customer's order in the same turn.
 */
public class Host extends Staff {

    public Host() {
        super("Host");
    }

    public ActionResult seat(Restaurant restaurant, Customer customer) {
        String problem = isAvailable() ? restaurant.whyCannotSeat(customer) : busyReason();
        if (problem != null) {
            return ActionResult.fail(problem);
        }
        Table table = restaurant.findFreeTable();
        restaurant.seatCustomer(customer, table);
        completeTask();
        return ActionResult.ok("seats " + customer.getId() + " at Table " + table.getNumber() + ".");
    }

    /** The customers the Host could seat right now, written as commands, e.g. "seat C2". */
    public List<String> availableTasks(Restaurant restaurant) {
        List<String> tasks = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            if (restaurant.whyCannotSeat(customer) == null) {
                tasks.add("seat " + customer.getId());
            }
        }
        return tasks;
    }
}
