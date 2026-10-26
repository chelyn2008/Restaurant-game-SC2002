package restaurant.domain;


public enum OrderStatus {
    PLACED,          // taken by the Waiter, no preparation yet
    IN_PREPARATION,  // some but not all preparation units done
    READY,           // all units done, waiting to be served
    SERVED,
    PAID,
    CANCELLED        // the customer left before being served
}
