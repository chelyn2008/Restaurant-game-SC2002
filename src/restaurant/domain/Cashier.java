package restaurant.domain;

import java.util.Comparator;

/**
 * The Cashier works automatically: each turn it takes payment from at most one
 * customer who became READY_TO_PAY on an earlier turn, oldest first.
 */
public class Cashier extends Staff {

    public Cashier() {
        super("Cashier");
    }

    /** Collects one payment if someone can pay; fails (with a reason) if no one can. */
    public ActionResult collectPayment(Restaurant restaurant, int turn) {
        if (!isAvailable()) {
            return ActionResult.fail(busyReason());
        }
        Customer next = restaurant.getCustomers().stream()
                .filter(customer -> canPay(customer, turn))
                .min(Comparator.comparingInt(Customer::getReadyToPayOnTurn)
                        .thenComparingInt(Customer::getArrivalTurn))
                .orElse(null);
        if (next == null) {
            return ActionResult.fail("No customer is ready to pay.");
        }
        int bill = next.getOrder().getPriceCents();
        int tableNumber = next.getTable().getNumber();
        restaurant.recordPayment(next);
        completeTask();
        return ActionResult.ok("collects " + Money.format(bill) + " from " + next.getId()
                + " (satisfaction " + next.getSatisfactionAtPayment() + " recorded). Table " + tableNumber + " is free.");
    }

    /** A customer who became READY_TO_PAY this turn pays next turn at the earliest. */
    private boolean canPay(Customer customer, int turn) {
        return customer.getStatus() == CustomerStatus.READY_TO_PAY && customer.getReadyToPayOnTurn() < turn;
    }
}
