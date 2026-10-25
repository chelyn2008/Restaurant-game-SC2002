package restaurant.ui;

import java.util.List;

import restaurant.control.ChefCommand;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.WaiterCommand;
import restaurant.domain.Menu;
import restaurant.domain.Restaurant;

/**
 * Everything the game engine needs from a user interface. The UI only collects the
 * manager's decisions and displays state; it does not apply game rules.
 */
public interface GameUI {

    void showWelcome(GameConfig config, Menu menu);

    void showTurnHeader(int turn, int totalTurns);

    /** The start-of-turn display, including the tasks each staff member can do now. */
    void showTurnStart(Restaurant restaurant, List<String> waiterTasks, List<String> chefTasks);

    WaiterCommand askWaiterCommand();

    ChefCommand askChefCommand();

    void showMessage(String message);

    void showTurnResults(int turn, int totalTurns, Restaurant restaurant);

    void showFinalResult(GameResult result, Restaurant restaurant);
}
