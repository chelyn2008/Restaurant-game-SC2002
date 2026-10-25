package restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

/**
 * The restaurant is composed of its tables, waiting queue, staff and menu.
 * It keeps the revenue and applies the turn-level rules for eating and waiting.
 */
public class Restaurant {
    private final Menu menu;
    private final List<Table> tables = new ArrayList<>();
    private final List<Customer> waitingQueue = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private int revenueCents;

    public Restaurant(Menu menu, int tableCount) {
        this.menu = menu;
        for (int number = 1; number <= tableCount; number++) {
            tables.add(new Table(number));
        }
    }

    /** Lets every staff member act again. */
    public void startNewTurn() {
        for (Staff staff : getStaff()) {
            staff.resetForNewTurn();
        }
    }

    /** A newly arrived customer joins the end of the waiting queue. */
    public void admitCustomer(Customer customer) {
        if (customer.getStatus() != CustomerStatus.WAITING || customers.contains(customer)) {
            throw new IllegalArgumentException(customer.getId() + " cannot join the queue.");
        }
        customers.add(customer);
        waitingQueue.add(customer);
    }

    /** Customers who were served on an earlier turn finish eating and become READY_TO_PAY. */
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

    /** Returns the customer with this id (e.g. "C2"), or null if there is none. */
    public Customer findCustomer(String id) {
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id)) {
                return customer;
            }
        }
        return null;
    }

    /** The lowest-numbered free table, or null if all are occupied. */
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

    /** All customers who have arrived so far, in arrival order. */
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

    public List<Staff> getStaff() {
        return List.of(waiter, chef, cashier);
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

    /** Average of the satisfaction recorded at payment; empty if no one has paid. */
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
