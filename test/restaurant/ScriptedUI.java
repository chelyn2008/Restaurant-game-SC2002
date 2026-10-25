package restaurant;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import restaurant.control.ChefCommand;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.WaiterCommand;
import restaurant.domain.Menu;
import restaurant.domain.Restaurant;
import restaurant.ui.GameUI;

/**
 * A GameUI for tests: answers prompts from a fixed list of lines (the same text a
 * player would type) and records every message instead of printing it.
 * When the lines run out, staff wait.
 */
public class ScriptedUI implements GameUI {
    private final Deque<String> lines;
    private final List<String> messages = new ArrayList<>();
    private List<String> lastWaiterTasks = List.of();

    public ScriptedUI(String... lines) {
        this.lines = new ArrayDeque<>(List.of(lines));
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getLastWaiterTasks() {
        return lastWaiterTasks;
    }

    public boolean hasMessageContaining(String text) {
        return messages.stream().anyMatch(message -> message.contains(text));
    }

    @Override
    public void showWelcome(GameConfig config, Menu menu) {
    }

    @Override
    public void showTurnHeader(int turn, int totalTurns) {
    }

    @Override
    public void showTurnStart(Restaurant restaurant, List<String> waiterTasks, List<String> chefTasks) {
        lastWaiterTasks = waiterTasks;
    }

    @Override
    public WaiterCommand askWaiterCommand() {
        while (!lines.isEmpty()) {
            try {
                return WaiterCommand.parse(lines.poll());
            } catch (IllegalArgumentException e) {
                messages.add("Invalid input: " + e.getMessage());
            }
        }
        return WaiterCommand.WAIT;
    }

    @Override
    public ChefCommand askChefCommand() {
        while (!lines.isEmpty()) {
            try {
                return ChefCommand.parse(lines.poll());
            } catch (IllegalArgumentException e) {
                messages.add("Invalid input: " + e.getMessage());
            }
        }
        return ChefCommand.WAIT;
    }

    @Override
    public void showMessage(String message) {
        messages.add(message);
    }

    @Override
    public void showTurnResults(int turn, int totalTurns, Restaurant restaurant) {
    }

    @Override
    public void showFinalResult(GameResult result, Restaurant restaurant) {
    }
}
