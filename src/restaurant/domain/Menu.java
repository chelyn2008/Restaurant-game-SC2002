package restaurant.domain;

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
