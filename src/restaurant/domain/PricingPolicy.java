package restaurant.domain;

import java.util.List;
import java.util.StringJoiner;

/**
 * Computes the price locked into an order when it is taken. Discounts stack in a fixed
 * order: first the customer's own bill (e.g. VIP 10% off), then every active promotion
 * (e.g. Happy Hour 20% off), rounding to the nearest cent at each step.
 * Example: VIP Burger during Happy Hour = $15.00 x 0.9 x 0.8 = $10.80.
 */
public class PricingPolicy {
    private final List<Promotion> promotions;

    public PricingPolicy(List<Promotion> promotions) {
        this.promotions = List.copyOf(promotions);
    }

    public int lockedPrice(Customer customer, MenuItem item, int turn) {
        int price = customer.calculateBill(item);
        for (Promotion promotion : promotions) {
            if (promotion.isActive(turn)) {
                price = promotion.applyDiscount(price);
            }
        }
        return price;
    }

    /** Which discounts apply, e.g. "VIP + Happy Hour"; empty when the menu price applies. */
    public String describeDiscounts(Customer customer, MenuItem item, int turn) {
        StringJoiner discounts = new StringJoiner(" + ");
        if (customer.calculateBill(item) != item.getPriceCents()) {
            discounts.add(customer.getTypeName());
        }
        for (Promotion promotion : promotions) {
            if (promotion.isActive(turn)) {
                discounts.add(promotion.getName());
            }
        }
        return discounts.toString();
    }
}
