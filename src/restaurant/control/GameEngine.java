package restaurant.control;

import java.util.List;
import java.util.StringJoiner;
import restaurant.domain.ActionResult;
import restaurant.domain.Chef;
import restaurant.domain.Customer;
import restaurant.domain.FoodChoiceStrategy;
import restaurant.domain.HappyHour;
import restaurant.domain.Host;
import restaurant.domain.Restaurant;
import restaurant.domain.ServiceAdvisor;
import restaurant.domain.Waiter;
import restaurant.ui.GameUI;

/**
 Runs the game: every turn goes through the six steps in order, and the game
 always lasts the full number of turns. The engine talks to the player only
 through the {@link GameUI} interface.
 */
public class GameEngine {
    private final GameConfig config;
    private final GameUI ui;
    private final FoodChoiceStrategy foodChoice;
    private final Restaurant restaurant;
    private final ArrivalSchedule arrivals;
    private final VictoryCondition victoryCondition;
    private final ServiceAdvisor advisor = new ServiceAdvisor();

    public GameEngine(GameConfig config, GameUI ui, FoodChoiceStrategy foodChoice) {
        this.config = config;
        this.ui = ui;
        this.foodChoice = foodChoice;
        this.restaurant = new Restaurant(config.menu(), config.tableCount(),
                config.hostEnabled(), config.happyHourEnabled());
        this.arrivals = config.arrivalSchedule();
        this.victoryCondition = new VictoryCondition(config);
    }

    // Plays every turn and returns the final result
    public GameResult run() {
        ui.showWelcome(config, restaurant.getMenu());
        for (int turn = 1; turn <= config.totalTurns(); turn++) {
            playTurn(turn);
        }
        GameResult result = victoryCondition.evaluate(restaurant);
        ui.showFinalResult(result, restaurant);
        return result;
    }

    // Plays one turn (steps 1-5)
    public void playTurn(int turn) {
        restaurant.startNewTurn();
        ui.showTurnHeader(turn, config.totalTurns());

        // Step 1: arrivals
        arrivals.customerArrivingOn(turn).ifPresent(customer -> {
            restaurant.admitCustomer(customer);
            ui.showMessage(customer.getId() + " (" + customer.getTypeName()
                    + ") arrives and joins the waiting queue with satisfaction " + customer.getSatisfaction() + ".");
        });

        // Turn start (Stage 3): the manager may switch on Happy Hour, then active turns give recovery
        restaurant.getHappyHour().filter(HappyHour::isAvailable).ifPresent(happyHour -> offerHappyHour(happyHour, turn));
        reportRecovery(restaurant.applyHappyHourRecovery(turn));

        List<String> waiterTasks = restaurant.getWaiter().availableTasks(restaurant, turn);
        List<String> chefTasks = restaurant.getChef().availableTasks(restaurant);
        ui.showTurnStart(restaurant, turn,
                restaurant.getHost().map(host -> host.availableTasks(restaurant)).orElse(List.of()),
                waiterTasks, chefTasks);
        if (config.hintsEnabled()) {
            showLeaveWarnings(turn);
        }

        // Steps 2 and 3: manager choices; the Host acts first, then the Waiter, then the Chef.
        // An earlier action can open a new task for the next one (Host seats -> Waiter can order;
        // Waiter takes an order -> Chef can cook it), so the changed list is shown again.
        restaurant.getHost().ifPresent(host -> runHostTask(host, turn));
        List<String> waiterTasksNow = restaurant.getWaiter().availableTasks(restaurant, turn);
        if (!waiterTasksNow.equals(waiterTasks)) {
            ui.showMessage("Waiter can now: " + String.join(" | ", waiterTasksNow) + (waiterTasksNow.isEmpty() ? "wait" : " | wait"));
        }
        runWaiterTask(turn);
        List<String> chefTasksNow = restaurant.getChef().availableTasks(restaurant);
        if (!chefTasksNow.equals(chefTasks)) {
            ui.showMessage("Chef can now: " + String.join(" | ", chefTasksNow) + (chefTasksNow.isEmpty() ? "wait" : " | wait"));
        }
        runChefTask(turn);

        // Step 4: eating, then the automatic Cashier
        for (Customer customer : restaurant.finishEating(turn)) {
            ui.showMessage(customer.getId() + " finishes eating and is READY_TO_PAY.");
        }
        ActionResult payment = restaurant.getCashier().collectPayment(restaurant, turn);
        if (payment.success()) {
            ui.showMessage("Cashier " + payment.message());
        }

        // Step 5: waiting and abandonment
        for (Customer customer : restaurant.applyWaitingPenalties()) {
            ui.showMessage(customer.getId() + " reaches satisfaction 0 and leaves without paying.");
        }
        ui.showTurnResults(turn, config.totalTurns(), restaurant);
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
    private void offerHappyHour(HappyHour happyHour, int turn) {
        if (ui.askHappyHour()) {
            ActionResult result = happyHour.activate(turn);
            ui.showMessage(result.success() ? result.message() : "Invalid choice: " + result.message());
        }
    }

    private void reportRecovery(List<Customer> recovered) {
        if (recovered.isEmpty()) {
            return;
        }
        StringJoiner now = new StringJoiner(", ");
        for (Customer customer : recovered) {
            now.add(customer.getId() + " " + customer.getSatisfaction());
        }
        ui.showMessage("Happy Hour: waiting customers recover " + HappyHour.RECOVERY_POINTS
                + " (max " + Customer.MAX_SATISFACTION + "): " + now + ".");
    }

    // Extra feature (--hints): 
    // when each waiting customer leaves if they are still not served
    private void showLeaveWarnings(int turn) {
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.isWaitingForService()) {
                ui.showMessage("Warning: " + customer.getId() + " (" + customer.getTypeName() + ", satisfaction "
                        + customer.getSatisfaction() + ", -" + customer.getSatisfactionLossPerTurn()
                        + " per turn) " + describeLeaving(customer, turn) + ".");
            }
        }
    }

    // "leaves at the end of turn 9 unless served", or, when that turn is after the last turn,
    // that the customer cannot leave before the game ends
    private String describeLeaving(Customer customer, int turn) {
        int leavingTurn = advisor.leavingTurn(customer, turn);
        if (leavingTurn > config.totalTurns()) {
            return "will not leave before the game ends (turn " + config.totalTurns() + ")";
        }
        return "leaves at the end of turn " + leavingTurn + " unless served";
    }

    // Answers "hint". A hint never uses up the staff task, whether hints are on or off
    private void showHint(int turn) {
        if (!config.hintsEnabled()) {
            ui.showMessage("Invalid choice: hints are off; start the game with --hints to use them. Choose again.");
            return;
        }
        ui.showMessage(advisor.mostUrgent(restaurant)
                .map(customer -> "Hint: most urgent is " + customer.getId() + " (" + customer.getTypeName()
                        + "), who " + describeLeaving(customer, turn)
                        + ". Next step: " + advisor.nextStep(restaurant, customer) + ".")
                .orElse("Hint: no customer is waiting for service."));
    }

    // Asks until the manager gives a valid Host choice; invalid choices and hints do not use the task
    private void runHostTask(Host host, int turn) {
        while (true) {
            HostCommand command = ui.askHostCommand();
            if (command.action() == HostCommand.Action.HINT) {
                showHint(turn);
                continue;
            }
            ActionResult result = executeHostCommand(host, command);
            report("Host", result);
            if (result.success()) {
                return;
            }
        }
    }

    // Asks until the manager gives a valid Waiter choice; invalid choices and hints do not use the task
    private void runWaiterTask(int turn) {
        while (true) {
            WaiterCommand command = ui.askWaiterCommand();
            if (command.action() == WaiterCommand.Action.HINT) {
                showHint(turn);
                continue;
            }
            ActionResult result = executeWaiterCommand(command, turn);
            report("Waiter", result);
            if (result.success()) {
                return;
            }
        }
    }

    private void runChefTask(int turn) {
        while (true) {
            ChefCommand command = ui.askChefCommand();
            if (command.action() == ChefCommand.Action.HINT) {
                showHint(turn);
                continue;
            }
            ActionResult result = executeChefCommand(command, turn);
            report("Chef", result);
            if (result.success()) {
                return;
            }
        }
    }

    private ActionResult executeHostCommand(Host host, HostCommand command) {
        if (command.action() == HostCommand.Action.WAIT) {
            return host.waitThisTurn();
        }
        Customer customer = restaurant.findCustomer(command.customerId());
        if (customer == null) {
            return ActionResult.fail("There is no customer " + command.customerId() + ".");
        }
        return host.seat(restaurant, customer);
    }

    private ActionResult executeWaiterCommand(WaiterCommand command, int turn) {
        Waiter waiter = restaurant.getWaiter();
        if (command.action() == WaiterCommand.Action.WAIT) {
            return waiter.waitThisTurn();
        }
        Customer customer = restaurant.findCustomer(command.customerId());
        if (customer == null) {
            return ActionResult.fail("There is no customer " + command.customerId() + ".");
        }
        return switch (command.action()) {
            case SEAT -> waiter.seat(restaurant, customer);
            case ORDER -> waiter.takeOrder(restaurant, customer, foodChoice, turn);
            case SERVE -> waiter.serve(customer, turn);
            case WAIT -> waiter.waitThisTurn();
            case HINT -> throw new IllegalStateException("A hint is handled before any staff task.");
        };
    }

    private ActionResult executeChefCommand(ChefCommand command, int turn) {
        Chef chef = restaurant.getChef();
        if (command.action() == ChefCommand.Action.WAIT) {
            return chef.waitThisTurn();
        }
        Customer customer = restaurant.findCustomer(command.customerId());
        if (customer == null) {
            return ActionResult.fail("There is no customer " + command.customerId() + ".");
        }
        return chef.prepare(customer, turn);
    }

    private void report(String role, ActionResult result) {
        if (result.success()) {
            ui.showMessage(role + " " + result.message());
        } else {
            ui.showMessage("Invalid choice: " + result.message() + " The " + role + "'s task is not used; choose again.");
        }
    }
}
