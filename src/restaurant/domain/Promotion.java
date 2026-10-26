package restaurant.domain;

public interface Promotion {

    String getName();

    boolean isActive(int turn);

    /** The price after this promotion's discount, in integer cents */
    int applyDiscount(int priceCents);
}
