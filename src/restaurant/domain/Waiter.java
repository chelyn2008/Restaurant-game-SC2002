package restaurant.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * The Waiter seats a queued customer, takes a seated customer's order, or serves a READY order.
 * Each check returns a reason when the task is not allowed, so the manager can choose again.
 */
public class Waiter extends Staff {

    public Waiter() {
        super("Waiter");
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

    /** Takes the order. The customer chooses the dish; the bill is locked now. */
    public ActionResult takeOrder(Restaurant restaurant, Customer customer, FoodChoiceStrategy foodChoice, int turn) {
        String problem = isAvailable() ? whyCannotTakeOrder(customer) : busyReason();
        if (problem != null) {
            return ActionResult.fail(problem);
        }
        MenuItem item = customer.chooseDish(restaurant.getMenu(), foodChoice);
        PricingPolicy pricing = restaurant.getPricingPolicy();
        int bill = pricing.lockedPrice(customer, item, turn);
        customer.placeOrder(new Order(customer, item, bill, turn));
        completeTask();
        String discounts = pricing.describeDiscounts(customer, item, turn);
        String billNote = discounts.isEmpty() ? "" : " " + discounts + " bill: " + Money.format(bill) + ".";
        return ActionResult.ok("takes " + customer.getId() + "'s order. " + customer.getId() + " chooses " + item + "." + billNote);
    }

    public ActionResult serve(Customer customer, int turn) {
        String problem = isAvailable() ? whyCannotServe(customer, turn) : busyReason();
        if (problem != null) {
            return ActionResult.fail(problem);
        }
        customer.markServed(turn);
        completeTask();
        return ActionResult.ok("serves " + customer.getId() + "'s " + customer.getOrder().getItem().getName() + ".");
    }

    /** The tasks the Waiter could do right now, written as commands, e.g. "seat C2". */
    public List<String> availableTasks(Restaurant restaurant, int turn) {
        List<String> tasks = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            if (restaurant.whyCannotSeat(customer) == null) {
                tasks.add("seat " + customer.getId());
            }
            if (whyCannotTakeOrder(customer) == null) {
                tasks.add("order " + customer.getId());
            }
            if (whyCannotServe(customer, turn) == null) {
                tasks.add("serve " + customer.getId());
            }
        }
        return tasks;
    }

    private String whyCannotTakeOrder(Customer customer) {
        if (customer.getStatus() == CustomerStatus.WAITING) {
            return customer.getId() + " must be seated before ordering.";
        }
        if (customer.getStatus() != CustomerStatus.SEATED) {
            return customer.getId() + " has already ordered or has left (status " + customer.getStatus() + ").";
        }
        return null;
    }

    private String whyCannotServe(Customer customer, int turn) {
        Order order = customer.getOrder();
        if (customer.getStatus() != CustomerStatus.ORDERED) {
            return customer.getId() + " has no order waiting to be served (status " + customer.getStatus() + ").";
        }
        if (!order.isReady()) {
            return customer.getId() + "'s " + order.getItem().getName() + " is not READY yet ("
                    + order.getUnitsDone() + "/" + order.getItem().getPrepUnits() + " prep units done).";
        }
        if (order.getReadyOnTurn() >= turn) {
            return customer.getId() + "'s " + order.getItem().getName()
                    + " only became READY this turn; it can be served next turn.";
        }
        return null;
    }
}
