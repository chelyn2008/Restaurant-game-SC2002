package restaurant.domain;

import java.util.ArrayList;
import java.util.List;

/** The list of dishes customers can choose from. */
public class Menu {
    private final List<MenuItem> items;

    public Menu(List<MenuItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A menu needs at least one item.");
        }
        this.items = List.copyOf(items);
    }

    /** The base-setting menu: Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units). */
    public static Menu standard() {
        return new Menu(List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2)));
    }

    /**
     * The Stage 3 menu: the three base dishes plus every combo of two different dishes
     * (Salad + Burger $21.60, Salad + Pasta $24.30, Burger + Pasta $29.70).
     */
    public static Menu withCombos() {
        List<MenuItem> items = new ArrayList<>(standard().getItems());
        MenuItem salad = items.get(0);
        MenuItem burger = items.get(1);
        MenuItem pasta = items.get(2);
        items.add(new ComboMeal(salad, burger));
        items.add(new ComboMeal(salad, pasta));
        items.add(new ComboMeal(burger, pasta));
        return new Menu(items);
    }

    public List<MenuItem> getItems() {
        return items;
    }

    /** Returns the item with the given name (case-insensitive), or null if there is none. */
    public MenuItem findItem(String name) {
        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
}
