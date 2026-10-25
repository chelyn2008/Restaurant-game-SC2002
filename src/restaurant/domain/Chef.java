package restaurant.domain;

import java.util.ArrayList;
import java.util.List;

/** The Chef advances one order by one preparation unit per turn. */
public class Chef extends Staff {

    public Chef() {
        super("Chef");
    }

    public ActionResult prepare(Customer customer, int turn) {
        String problem = isAvailable() ? whyCannotPrepare(customer) : busyReason();
        if (problem != null) {
            return ActionResult.fail(problem);
        }
        Order order = customer.getOrder();
        order.prepareOneUnit(turn);
        completeTask();
        String progress = order.getUnitsDone() + "/" + order.getItem().getPrepUnits();
        if (order.isReady()) {
            return ActionResult.ok("finishes " + customer.getId() + "'s " + order.getItem().getName()
                    + " (" + progress + "): READY.");
        }
        return ActionResult.ok("prepares " + customer.getId() + "'s " + order.getItem().getName() + " (" + progress + ").");
    }

    /** The orders the Chef could work on right now, written as commands, e.g. "cook C1". */
    public List<String> availableTasks(Restaurant restaurant) {
        List<String> tasks = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            if (whyCannotPrepare(customer) == null) {
                tasks.add("cook " + customer.getId());
            }
        }
        return tasks;
    }

    private String whyCannotPrepare(Customer customer) {
        if (customer.getStatus() != CustomerStatus.ORDERED) {
            return customer.getId() + " has no order in the kitchen (status " + customer.getStatus() + ").";
        }
        if (!customer.getOrder().needsPreparation()) {
            return customer.getId() + "'s " + customer.getOrder().getItem().getName() + " is already READY.";
        }
        return null;
    }
}
