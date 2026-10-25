package restaurant.domain;

/**
 * A restaurant customer. The service flow (queue, seat, order, serve, eat, pay)
 * is the same for every customer; subclasses change behaviour by overriding the
 * hook methods such as {@link #getSatisfactionLossPerTurn()}.
 *
 * State only changes through package-private methods called by the staff and the
 * Restaurant, which check the game rules first.
 */
public abstract class Customer {
    public static final int MAX_SATISFACTION = 100;
    private static final int NOT_YET = -1;

    private final String id;
    private final int arrivalTurn;
    private int satisfaction = MAX_SATISFACTION;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Table table;
    private Order order;
    private int servedOnTurn = NOT_YET;
    private int readyToPayOnTurn = NOT_YET;
    private int satisfactionAtPayment = NOT_YET;

    protected Customer(String id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    /** Satisfaction points lost for each turn spent waiting (in the queue, at a table, or for food). */
    public abstract int getSatisfactionLossPerTurn();

    /** Label shown in the UI, e.g. "Regular". */
    public abstract String getTypeName();

    /** The bill for a dish. Full price by default. */
    public int calculateBill(MenuItem item) {
        return item.getPriceCents();
    }

    /** Picks the dish when the order is taken. By default the customer follows the game's food-choice rule. */
    public MenuItem chooseDish(Menu menu, FoodChoiceStrategy defaultChoice) {
        return defaultChoice.choose(this, menu);
    }

    /** True while the customer is still waiting for food: in the queue, seated or ordered. */
    public boolean isWaitingForService() {
        return status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED
                || status == CustomerStatus.ORDERED;
    }

    public boolean hasBeenServed() {
        return servedOnTurn != NOT_YET;
    }

    void loseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - getSatisfactionLossPerTurn());
    }

    void recoverSatisfaction(int points) {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + points);
    }

    void seatAt(Table table) {
        requireStatus(CustomerStatus.WAITING);
        this.table = table;
        status = CustomerStatus.SEATED;
    }

    void placeOrder(Order order) {
        requireStatus(CustomerStatus.SEATED);
        this.order = order;
        status = CustomerStatus.ORDERED;
    }

    void markServed(int turn) {
        requireStatus(CustomerStatus.ORDERED);
        order.markServed();
        servedOnTurn = turn;
        status = CustomerStatus.SERVED;
    }

    void finishEating(int turn) {
        requireStatus(CustomerStatus.SERVED);
        readyToPayOnTurn = turn;
        status = CustomerStatus.READY_TO_PAY;
    }

    void markPaid() {
        requireStatus(CustomerStatus.READY_TO_PAY);
        order.markPaid();
        satisfactionAtPayment = satisfaction;
        table = null;
        status = CustomerStatus.PAID;
    }

    void leave() {
        if (!isWaitingForService()) {
            throw new IllegalStateException(id + " has been served and does not leave unhappy.");
        }
        if (order != null) {
            order.cancel();
        }
        table = null;
        status = CustomerStatus.LEFT;
    }

    private void requireStatus(CustomerStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(id + " is " + status + ", expected " + expected);
        }
    }

    public String getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    /** The customer's table, or null if not seated. */
    public Table getTable() {
        return table;
    }

    /** The customer's order, or null if none has been placed. */
    public Order getOrder() {
        return order;
    }

    public int getServedOnTurn() {
        return servedOnTurn;
    }

    public int getReadyToPayOnTurn() {
        return readyToPayOnTurn;
    }

    /** Satisfaction recorded when the customer paid, or -1 if not paid. */
    public int getSatisfactionAtPayment() {
        return satisfactionAtPayment;
    }

    @Override
    public String toString() {
        return id;
    }
}
