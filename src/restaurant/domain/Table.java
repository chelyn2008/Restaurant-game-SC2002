package restaurant.domain;

/** A table that holds at most one customer until that customer pays or leaves. */
public class Table {
    private final int number;
    private Customer occupant;

    Table(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    /** The customer at this table, or null if it is free. */
    public Customer getOccupant() {
        return occupant;
    }

    public boolean isFree() {
        return occupant == null;
    }

    void assign(Customer customer) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + number + " is already occupied.");
        }
        occupant = customer;
    }

    void release() {
        occupant = null;
    }
}
