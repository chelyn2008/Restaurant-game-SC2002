package restaurant.domain;

/**
 * A time-limited discount applied on top of the customer's own bill.
 * PricingPolicy applies every promotion that is active when the order is taken.
 */
public interface Promotion {

    String getName();

    boolean isActive(int turn);

    /** The price after this promotion's discount, in integer cents. */
    int applyDiscount(int priceCents);
}
