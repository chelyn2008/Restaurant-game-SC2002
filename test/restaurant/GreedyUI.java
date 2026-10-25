package restaurant;

import java.util.ArrayList;
import java.util.List;

import restaurant.control.ChefCommand;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.WaiterCommand;
import restaurant.domain.Menu;
import restaurant.domain.Restaurant;
import restaurant.ui.GameUI;

/**
 * A GameUI that plays as fast as possible: the Waiter serves before taking orders
 * before seating, and the Chef cooks the oldest unfinished order. It records the
 * commands it chose so a run can be saved as an input script.
 */
public class GreedyUI implements GameUI {
    private final List<String> chosenCommands = new ArrayList<>();
    private List<String> waiterTasks = List.of();
    private List<String> chefTasks = List.of();

    public List<String> getChosenCommands() {
        return chosenCommands;
    }

    @Override
    public void showTurnStart(Restaurant restaurant, List<String> waiterTasks, List<String> chefTasks) {
        this.waiterTasks = waiterTasks;
        this.chefTasks = chefTasks;
    }

    @Override
    public WaiterCommand askWaiterCommand() {
        String choice = firstStartingWith("serve ");
        if (choice == null) {
            choice = firstStartingWith("order ");
        }
        if (choice == null) {
            choice = firstStartingWith("seat ");
        }
        return WaiterCommand.parse(record(choice == null ? "wait" : choice));
    }

    @Override
    public ChefCommand askChefCommand() {
        // The Waiter may have just taken an order, so recompute from the commands chosen this turn.
        return ChefCommand.parse(record(chefTasks.isEmpty() ? pendingOrderFromWaiter() : chefTasks.get(0)));
    }

    private String pendingOrderFromWaiter() {
        String last = chosenCommands.get(chosenCommands.size() - 1);
        return last.startsWith("order ") ? "cook " + last.substring("order ".length()) : "wait";
    }

    private String firstStartingWith(String prefix) {
        return waiterTasks.stream().filter(task -> task.startsWith(prefix)).findFirst().orElse(null);
    }

    private String record(String command) {
        chosenCommands.add(command);
        return command;
    }

    @Override
    public void showWelcome(GameConfig config, Menu menu) {
    }

    @Override
    public void showTurnHeader(int turn, int totalTurns) {
    }

    @Override
    public void showMessage(String message) {
    }

    @Override
    public void showTurnResults(int turn, int totalTurns, Restaurant restaurant) {
    }

    @Override
    public void showFinalResult(GameResult result, Restaurant restaurant) {
    }
}
