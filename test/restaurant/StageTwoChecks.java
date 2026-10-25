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
import java.util.stream.Stream;

import restaurant.control.ArrivalSchedule;
import restaurant.control.GameConfig;
import restaurant.control.GameEngine;
import restaurant.control.GameResult;
import restaurant.domain.CriticCustomer;
import restaurant.domain.Customer;
import restaurant.domain.CustomerStatus;
import restaurant.domain.Menu;
import restaurant.domain.MenuItem;
import restaurant.domain.OrderStatus;
import restaurant.domain.RegularCustomer;
import restaurant.domain.SeededRandomFoodChoice;
import restaurant.domain.VIPCustomer;
import restaurant.ui.ConsoleUI;

/**
 * Automated checks for Stage 2 (VIP and Critic customers), using plain Java.
 * Run from the project root: java -cp out-test restaurant.StageTwoChecks
 * (exit code 1 if any check fails). Run StageOneChecks as well for the Stage 1 rules.
 */
public class StageTwoChecks {
    private static final Menu MENU = Menu.standard();
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        vipBillIsTenPercentOff();
        lossRatesPerType();
        fullServiceForEachType();
        satisfactionStaysBetweenZeroAndHundred();
        criticOrdersSignatureDish();
        polymorphicDispatch();
        sameBaseSettingAndTargets();
        stageTwoRunsReproduce();

        System.out.println();
        System.out.println(passed + " checks passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void vipBillIsTenPercentOff() {
        section("VIP bill is 10% off (integer cents, nearest cent)");
        Customer vip = new VIPCustomer("C1", 1);
        check(vip.calculateBill(MENU.findItem("Salad")) == 810, "Salad $9.00 -> $8.10");
        check(vip.calculateBill(MENU.findItem("Burger")) == 1350, "Burger $15.00 -> $13.50");
        check(vip.calculateBill(MENU.findItem("Pasta")) == 1620, "Pasta $18.00 -> $16.20");
        check(vip.calculateBill(new MenuItem("Test", 1005, 1)) == 905, "$10.05 -> $9.045 rounds up to $9.05");
        check(vip.calculateBill(new MenuItem("Test", 1004, 1)) == 904, "$10.04 -> $9.036 rounds to $9.04");
        check(new RegularCustomer("C2", 1).calculateBill(MENU.findItem("Burger")) == 1500, "Regular pays full price $15.00");
        check(new CriticCustomer("C3", 1).calculateBill(MENU.findItem("Burger")) == 1500, "Critic pays full price $15.00");
    }

    /** Three customers arrive on turns 1, 2, 3 and nobody is served; after turn 3 each has waited a different number of turns. */
    private static void lossRatesPerType() {
        section("Satisfaction loss per waiting turn: Regular 8, VIP 5, Critic 12");
        ArrivalSchedule schedule = new ArrivalSchedule(List.of(
                new ArrivalSchedule.Arrival(1, RegularCustomer::new),
                new ArrivalSchedule.Arrival(2, VIPCustomer::new),
                new ArrivalSchedule.Arrival(3, CriticCustomer::new)));
        GameEngine engine = new GameEngine(GameConfig.baseSetting(GameConfig.DEFAULT_SEED, schedule),
                new ScriptedUI(), new FixedFoodChoice("Salad"));
        for (int turn = 1; turn <= 3; turn++) {
            engine.playTurn(turn);
        }
        check(customer(engine, "C1").getSatisfaction() == 76, "Regular C1 after 3 waiting turns: 100 - 3x8 = 76");
        check(customer(engine, "C2").getSatisfaction() == 90, "VIP C2 after 2 waiting turns: 100 - 2x5 = 90");
        check(customer(engine, "C3").getSatisfaction() == 88, "Critic C3 after 1 waiting turn: 100 - 12 = 88");
    }

    /** The Section 3.3 flow with full attention, for VIP and Critic: same turns, different numbers. */
    private static void fullServiceForEachType() {
        section("Same service flow for every type (Section 3.3 script)");
        checkFullService("VIP", VIPCustomer::new, List.of(95, 90, 85, 85, 85), 1620, 85);
        checkFullService("Critic", CriticCustomer::new, List.of(88, 76, 64, 64, 64), 1800, 64);
    }

    private static void checkFullService(String type, restaurant.control.CustomerFactory factory,
                                         List<Integer> expected, int bill, int recorded) {
        ArrivalSchedule schedule = new ArrivalSchedule(List.of(new ArrivalSchedule.Arrival(1, factory)));
        ScriptedUI ui = new ScriptedUI("seat C1", "wait", "order C1", "cook C1", "wait", "cook C1", "serve C1", "wait");
        GameEngine engine = new GameEngine(GameConfig.baseSetting(GameConfig.DEFAULT_SEED, schedule), ui, new FixedFoodChoice("Pasta"));
        List<Integer> satisfaction = new ArrayList<>();
        for (int turn = 1; turn <= 5; turn++) {
            engine.playTurn(turn);
            satisfaction.add(customer(engine, "C1").getSatisfaction());
        }
        engine.playTurn(6);
        Customer c1 = customer(engine, "C1");
        check(satisfaction.equals(expected), type + " Pasta: satisfaction " + satisfaction + " (expected " + expected + ")");
        check(c1.getStatus() == CustomerStatus.PAID && engine.getRestaurant().getRevenueCents() == bill
                        && c1.getSatisfactionAtPayment() == recorded,
                type + " pays on turn 6: revenue " + engine.getRestaurant().getRevenueCents() + " cents, satisfaction "
                        + c1.getSatisfactionAtPayment() + " recorded");
    }

    /** LSP: whatever the loss rate, satisfaction never leaves 0..100, and 0 still means leaving. */
    private static void satisfactionStaysBetweenZeroAndHundred() {
        section("Satisfaction stays between 0 and 100 for every type");
        ArrivalSchedule schedule = new ArrivalSchedule(List.of(new ArrivalSchedule.Arrival(1, CriticCustomer::new)));
        GameEngine engine = new GameEngine(GameConfig.baseSetting(GameConfig.DEFAULT_SEED, schedule),
                new ScriptedUI("seat C1", "wait", "order C1", "cook C1"), new FixedFoodChoice("Salad"));
        for (int turn = 1; turn <= 8; turn++) {
            engine.playTurn(turn);
        }
        Customer critic = customer(engine, "C1");
        check(critic.getSatisfaction() == 4, "Critic waiting with a READY Pasta: 4 after turn 8");
        engine.playTurn(9);
        check(critic.getSatisfaction() == 0, "turn 9: 4 - 12 stops at 0, not -8");
        check(critic.getStatus() == CustomerStatus.LEFT && critic.getOrder().getStatus() == OrderStatus.CANCELLED
                        && engine.getRestaurant().getTables().get(0).isFree(),
                "Critic leaves at 0: order cancelled, table freed (same rule as Regular)");

        boolean inRange = true;
        for (long seed = 1; seed <= 30 && inRange; seed++) {
            GameEngine game = new GameEngine(GameConfig.baseSetting(seed, ArrivalSchedule.stageTwoMix()),
                    new GreedyUI(), new SeededRandomFoodChoice(seed));
            for (int turn = 1; turn <= 18; turn++) {
                game.playTurn(turn);
                for (Customer customer : game.getRestaurant().getCustomers()) {
                    inRange &= customer.getSatisfaction() >= 0 && customer.getSatisfaction() <= Customer.MAX_SATISFACTION;
                }
            }
        }
        check(inRange, "mixed schedule, seeds 1-30, fastest play: every satisfaction in 0..100 at every turn end");
    }

    private static void criticOrdersSignatureDish() {
        section("Critic special behaviour: always orders the most expensive dish");
        FixedFoodChoice salad = new FixedFoodChoice("Salad");
        check(new CriticCustomer("C1", 1).chooseDish(MENU, salad).getName().equals("Pasta"),
                "Critic orders Pasta even when the food-choice rule says Salad");
        check(new RegularCustomer("C2", 1).chooseDish(MENU, salad).getName().equals("Salad"),
                "Regular follows the food-choice rule (Salad)");
        check(new VIPCustomer("C3", 1).chooseDish(MENU, salad).getName().equals("Salad"),
                "VIP follows the food-choice rule (Salad)");
        Menu other = new Menu(List.of(new MenuItem("Soup", 500, 1), new MenuItem("Steak", 2500, 3), new MenuItem("Fish", 2500, 2)));
        check(new CriticCustomer("C4", 1).chooseDish(other, salad).getName().equals("Steak"),
                "on another menu the Critic picks the most expensive (Steak; first listed on a tie with Fish)");

        boolean alwaysPasta = true;
        for (long seed = 1; seed <= 30; seed++) {
            alwaysPasta &= new CriticCustomer("C1", 1).chooseDish(MENU, new SeededRandomFoodChoice(seed)).getName().equals("Pasta");
        }
        check(alwaysPasta, "with seeded random choice (seeds 1-30) the Critic still always orders Pasta");

        ArrivalSchedule schedule = new ArrivalSchedule(List.of(new ArrivalSchedule.Arrival(1, CriticCustomer::new)));
        ScriptedUI ui = new ScriptedUI("seat C1", "wait", "order C1", "wait");
        GameEngine engine = new GameEngine(GameConfig.baseSetting(GameConfig.DEFAULT_SEED, schedule), ui, salad);
        engine.playTurn(1);
        engine.playTurn(2);
        check(ui.hasMessageContaining("C1 chooses Pasta $18.00 (2 prep units)"), "the choice is displayed when the order is taken");
    }

    private static void polymorphicDispatch() {
        section("Behaviour is dispatched through the Customer objects");
        List<Customer> customers = List.of(new RegularCustomer("C1", 1), new VIPCustomer("C2", 1), new CriticCustomer("C3", 1));
        List<String> seen = new ArrayList<>();
        MenuItem burger = MENU.findItem("Burger");
        FixedFoodChoice salad = new FixedFoodChoice("Salad");
        for (Customer customer : customers) { // only the Customer type is used here
            seen.add(customer.getTypeName() + " " + customer.getSatisfactionLossPerTurn() + " "
                    + customer.calculateBill(burger) + " " + customer.chooseDish(MENU, salad).getName());
        }
        check(seen.equals(List.of("Regular 8 1500 Salad", "VIP 5 1350 Salad", "Critic 12 1500 Pasta")),
                "same calls on Customer references give type-specific results: " + seen);

        GameEngine engine = new GameEngine(GameConfig.baseSetting(GameConfig.DEFAULT_SEED, ArrivalSchedule.stageTwoMix()),
                new GreedyUI(), new SeededRandomFoodChoice(GameConfig.DEFAULT_SEED));
        engine.run();
        Customer vip = customer(engine, "C3");
        Customer critic = customer(engine, "C2");
        check(vip.getStatus() == CustomerStatus.PAID
                        && vip.getOrder().getPriceCents() == vip.calculateBill(vip.getOrder().getItem())
                        && vip.getOrder().getPriceCents() < vip.getOrder().getItem().getPriceCents(),
                "unchanged engine, mixed game: VIP C3 paid the discounted bill " + vip.getOrder().getPriceCents() + " cents");
        check(critic.getStatus() == CustomerStatus.PAID && critic.getOrder().getItem().getName().equals("Pasta"),
                "unchanged engine, mixed game: Critic C2 ordered Pasta and paid");

        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Path.of("src"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String name = file.getFileName().toString();
                String code = Files.readString(file);
                boolean allowedToNameTypes = name.equals("ArrivalSchedule.java")
                        || name.equals("VIPCustomer.java") || name.equals("CriticCustomer.java");
                if (code.contains("instanceof") || code.contains("getClass()")
                        || (!allowedToNameTypes && (code.contains("VIPCustomer") || code.contains("CriticCustomer")))) {
                    offenders.add(name);
                }
            }
            check(offenders.isEmpty(), "no instanceof/getClass() anywhere in src, and only ArrivalSchedule names VIPCustomer/CriticCustomer"
                    + (offenders.isEmpty() ? "" : ": " + offenders));
        } catch (IOException e) {
            check(false, "could not scan src (run from the project root): " + e.getMessage());
        }
    }

    private static void sameBaseSettingAndTargets() {
        section("Stage 2 uses the same base setting and win/lose criteria");
        GameConfig stageOne = GameConfig.baseSetting(GameConfig.DEFAULT_SEED);
        GameConfig stageTwo = GameConfig.baseSetting(GameConfig.DEFAULT_SEED, ArrivalSchedule.stageTwoMix());
        check(stageTwo.totalTurns() == 18 && stageTwo.tableCount() == 2, "18 turns, 2 tables");
        check(stageTwo.minPaidCustomers() == stageOne.minPaidCustomers() && stageTwo.minRevenueCents() == stageOne.minRevenueCents()
                        && stageTwo.minAverageSatisfaction() == stageOne.minAverageSatisfaction(),
                "same targets: 4 paid, $45.00, average 60");
        List<Integer> turns = new ArrayList<>();
        List<String> types = new ArrayList<>();
        for (ArrivalSchedule.Arrival arrival : stageTwo.arrivalSchedule().getArrivals()) {
            turns.add(arrival.turn());
            types.add(arrival.factory().create("X", arrival.turn()).getTypeName());
        }
        check(turns.equals(List.of(1, 4, 7, 10, 13, 16)), "six arrivals on turns 1, 4, 7, 10, 13, 16");
        check(types.contains("VIP") && types.contains("Critic") && types.stream().filter("Regular"::equals).count() == 4,
                "mixed schedule: one VIP and one Critic replace two Regulars " + types);
    }

    private static void stageTwoRunsReproduce() {
        section("Saved Stage 2 runs reproduce (mixed schedule, seed 2002)");
        GameEngine winner = checkRun("stage2-critic-first", true);
        GameEngine loser = checkRun("stage2-regular-first", false);
        if (winner != null && loser != null) {
            check(customer(winner, "C2").getSatisfactionAtPayment() == 64 && customer(loser, "C2").getSatisfactionAtPayment() == 52,
                    "the Critic records 64 when seated first, 52 when the Regular goes first (one more turn x 12)");
            check(customer(winner, "C1").getSatisfactionAtPayment() == 44 && customer(loser, "C1").getSatisfactionAtPayment() == 52,
                    "the Regular records 44 instead of 52 (one more turn x 8): net +4 points for serving the Critic first");
        }
    }

    private static GameEngine checkRun(String name, boolean expectVictory) {
        Path input = Path.of("runs", name + ".txt");
        Path log = Path.of("runs", name + ".log");
        try (InputStream in = Files.newInputStream(input)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
            GameConfig config = GameConfig.baseSetting(GameConfig.DEFAULT_SEED, ArrivalSchedule.stageTwoMix());
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
