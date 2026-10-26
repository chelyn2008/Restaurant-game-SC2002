package restaurant.domain;

import restaurant.domain.menu.MenuItem;

/**
 * One customer's order. The price is locked when the order is taken.
 * The Chef advances it one preparation unit at a time until it is READY.
 */
public class Order {
    private static final int NOT_YET = -1;

    private final Customer customer;
    private final MenuItem item;
    private final int priceCents;
    private final int takenOnTurn;
    private int unitsDone;
    private OrderStatus status = OrderStatus.PLACED;
    private int readyOnTurn = NOT_YET;

    Order(Customer customer, MenuItem item, int priceCents, int takenOnTurn) {
        this.customer = customer;
        this.item = item;
        this.priceCents = priceCents;
        this.takenOnTurn = takenOnTurn;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getTakenOnTurn() {
        return takenOnTurn;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /** The turn on which the last preparation unit was done, or -1 if not READY yet */
    public int getReadyOnTurn() {
        return readyOnTurn;
    }

    public boolean needsPreparation() {
        return status == OrderStatus.PLACED || status == OrderStatus.IN_PREPARATION;
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    void prepareOneUnit(int turn) {
        if (!needsPreparation()) {
            throw new IllegalStateException("Order for " + customer.getId() + " needs no preparation.");
        }
        unitsDone++;
        if (unitsDone == item.getPrepUnits()) {
            status = OrderStatus.READY;
            readyOnTurn = turn;
        } else {
            status = OrderStatus.IN_PREPARATION;
        }
    }

    void markServed() {
        requireStatus(OrderStatus.READY);
        status = OrderStatus.SERVED;
    }

    void markPaid() {
        requireStatus(OrderStatus.SERVED);
        status = OrderStatus.PAID;
    }

    void cancel() {
        if (status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            throw new IllegalStateException("A served order cannot be cancelled.");
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Order for " + customer.getId() + " is " + status + ", expected " + expected);
        }
    }

    /** Short description for display, e.g. "Pasta $18.00 IN_PREPARATION 1/2". */
    public String describe() {
        return item.getName() + " " + Money.format(priceCents) + " " + status
                + " " + unitsDone + "/" + item.getPrepUnits();
    }
}
