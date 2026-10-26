package restaurant.ui;

import java.util.List;
import restaurant.control.ChefCommand;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.HostCommand;
import restaurant.control.WaiterCommand;
import restaurant.domain.Restaurant;
import restaurant.domain.menu.Menu;

/**
 * Everything the game engine needs from a user interface. The UI only collects the
 * manager's decisions and displays state; it does not apply game rules.
 */
public interface GameUI {

    void showWelcome(GameConfig config, Menu menu);

    void showTurnHeader(int turn, int totalTurns);

    void showTurnStart(Restaurant restaurant, int turn, List<String> hostTasks,
                       List<String> waiterTasks, List<String> chefTasks);

    boolean askHappyHour();

    HostCommand askHostCommand();

    WaiterCommand askWaiterCommand();

    ChefCommand askChefCommand();

    void showMessage(String message);

    void showTurnResults(int turn, int totalTurns, Restaurant restaurant);

    void showFinalResult(GameResult result, Restaurant restaurant);
}
