package restaurant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import restaurant.control.GameConfig;
import restaurant.control.GameEngine;
import restaurant.control.GameResult;
import restaurant.control.VictoryCondition;
import restaurant.domain.ActionResult;
import restaurant.domain.Customer;
import restaurant.domain.CustomerStatus;
import restaurant.domain.OrderStatus;
import restaurant.domain.RegularCustomer;
import restaurant.domain.Restaurant;
import restaurant.domain.SeededRandomFoodChoice;
import restaurant.domain.menu.Menu;
import restaurant.ui.ConsoleUI;

/**
 * Automated checks for Stage 1, using plain Java (no test library).
 * Run from the project root: java -cp out-test restaurant.StageOneChecks
 * (exit code 1 if any check fails)
 */
public class StageOneChecks {
    private static final GameConfig CONFIG = GameConfig.baseSetting(GameConfig.DEFAULT_SEED);
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        sectionThreeThreeExample();
        dishCannotBeServedOnTheTurnItBecomesReady();
        abandonmentCancelsOrderAndFreesTable();
        cashierPaysOldestFirst();
        invalidInputDoesNotUseTheTask();
        winAndLoseBoundaries();
        atMostFiveCustomersCanPay();
        savedRunsReproduce();

        System.out.println();
        System.out.println(passed + " checks passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Section 3.3: a Regular customer ordering Pasta: 92, 84, 76, 76, 76, paid on turn 6 for $18.00. */
    private static void sectionThreeThreeExample() {
        section("Section 3.3 example (Pasta)");
        ScriptedUI ui = new ScriptedUI(
                "seat C1", "wait",       // turn 1
                "order C1", "cook C1",   // turn 2
                "wait", "cook C1",       // turn 3
                "serve C1", "wait",      // turn 4
                "wait", "wait",          // turn 5
                "wait", "wait");         // turn 6
        GameEngine engine = new GameEngine(CONFIG, ui, new FixedFoodChoice("Pasta"));
        List<Integer> satisfaction = new ArrayList<>();
        for (int turn = 1; turn <= 5; turn++) {
            engine.playTurn(turn);
            satisfaction.add(customer(engine, "C1").getSatisfaction());
            if (turn == 3) {
                check(customer(engine, "C1").getOrder().getStatus() == OrderStatus.READY, "Pasta is READY after turn 3");
            }
            if (turn == 4) {
                check(customer(engine, "C1").getStatus() == CustomerStatus.SERVED, "C1 is SERVED on turn 4");
            }
        }
        check(satisfaction.equals(List.of(92, 84, 76, 76, 76)), "satisfaction at turn ends is 92, 84, 76, 76, 76 (got " + satisfaction + ")");
        check(customer(engine, "C1").getStatus() == CustomerStatus.READY_TO_PAY, "C1 is READY_TO_PAY after turn 5 and has not paid");
        check(engine.getRestaurant().getRevenueCents() == 0, "no revenue before turn 6");

        engine.playTurn(6);
        Customer c1 = customer(engine, "C1");
        check(c1.getStatus() == CustomerStatus.PAID, "C1 pays on turn 6");
        check(c1.getOrder().getStatus() == OrderStatus.PAID, "order is PAID");
        check(engine.getRestaurant().getRevenueCents() == 1800, "revenue is $18.00");
        check(c1.getSatisfactionAtPayment() == 76, "satisfaction 76 recorded at payment");
        check(engine.getRestaurant().getTables().get(0).isFree(), "Table 1 is freed");
    }

    private static void dishCannotBeServedOnTheTurnItBecomesReady() {
        section("A dish cannot be served on the turn it becomes READY");
        Restaurant restaurant = new Restaurant(Menu.standard(), 2);
        Customer c1 = new RegularCustomer("C1", 1);
        restaurant.admitCustomer(c1);
        FixedFoodChoice salad = new FixedFoodChoice("Salad");

        restaurant.startNewTurn(); // turn 1
        restaurant.getWaiter().seat(restaurant, c1);
        restaurant.startNewTurn(); // turn 2
        restaurant.getWaiter().takeOrder(restaurant, c1, salad, 2);
        restaurant.getChef().prepare(c1, 2);
        check(c1.getOrder().isReady() && c1.getOrder().getReadyOnTurn() == 2, "Salad became READY on turn 2");

        // Even if the Waiter were free again on turn 2, serving is refused.
        restaurant.startNewTurn();
        ActionResult sameTurn = restaurant.getWaiter().serve(c1, 2);
        check(!sameTurn.success() && sameTurn.message().contains("only became READY this turn"),
                "serving on turn 2 is refused: " + sameTurn.message());
        check(!restaurant.getWaiter().availableTasks(restaurant, 2).contains("serve C1"), "'serve C1' is not offered on turn 2");
        ActionResult nextTurn = restaurant.getWaiter().serve(c1, 3);
        check(nextTurn.success() && c1.getOrder().getStatus() == OrderStatus.SERVED, "serving on turn 3 succeeds");

        // The same rule in a real game: the dish cooked on turn 2 is still unserved at the end of turn 2.
        ScriptedUI ui = new ScriptedUI("seat C1", "wait", "order C1", "cook C1", "serve C1", "wait");
        GameEngine engine = new GameEngine(CONFIG, ui, salad);
        engine.playTurn(1);
        engine.playTurn(2);
        check(customer(engine, "C1").getStatus() == CustomerStatus.ORDERED, "engine: READY Salad not served on turn 2");
        engine.playTurn(3);
        check(customer(engine, "C1").getStatus() == CustomerStatus.SERVED, "engine: Salad served on turn 3");
    }

    private static void abandonmentCancelsOrderAndFreesTable() {
        section("Abandonment at satisfaction 0");
        ScriptedUI ui = new ScriptedUI("seat C1", "wait", "order C1", "cook C1"); // then everyone waits
        GameEngine engine = new GameEngine(CONFIG, ui, new FixedFoodChoice("Pasta"));
        for (int turn = 1; turn <= 12; turn++) {
            engine.playTurn(turn);
        }
        Customer c1 = customer(engine, "C1");
        check(c1.getSatisfaction() == 4 && c1.getStatus() == CustomerStatus.ORDERED, "C1 has 4 satisfaction after turn 12");
        engine.playTurn(13);
        Restaurant restaurant = engine.getRestaurant();
        check(c1.getSatisfaction() == 0 && c1.getStatus() == CustomerStatus.LEFT, "C1 reaches 0 on turn 13 and leaves");
        check(c1.getOrder().getStatus() == OrderStatus.CANCELLED, "C1's unfinished order is CANCELLED");
        check(restaurant.getTables().get(0).isFree(), "Table 1 is freed");
        check(restaurant.getRevenueCents() == 0, "no revenue from the abandoned order");
        check(ui.hasMessageContaining("C1 reaches satisfaction 0 and leaves without paying"), "departure is reported");

        for (int turn = 14; turn <= 16; turn++) {
            engine.playTurn(turn);
        }
        Customer c2 = customer(engine, "C2");
        check(c2.getStatus() == CustomerStatus.LEFT && !restaurant.getWaitingQueue().contains(c2),
                "C2, never seated, leaves the queue at the end of turn 16");
        check(restaurant.getUnhappyDepartures() == 2, "unhappy departures = 2");
    }

    private static void cashierPaysOldestFirst() {
        section("Cashier pays one customer per turn, oldest first");
        Restaurant restaurant = new Restaurant(Menu.standard(), 2);
        Customer c1 = new RegularCustomer("C1", 1);
        Customer c2 = new RegularCustomer("C2", 2);
        restaurant.admitCustomer(c1);
        restaurant.admitCustomer(c2);
        FixedFoodChoice salad = new FixedFoodChoice("Salad");

        restaurant.startNewTurn();
        restaurant.getWaiter().seat(restaurant, c1);
        restaurant.startNewTurn();
        restaurant.getWaiter().seat(restaurant, c2);
        restaurant.startNewTurn();
        restaurant.getWaiter().takeOrder(restaurant, c1, salad, 3);
        restaurant.getChef().prepare(c1, 3);
        restaurant.startNewTurn();
        restaurant.getWaiter().takeOrder(restaurant, c2, salad, 4);
        restaurant.getChef().prepare(c2, 4);
        restaurant.startNewTurn();
        restaurant.getWaiter().serve(c1, 5);
        restaurant.startNewTurn();
        restaurant.getWaiter().serve(c2, 6);

        // Both finish eating on the same turn, so both are READY_TO_PAY together.
        restaurant.finishEating(7);
        restaurant.startNewTurn();
        ActionResult sameTurn = restaurant.getCashier().collectPayment(restaurant, 7);
        check(!sameTurn.success(), "nobody pays on the turn they became READY_TO_PAY");

        restaurant.startNewTurn();
        restaurant.getCashier().collectPayment(restaurant, 8);
        check(c1.getStatus() == CustomerStatus.PAID && c2.getStatus() == CustomerStatus.READY_TO_PAY,
                "both READY_TO_PAY since turn 7: earlier arrival C1 pays on turn 8, C2 waits (one payment per turn)");
        ActionResult second = restaurant.getCashier().collectPayment(restaurant, 8);
        check(!second.success(), "a second payment in the same turn is refused");
        restaurant.startNewTurn();
        restaurant.getCashier().collectPayment(restaurant, 9);
        check(c2.getStatus() == CustomerStatus.PAID && restaurant.getRevenueCents() == 1800, "turn 9: C2 pays, revenue $18.00");

        // Someone who has been READY_TO_PAY longer is paid before an earlier arrival.
        Restaurant other = new Restaurant(Menu.standard(), 2);
        Customer early = new RegularCustomer("C1", 1);
        Customer late = new RegularCustomer("C2", 2);
        other.admitCustomer(early);
        other.admitCustomer(late);
        other.startNewTurn();
        other.getWaiter().seat(other, early);
        other.startNewTurn();
        other.getWaiter().seat(other, late);
        other.startNewTurn();
        other.getWaiter().takeOrder(other, late, salad, 3);
        other.getChef().prepare(late, 3);
        other.startNewTurn();
        other.getWaiter().takeOrder(other, early, salad, 4);
        other.getChef().prepare(early, 4);
        other.startNewTurn();
        other.getWaiter().serve(late, 5);
        other.startNewTurn();
        other.getWaiter().serve(early, 6);
        other.finishEating(6);  // late: READY_TO_PAY on turn 6
        other.finishEating(7);  // early: READY_TO_PAY on turn 7
        other.startNewTurn();
        other.getCashier().collectPayment(other, 8);
        check(late.getStatus() == CustomerStatus.PAID && early.getStatus() == CustomerStatus.READY_TO_PAY,
                "the customer READY_TO_PAY first (C2) pays first");
    }

    private static void invalidInputDoesNotUseTheTask() {
        section("Invalid input does not use up the task");
        ScriptedUI ui = new ScriptedUI(
                "serve C1",   // C1 has no order
                "dance",      // not a command
                "seat C9",    // no such customer
                "seat C1",    // valid
                "cook C1",    // C1 has no order
                "wait");
        GameEngine engine = new GameEngine(CONFIG, ui, new FixedFoodChoice("Salad"));
        engine.playTurn(1);
        check(customer(engine, "C1").getStatus() == CustomerStatus.SEATED, "C1 is seated after three invalid attempts");
        check(ui.hasMessageContaining("C1 has no order waiting to be served"), "reason given for invalid serve");
        check(ui.hasMessageContaining("Waiter commands are"), "reason given for unknown command");
        check(ui.hasMessageContaining("There is no customer C9"), "reason given for unknown customer");
        check(ui.hasMessageContaining("C1 has no order in the kitchen"), "reason given for invalid cook");
        check(ui.hasMessageContaining("Chef waits this turn"), "Chef could still choose after an invalid choice");

        Restaurant restaurant = new Restaurant(Menu.standard(), 1);
        Customer c1 = new RegularCustomer("C1", 1);
        Customer c2 = new RegularCustomer("C2", 1);
        restaurant.admitCustomer(c1);
        restaurant.admitCustomer(c2);
        restaurant.startNewTurn();
        restaurant.getWaiter().seat(restaurant, c1);
        restaurant.startNewTurn();
        ActionResult noTable = restaurant.getWaiter().seat(restaurant, c2);
        check(!noTable.success() && noTable.message().contains("No table is free"), "seating with no free table is refused");
        check(restaurant.getWaiter().isAvailable(), "the Waiter is still available after the refusal");
    }

    private static void winAndLoseBoundaries() {
        section("Win and lose boundaries");
        VictoryCondition condition = new VictoryCondition(CONFIG);
        check(condition.evaluate(4, 4500, OptionalDouble.of(60.0)).victory(), "4 paid, $45.00, average 60 -> Victory");
        check(!condition.evaluate(3, 4500, OptionalDouble.of(60.0)).victory(), "3 paid -> Defeat");
        check(!condition.evaluate(4, 4499, OptionalDouble.of(60.0)).victory(), "$44.99 -> Defeat");
        check(!condition.evaluate(4, 4500, OptionalDouble.of(59.75)).victory(), "average 59.75 -> Defeat");
        check(!condition.evaluate(0, 0, OptionalDouble.empty()).victory(), "nobody paid -> Defeat");
        check(condition.evaluate(5, 9000, OptionalDouble.of(100.0)).victory(), "5 paid, $90.00, average 100 -> Victory");
    }

    private static void atMostFiveCustomersCanPay() {
        section("At most five customers can pay");
        String[] dishes = {"Salad", "Burger", "Pasta"};
        for (String dish : dishes) {
            GameEngine engine = new GameEngine(CONFIG, new GreedyUI(), new FixedFoodChoice(dish));
            GameResult result = engine.run();
            Restaurant restaurant = engine.getRestaurant();
            check(restaurant.getPaidCount() <= 5 && customer(engine, "C6").getStatus() != CustomerStatus.PAID,
                    "fastest play, everyone orders " + dish + ": " + restaurant.getPaidCount()
                            + " paid, C6 does not pay (" + (result.victory() ? "Victory" : "Defeat") + ")");
        }
        GameEngine engine = new GameEngine(CONFIG, new GreedyUI(), new FixedFoodChoice("Salad"));
        engine.run();
        check(engine.getRestaurant().getPaidCount() == 5, "with Salad and fastest play exactly 5 customers pay");

        for (long seed = 1; seed <= 50; seed++) {
            GameEngine seeded = new GameEngine(GameConfig.baseSetting(seed), new GreedyUI(), new SeededRandomFoodChoice(seed));
            seeded.run();
            if (seeded.getRestaurant().getPaidCount() > 5) {
                check(false, "seed " + seed + " let more than five customers pay");
                return;
            }
        }
        check(true, "seeds 1-50 with fastest play: never more than five paid");
    }

    /** Replays runs/victory.txt and runs/defeat.txt with the default seed and compares with the saved logs. */
    private static void savedRunsReproduce() {
        section("Saved victory and defeat runs reproduce");
        checkRun("victory", true);
        checkRun("defeat", false);
    }

    private static void checkRun(String name, boolean expectVictory) {
        Path input = Path.of("runs", name + ".txt");
        Path log = Path.of("runs", name + ".log");
        try (InputStream in = Files.newInputStream(input)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
            GameConfig config = GameConfig.baseSetting(GameConfig.DEFAULT_SEED);
            GameResult result = new GameEngine(config, new ConsoleUI(in, out, true),
                    new SeededRandomFoodChoice(config.seed())).run();
            String expected = Files.readString(log, StandardCharsets.UTF_8);
            String actual = buffer.toString(StandardCharsets.UTF_8);
            check(result.victory() == expectVictory, name + " run ends in " + (expectVictory ? "VICTORY" : "DEFEAT"));
            check(fromFirstTurn(actual).equals(fromFirstTurn(expected)), name + " run output from turn 1 on matches " + log);
        } catch (IOException e) {
            check(false, "could not read " + input + " or " + log + " (run from the project root): " + e.getMessage());
        }
    }

    /**
     * The game from turn 1 on, ignoring line endings. The welcome banner is skipped because it
     * names the stage; the saved logs were recorded with the Stage 1 build.
     */
    static String fromFirstTurn(String text) {
        String normalised = text.replace("\r\n", "\n");
        int start = normalised.indexOf("==================== TURN 1 /");
        return start < 0 ? normalised : normalised.substring(start);
    }

    private static Customer customer(GameEngine engine, String id) {
        return engine.getRestaurant().findCustomer(id);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }

    private static void check(boolean condition, String description) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + description);
        } else {
            failed++;
            System.out.println("FAIL  " + description);
        }
    }
}
