package restaurant.domain;

/**
 * Two existing menu items sold together. Its preparation units are the sum of the
 * two components and its price is 90% of their combined price, rounded to the nearest cent.
 * Example: Salad + Pasta = ($9.00 + $18.00) x 0.9 = $24.30, 3 units.
 */
public class ComboMeal extends MenuItem {
    private static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                comboPrice(first, second),
                first.getPrepUnits() + second.getPrepUnits());
        this.first = first;
        this.second = second;
    }

    /** 90% of the combined price in cents, half a cent rounding up. */
    private static int comboPrice(MenuItem first, MenuItem second) {
        return ((first.getPriceCents() + second.getPriceCents()) * PRICE_PERCENT + 50) / 100;
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }
}
