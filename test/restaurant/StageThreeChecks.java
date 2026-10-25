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

import restaurant.control.ArrivalSchedule;
import restaurant.control.CustomerFactory;
import restaurant.control.GameConfig;
import restaurant.control.GameEngine;
import restaurant.control.GameResult;
import restaurant.control.HostCommand;
import restaurant.domain.ActionResult;
import restaurant.domain.ComboMeal;
import restaurant.domain.CriticCustomer;
import restaurant.domain.Customer;
import restaurant.domain.CustomerStatus;
import restaurant.domain.HappyHour;
import restaurant.domain.Host;
import restaurant.domain.Menu;
import restaurant.domain.MenuItem;
import restaurant.domain.OrderStatus;
import restaurant.domain.PricingPolicy;
import restaurant.domain.RegularCustomer;
import restaurant.domain.Restaurant;
import restaurant.domain.SeededRandomFoodChoice;
import restaurant.domain.VIPCustomer;
import restaurant.ui.ConsoleUI;

/**
 * Automated checks for Stage 3 (Host, ComboMeal, Happy Hour), using plain Java.
 * Run from the project root: java -cp out-test restaurant.StageThreeChecks
 * (exit code 1 if any check fails). Run StageOneChecks and StageTwoChecks as well.
 *
 * In a Stage 3 game the scripted answers per turn are: the Happy Hour answer (only while
 * Happy Hour is unused), then the Host, Waiter and Chef commands.
 */
public class StageThreeChecks {
    private static final Menu MENU = Menu.withCombos();
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        hostAndWaiterSeatInOneTurn();
        comboUnitsAndPrice();
        happyHourOncePerGame();
        happyHourLastsTwoTurns();
        happyHourRecovery();
        discountOnlyForOrdersTakenWhileActive();
        vipDiscountFirstThenHappyHour();
        priceStaysLockedAfterHappyHour();
        sameBaseSettingAndFeatureSwitches();
        stageThreeRunsReproduce();

        System.out.println();
        System.out.println(passed + " checks passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void hostAndWaiterSeatInOneTurn() {
        section("Host seats one queued customer in addition to the Waiter's task");
        ScriptedUI ui = new ScriptedUI(
                "no", "wait", "wait", "wait",            // turn 1: C1 arrives, nobody acts
                "no", "seat C1", "seat C2", "wait");     // turn 2: C2 arrives; Host seats C1, Waiter seats C2
        GameEngine engine = new GameEngine(config(arrivals(RegularCustomer::new, RegularCustomer::new)), ui, new FixedFoodChoice("Salad"));
        engine.playTurn(1);
        engine.playTurn(2);
        Restaurant restaurant = engine.getRestaurant();
        check(ui.hasMessageContaining("Host seats C1 at Table 1") && ui.hasMessageContaining("Waiter seats C2 at Table 2"),
                "turn 2: Host seats C1 at Table 1 and Waiter seats C2 at Table 2");
        check(restaurant.getWaitingQueue().isEmpty() && restaurant.findFreeTable() == null, "queue empty, both tables occupied");

        Restaurant small = new Restaurant(Menu.standard(), 3, true, false);
        Customer c1 = new RegularCustomer("C1", 1);
        Customer c2 = new RegularCustomer("C2", 1);
        small.admitCustomer(c1);
        small.admitCustomer(c2);
        small.startNewTurn();
        Host host = small.getHost().orElseThrow();
        host.seat(small, c1);
        ActionResult second = host.seat(small, c2);
        check(!second.success() && second.message().contains("already completed a task"), "the Host cannot seat a second customer in the same turn");
        check(small.getWaiter().seat(small, c2).success(), "the Waiter can still seat that customer in the same turn");

        Restaurant oneTable = new Restaurant(Menu.standard(), 1, true, false);
        Customer d1 = new RegularCustomer("C1", 1);
        Customer d2 = new RegularCustomer("C2", 1);
        oneTable.admitCustomer(d1);
        oneTable.admitCustomer(d2);
        oneTable.startNewTurn();
        oneTable.getWaiter().seat(oneTable, d1);
        Host busyless = oneTable.getHost().orElseThrow();
        ActionResult noTable = busyless.seat(oneTable, d2);
        check(!noTable.success() && noTable.message().contains("No table is free") && busyless.isAvailable(),
                "invalid Host choice (no free table) explains why and does not use the Host's task");
        boolean rejected;
        try {
            HostCommand.parse("order C1");
            rejected = false;
        } catch (IllegalArgumentException e) {
            rejected = e.getMessage().contains("Host commands are");
        }
        check(rejected, "the Host can only seat or wait ('order C1' is rejected with the list of Host commands)");
    }

    private static void comboUnitsAndPrice() {
        section("ComboMeal: units add up, price is 90% of the components (nearest cent)");
        MenuItem salad = MENU.findItem("Salad");
        MenuItem burger = MENU.findItem("Burger");
        MenuItem pasta = MENU.findItem("Pasta");
        ComboMeal saladPasta = new ComboMeal(salad, pasta);
        check(saladPasta.getPriceCents() == 2430 && saladPasta.getPrepUnits() == 3, "Salad + Pasta = $24.30, 3 units");
        check(new ComboMeal(salad, burger).getPriceCents() == 2160 && new ComboMeal(salad, burger).getPrepUnits() == 2, "Salad + Burger = $21.60, 2 units");
        check(new ComboMeal(burger, pasta).getPriceCents() == 2970 && new ComboMeal(burger, pasta).getPrepUnits() == 3, "Burger + Pasta = $29.70, 3 units");
        check(new ComboMeal(new MenuItem("A", 1005, 1), new MenuItem("B", 900, 1)).getPriceCents() == 1715,
                "$10.05 + $9.00 = $19.05 x 0.9 = $17.145 rounds to $17.15");
        check(saladPasta.getFirst() == salad && saladPasta.getSecond() == pasta, "a combo keeps its two components");
        List<String> names = new ArrayList<>();
        for (MenuItem item : MENU.getItems()) {
            names.add(item.getName() + " " + item.getPriceCents() + "/" + item.getPrepUnits());
        }
        check(names.equals(List.of("Salad 900/1", "Burger 1500/1", "Pasta 1800/2", "Salad + Burger Combo 2160/2",
                "Salad + Pasta Combo 2430/3", "Burger + Pasta Combo 2970/3")), "Stage 3 menu: " + names);
        check(Menu.standard().getItems().size() == 3, "the Stage 1/2 menu is unchanged (3 single items)");

        ScriptedUI ui = new ScriptedUI(
                "no", "seat C1", "order C1", "cook C1",   // turn 1
                "no", "wait", "serve C1", "wait", "cook C1", // turn 2: serve rejected (not READY), Waiter waits instead
                "no", "wait", "wait", "cook C1",          // turn 3: unit 3 of 3 -> READY
                "no", "wait", "serve C1", "wait");        // turn 4: served
        GameEngine engine = new GameEngine(config(arrivals(RegularCustomer::new)), ui, new FixedFoodChoice("Salad + Pasta Combo"));
        List<String> progress = new ArrayList<>();
        for (int turn = 1; turn <= 4; turn++) {
            engine.playTurn(turn);
            Customer c1 = customer(engine, "C1");
            progress.add(c1.getOrder().getUnitsDone() + "/" + c1.getOrder().getItem().getPrepUnits() + " " + c1.getOrder().getStatus());
        }
        check(progress.equals(List.of("1/3 IN_PREPARATION", "2/3 IN_PREPARATION", "3/3 READY", "3/3 SERVED")),
                "the Chef advances the combo one unit per turn: " + progress);
        check(ui.hasMessageContaining("C1 chooses Salad + Pasta Combo $24.30 (3 prep units)"), "the combo choice is displayed when ordered");
        check(ui.hasMessageContaining("is not READY yet (1/3 prep units done)"),
                "serving a combo before it is READY is refused with a reason (turn 2, before the Chef's second unit)");
        check(new VIPCustomer("C1", 1).calculateBill(saladPasta) == 2187, "a VIP gets 10% off a combo too: $24.30 -> $21.87");
    }

    private static void happyHourOncePerGame() {
        section("Happy Hour can be activated once per game");
        HappyHour happyHour = new HappyHour();
        check(happyHour.isAvailable() && happyHour.activate(3).success(), "first activation on turn 3 succeeds");
        ActionResult again = happyHour.activate(7);
        check(!again.success() && again.message().contains("only once per game") && happyHour.getActivatedOnTurn() == 3,
                "second activation is refused with a reason: " + again.message());

        ScriptedUI ui = new ScriptedUI("no", "wait", "wait", "wait", "yes", "wait", "wait", "wait");
        GameEngine engine = new GameEngine(config(arrivals(RegularCustomer::new)), ui, new FixedFoodChoice("Salad"));
        for (int turn = 1; turn <= 6; turn++) {
            engine.playTurn(turn);
        }
        check(ui.getHappyHourQuestions() == 2, "the engine stops offering Happy Hour once it is used (asked on turns 1 and 2 only)");
    }

    private static void happyHourLastsTwoTurns() {
        section("Happy Hour lasts the activation turn and the next");
        HappyHour happyHour = new HappyHour();
        happyHour.activate(5);
        check(!happyHour.isActive(4) && happyHour.isActive(5) && happyHour.isActive(6) && !happyHour.isActive(7),
                "activated on turn 5: active on 5 and 6 only");
        check(happyHour.remainingTurns(5) == 2 && happyHour.remainingTurns(6) == 1 && happyHour.remainingTurns(7) == 0,
                "remaining turns shown: 2 on turn 5, 1 on turn 6, 0 after");
        HappyHour late = new HappyHour();
        late.activate(18);
        check(late.isActive(18) && late.remainingTurns(18) == 2, "activated on turn 18 it is active on 18 (the game then ends)");
    }

    private static void happyHourRecovery() {
        section("Waiting customers recover 15 at the start of each active turn (max 100)");
        // C1 waits in the queue from turn 1; Happy Hour on turn 4 (active 4 and 5).
        ScriptedUI ui = new ScriptedUI("no", "wait", "wait", "wait", "no", "wait", "wait", "wait", "no", "wait", "wait", "wait",
                "yes", "wait", "wait", "wait");
        GameEngine engine = new GameEngine(config(arrivals(RegularCustomer::new)), ui, new FixedFoodChoice("Salad"));
        List<Integer> ends = new ArrayList<>();
        for (int turn = 1; turn <= 6; turn++) {
            engine.playTurn(turn);
            ends.add(customer(engine, "C1").getSatisfaction());
        }
        check(ends.equals(List.of(92, 84, 76, 83, 90, 82)),
                "queued Regular: 76 +15 = 91 -8 = 83 (turn 4), +15 -8 = 90 (turn 5), no recovery on turn 6: 82; got " + ends);

        // C1 served (not waiting), C2 queued at 92: recovery caps C2 at 100 and skips C1.
        ScriptedUI capUi = new ScriptedUI(
                "no", "seat C1", "order C1", "cook C1",   // turn 1
                "no", "wait", "serve C1", "wait",         // turn 2: C1 served at 92; C2 arrives
                "yes", "wait", "wait", "wait");           // turn 3: Happy Hour
        GameEngine capped = new GameEngine(config(arrivals(RegularCustomer::new, RegularCustomer::new)), capUi, new FixedFoodChoice("Salad"));
        for (int turn = 1; turn <= 3; turn++) {
            capped.playTurn(turn);
        }
        check(capUi.hasMessageContaining("recover 15 (max 100): C2 100."), "C2 at 92 recovers to 100, not 107");
        check(customer(capped, "C1").getSatisfaction() == 92, "C1, already served, does not recover (stays 92)");

        // A Critic who has already left is not affected.
        List<String> lines = new ArrayList<>();
        for (int turn = 1; turn <= 9; turn++) {
            lines.addAll(List.of("no", "wait", "wait", "wait"));
        }
        lines.addAll(List.of("yes", "wait", "wait", "wait"));
        ScriptedUI leftUi = new ScriptedUI(lines.toArray(new String[0]));
        GameEngine left = new GameEngine(config(arrivals(CriticCustomer::new)), leftUi, new FixedFoodChoice("Salad"));
        for (int turn = 1; turn <= 10; turn++) {
            left.playTurn(turn);
        }
        Customer critic = customer(left, "C1");
        check(critic.getStatus() == CustomerStatus.LEFT && critic.getSatisfaction() == 0 && !leftUi.hasMessageContaining("recover 15 (max"),
                "a customer who left on turn 9 is not affected by Happy Hour on turn 10");
    }

    private static void discountOnlyForOrdersTakenWhileActive() {
        section("20% off only for orders taken while Happy Hour is active");
        Restaurant restaurant = new Restaurant(Menu.standard(), 4, false, true);
        FixedFoodChoice burger = new FixedFoodChoice("Burger");
        List<Customer> customers = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Customer customer = new RegularCustomer("C" + i, 1);
            customers.add(customer);
            restaurant.admitCustomer(customer);
            restaurant.startNewTurn();
            restaurant.getWaiter().seat(restaurant, customer);
        }
        HappyHour happyHour = restaurant.getHappyHour().orElseThrow();
        int[] orderTurns = {4, 5, 6, 7};
        for (int i = 0; i < 4; i++) {
            if (orderTurns[i] == 5) {
                happyHour.activate(5);
            }
            restaurant.startNewTurn();
            restaurant.getWaiter().takeOrder(restaurant, customers.get(i), burger, orderTurns[i]);
        }
        List<Integer> prices = new ArrayList<>();
        for (Customer customer : customers) {
            prices.add(customer.getOrder().getPriceCents());
        }
        check(prices.equals(List.of(1500, 1200, 1200, 1500)),
                "Burger ordered on turns 4 / 5 / 6 / 7 with Happy Hour on turns 5-6: " + prices);
        check(customers.get(0).getOrder().getPriceCents() == 1500, "the order placed before activation keeps its full price");
    }

    private static void vipDiscountFirstThenHappyHour() {
        section("Discount order: VIP first, then Happy Hour");
        HappyHour happyHour = new HappyHour();
        happyHour.activate(1);
        PricingPolicy pricing = new PricingPolicy(List.of(happyHour));
        Customer vip = new VIPCustomer("C1", 1);
        MenuItem burger = MENU.findItem("Burger");
        check(pricing.lockedPrice(vip, burger, 1) == 1080, "VIP Burger during Happy Hour: $15.00 x 0.9 x 0.8 = $10.80");
        check(pricing.describeDiscounts(vip, burger, 1).equals("VIP + Happy Hour"), "the bill label names both, VIP first");
        check(pricing.lockedPrice(new RegularCustomer("C2", 1), burger, 1) == 1200, "Regular Burger during Happy Hour: $12.00");
        check(pricing.lockedPrice(vip, burger, 3) == 1350, "VIP Burger after Happy Hour: $13.50");
        MenuItem tiny = new MenuItem("Mint", 35, 1);
        int vipFirst = pricing.lockedPrice(vip, tiny, 1);
        int happyHourFirst = vip.calculateBill(new MenuItem("Mint", happyHour.applyDiscount(35), 1));
        check(vipFirst == 26 && happyHourFirst == 25,
                "the order matters with rounding: 35c -> VIP 32c -> Happy Hour 26c (the other order would give 25c)");
    }

    private static void priceStaysLockedAfterHappyHour() {
        section("The price is locked when the order is taken");
        ScriptedUI ui = new ScriptedUI(
                "yes", "seat C1", "order C1", "cook C1",  // turn 1: Happy Hour on; VIP orders a Burger
                "wait", "serve C1", "wait",               // turn 2 (no Happy Hour question any more)
                "wait", "wait", "wait",                   // turn 3: finishes eating
                "wait", "wait", "wait");                  // turn 4: pays, Happy Hour long over
        GameEngine engine = new GameEngine(config(arrivals(VIPCustomer::new)), ui, new FixedFoodChoice("Burger"));
        for (int turn = 1; turn <= 4; turn++) {
            engine.playTurn(turn);
        }
        Customer vip = customer(engine, "C1");
        HappyHour happyHour = engine.getRestaurant().getHappyHour().orElseThrow();
        check(ui.hasMessageContaining("VIP + Happy Hour bill: $10.80"), "the locked bill is shown when the order is taken");
        check(!happyHour.isActive(4) && vip.getStatus() == CustomerStatus.PAID && vip.getOrder().getStatus() == OrderStatus.PAID,
                "VIP pays on turn 4, after Happy Hour has ended");
        check(vip.getOrder().getPriceCents() == 1080 && engine.getRestaurant().getRevenueCents() == 1080,
                "the Cashier collects the locked $10.80, not $13.50");
    }

    private static void sameBaseSettingAndFeatureSwitches() {
        section("Stage 3 keeps the base setting; earlier stages keep their features");
        GameConfig stageThree = GameConfig.stageThree(GameConfig.DEFAULT_SEED);
        check(stageThree.totalTurns() == 18 && stageThree.tableCount() == 2 && stageThree.minPaidCustomers() == 4
                        && stageThree.minRevenueCents() == 4500 && stageThree.minAverageSatisfaction() == 60
                        && stageThree.arrivalSchedule().getArrivals().size() == 6,
                "18 turns, 2 tables, six arrivals, targets 4 paid / $45.00 / 60");
        check(stageThree.hostEnabled() && stageThree.happyHourEnabled() && stageThree.menu().getItems().size() == 6,
                "Stage 3 adds the Host, Happy Hour and the combos");
        GameConfig stageOne = GameConfig.forStage(1, GameConfig.DEFAULT_SEED);
        GameConfig stageTwo = GameConfig.forStage(2, GameConfig.DEFAULT_SEED);
        check(!stageOne.hostEnabled() && !stageOne.happyHourEnabled() && stageOne.menu().getItems().size() == 3
                        && !stageTwo.hostEnabled() && !stageTwo.happyHourEnabled() && stageTwo.menu().getItems().size() == 3,
                "--stage 1 and --stage 2 have no Host, no Happy Hour and the 3-item menu");
        Restaurant plain = new Restaurant(Menu.standard(), 2);
        check(plain.getHost().isEmpty() && plain.getHappyHour().isEmpty() && plain.getStaff().size() == 3,
                "a Stage 1/2 restaurant has only Waiter, Chef and Cashier");
    }

    private static void stageThreeRunsReproduce() {
        section("Saved Stage 3 runs reproduce (seed 2002)");
        GameEngine winner = checkRun("stage3-victory", true);
        GameEngine loser = checkRun("stage3-defeat", false);
        if (winner != null && loser != null) {
            check(customer(winner, "C3").getOrder().getPriceCents() == 1080 && customer(loser, "C3").getOrder().getPriceCents() == 1350,
                    "VIP C3's Burger: $10.80 with Happy Hour, $13.50 without");
            check(customer(winner, "C2").getSatisfactionAtPayment() == 43 && customer(loser, "C2").getSatisfactionAtPayment() == 28,
                    "Happy Hour recovery lifts the Critic from 28 to 43 at payment");
            check(customer(winner, "C1").getOrder().getItem().getPrepUnits() == 3
                            && customer(winner, "C1").getOrder().getItem().getName().equals("Salad + Pasta Combo"),
                    "C1 ordered the Salad + Pasta combo (3 units)");
        }
    }

    private static GameEngine checkRun(String name, boolean expectVictory) {
        Path input = Path.of("runs", name + ".txt");
        Path log = Path.of("runs", name + ".log");
        try (InputStream in = Files.newInputStream(input)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
            GameConfig config = GameConfig.stageThree(GameConfig.DEFAULT_SEED);
            GameEngine engine = new GameEngine(config, new ConsoleUI(in, out, true), new SeededRandomFoodChoice(config.seed()));
            GameResult result = engine.run();
            String expected = Files.readString(log, StandardCharsets.UTF_8).replace("\r\n", "\n");
            String actual = buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
            check(result.victory() == expectVictory, name + " ends in " + (expectVictory ? "VICTORY" : "DEFEAT"));
            check(actual.equals(expected), name + " output matches " + log);
            return engine;
        } catch (IOException e) {
            check(false, "could not read " + input + " or " + log + " (run from the project root): " + e.getMessage());
            return null;
        }
    }

    /** A Stage 3 game with the given customers arriving on turns 1, 2, 3, ... */
    private static GameConfig config(ArrivalSchedule schedule) {
        return GameConfig.stageThree(GameConfig.DEFAULT_SEED, schedule);
    }

    private static ArrivalSchedule arrivals(CustomerFactory... factories) {
        List<ArrivalSchedule.Arrival> list = new ArrayList<>();
        for (int i = 0; i < factories.length; i++) {
            list.add(new ArrivalSchedule.Arrival(i + 1, factories[i]));
        }
        return new ArrivalSchedule(list);
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
