package restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import restaurant.domain.menu.Menu;

/**
 * composed of its tables, waiting queue, staff, menu and pricing policy,
 * plus (in Stage 3) a Host and the Happy Hour promotion. It keeps the revenue and applies
 * the turn-level rules for eating, waiting and Happy Hour recovery
 */
public class Restaurant {
    private final Menu menu;
    private final List<Table> tables = new ArrayList<>();
    private final List<Customer> waitingQueue = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final Host host;            // null when this game has no Host
    private final HappyHour happyHour;  // null when this game offers no Happy Hour
    private final PricingPolicy pricingPolicy;
    private int revenueCents;

    /** A restaurant without a Host or Happy Hour (Stages 1 and 2) */
    public Restaurant(Menu menu, int tableCount) {
        this(menu, tableCount, false, false);
    }

    public Restaurant(Menu menu, int tableCount, boolean withHost, boolean withHappyHour) {
        this.menu = menu;
        for (int number = 1; number <= tableCount; number++) {
            tables.add(new Table(number));
        }
        this.host = withHost ? new Host() : null;
        this.happyHour = withHappyHour ? new HappyHour() : null;
        this.pricingPolicy = new PricingPolicy(happyHour == null ? List.of() : List.of(happyHour));
    }

    /** Lets every staff member act again at each turn */
    public void startNewTurn() {
        for (Staff staff : getStaff()) {
            staff.resetForNewTurn();
        }
    }

    /** A newly arrived customer joins the end of the waiting queue */
    public void admitCustomer(Customer customer) {
        if (customer.getStatus() != CustomerStatus.WAITING || customers.contains(customer)) {
            throw new IllegalArgumentException(customer.getId() + " cannot join the queue.");
        }
        customers.add(customer);
        waitingQueue.add(customer);
    }

    /**
     * Turn start, while Happy Hour is active: every customer still waiting for a table or food
     * recovers 15 satisfaction (maximum 100). Returns the customers who recovered.
     */
    public List<Customer> applyHappyHourRecovery(int turn) {
        List<Customer> recovered = new ArrayList<>();
        if (happyHour == null || !happyHour.isActive(turn)) {
            return recovered;
        }
        for (Customer customer : customers) {
            if (customer.isWaitingForService()) {
                customer.recoverSatisfaction(HappyHour.RECOVERY_POINTS);
                recovered.add(customer);
            }
        }
        return recovered;
    }

    /** Customers who were served on an earlier turn finish eating and become READY_TO_PAY */
    public List<Customer> finishEating(int turn) {
        List<Customer> finished = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getStatus() == CustomerStatus.SERVED && customer.getServedOnTurn() < turn) {
                customer.finishEating(turn);
                finished.add(customer);
            }
        }
        return finished;
    }

    /**
     * End of turn: every customer still waiting for food loses satisfaction.
     * Those who reach 0 leave without paying. Returns the customers who left.
     */
    public List<Customer> applyWaitingPenalties() {
        List<Customer> departed = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.isWaitingForService()) {
                customer.loseSatisfaction();
                if (customer.getSatisfaction() == 0) {
                    removeUnhappyCustomer(customer);
                    departed.add(customer);
                }
            }
        }
        return departed;
    }

    /** Why this customer cannot be seated now (shared by Waiter and Host), or null if they can. */
    String whyCannotSeat(Customer customer) {
        if (customer.getStatus() != CustomerStatus.WAITING) {
            return customer.getId() + " is not in the waiting queue (status " + customer.getStatus() + ").";
        }
        if (findFreeTable() == null) {
            return "No table is free. A table is freed only when its customer pays or leaves.";
        }
        return null;
    }

    void seatCustomer(Customer customer, Table table) {
        waitingQueue.remove(customer);
        table.assign(customer);
        customer.seatAt(table);
    }

    void recordPayment(Customer customer) {
        Table table = customer.getTable();
        revenueCents += customer.getOrder().getPriceCents();
        customer.markPaid();
        table.release();
    }

    private void removeUnhappyCustomer(Customer customer) {
        waitingQueue.remove(customer);
        Table table = customer.getTable();
        if (table != null) {
            table.release();
        }
        customer.leave();
    }

    /** Returns the customer with this id (e.g. "C2"), or null if there is none */
    public Customer findCustomer(String id) {
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id)) {
                return customer;
            }
        }
        return null;
    }

    /** The lowest-numbered free table, or null if all are occupied */
    public Table findFreeTable() {
        for (Table table : tables) {
            if (table.isFree()) {
                return table;
            }
        }
        return null;
    }

    public Menu getMenu() {
        return menu;
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<Customer> getWaitingQueue() {
        return Collections.unmodifiableList(waitingQueue);
    }

    /** All customers who have arrived so far, in arrival order */
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public Chef getChef() {
        return chef;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public Optional<Host> getHost() {
        return Optional.ofNullable(host);
    }

    public Optional<HappyHour> getHappyHour() {
        return Optional.ofNullable(happyHour);
    }

    public PricingPolicy getPricingPolicy() {
        return pricingPolicy;
    }

    public List<Staff> getStaff() {
        List<Staff> staff = new ArrayList<>(List.of(waiter, chef, cashier));
        if (host != null) {
            staff.add(host);
        }
        return staff;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return countWithStatus(CustomerStatus.PAID);
    }

    public int getUnhappyDepartures() {
        return countWithStatus(CustomerStatus.LEFT);
    }

    public int getServedCount() {
        return (int) customers.stream().filter(Customer::hasBeenServed).count();
    }

    /** Average of the satisfaction recorded at payment; empty if no one has paid */
    public OptionalDouble getAverageSatisfactionOfPaid() {
        return customers.stream()
                .filter(customer -> customer.getStatus() == CustomerStatus.PAID)
                .mapToInt(Customer::getSatisfactionAtPayment)
                .average();
    }

    private int countWithStatus(CustomerStatus status) {
        return (int) customers.stream().filter(customer -> customer.getStatus() == status).count();
    }
}
