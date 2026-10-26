package restaurant;

import java.util.ArrayList;
import java.util.List;

import restaurant.control.ChefCommand;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.HostCommand;
import restaurant.control.WaiterCommand;
import restaurant.domain.Restaurant;
import restaurant.domain.menu.Menu;
import restaurant.ui.GameUI;

/**
 * A GameUI that plays as fast as possible: the Host seats whoever it can, the Waiter
 * serves before taking orders before seating, and the Chef cooks the oldest unfinished
 * order. Happy Hour is never used. It records the commands it chose so a run can be
 * saved as an input script.
 */
public class GreedyUI implements GameUI {
    private final List<String> chosenCommands = new ArrayList<>();
    private Restaurant restaurant;
    private int turn;
    private List<String> hostTasks = List.of();
    private List<String> chefTasks = List.of();

    public List<String> getChosenCommands() {
        return chosenCommands;
    }

    @Override
    public void showTurnStart(Restaurant restaurant, int turn, List<String> hostTasks,
                              List<String> waiterTasks, List<String> chefTasks) {
        this.restaurant = restaurant;
        this.turn = turn;
        this.hostTasks = hostTasks;
        this.chefTasks = chefTasks;
    }

    @Override
    public boolean askHappyHour() {
        record("no");
        return false;
    }

    @Override
    public HostCommand askHostCommand() {
        return HostCommand.parse(record(hostTasks.isEmpty() ? "wait" : hostTasks.get(0)));
    }

    @Override
    public WaiterCommand askWaiterCommand() {
        // Read the Waiter's tasks now, because the Host may have just seated someone.
        List<String> waiterTasks = restaurant.getWaiter().availableTasks(restaurant, turn);
        String choice = firstStartingWith(waiterTasks, "serve ");
        if (choice == null) {
            choice = firstStartingWith(waiterTasks, "order ");
        }
        if (choice == null) {
            choice = firstStartingWith(waiterTasks, "seat ");
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

    private static String firstStartingWith(List<String> tasks, String prefix) {
        return tasks.stream().filter(task -> task.startsWith(prefix)).findFirst().orElse(null);
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
