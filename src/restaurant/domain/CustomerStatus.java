package restaurant.domain;


public enum CustomerStatus {
    WAITING,       // in the waiting queue
    SEATED,        // at a table, has not ordered yet
    ORDERED,       // order placed, food not served yet
    SERVED,        // eating
    READY_TO_PAY,  // finished eating, waiting for the Cashier
    PAID,          // paid and left happily
    LEFT           // satisfaction reached 0, left without paying
}
