package restaurant;

import restaurant.control.ArrivalSchedule;
import restaurant.control.GameConfig;
import restaurant.control.GameEngine;
import restaurant.domain.SeededRandomFoodChoice;
import restaurant.ui.ConsoleUI;

/**
 * Starts the game with the base setting.
 * Usage: java -cp out restaurant.Main [--seed N] [--schedule mixed|regular] [--echo]
 */
public class Main {
    private static final String USAGE = "Usage: java -cp out restaurant.Main [--seed N] [--schedule mixed|regular] [--echo]";

    public static void main(String[] args) {
        long seed = GameConfig.DEFAULT_SEED;
        ArrivalSchedule schedule = ArrivalSchedule.stageTwoMix();
        boolean echo = System.console() == null; // input is probably redirected from a file
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--seed" -> {
                    if (i + 1 >= args.length) {
                        usageAndExit("--seed needs a number.");
                    }
                    try {
                        seed = Long.parseLong(args[++i]);
                    } catch (NumberFormatException e) {
                        usageAndExit("'" + args[i] + "' is not a valid seed.");
                    }
                }
                case "--schedule" -> {
                    String name = i + 1 < args.length ? args[++i] : "";
                    switch (name) {
                        case "mixed" -> schedule = ArrivalSchedule.stageTwoMix();
                        case "regular" -> schedule = ArrivalSchedule.allRegular();
                        default -> usageAndExit("--schedule must be 'mixed' or 'regular'.");
                    }
                }
                case "--echo" -> echo = true;
                default -> usageAndExit("Unknown argument '" + args[i] + "'.");
            }
        }

        GameConfig config = GameConfig.baseSetting(seed, schedule);
        ConsoleUI ui = new ConsoleUI(System.in, System.out, echo);
        new GameEngine(config, ui, new SeededRandomFoodChoice(seed)).run();
    }

    private static void usageAndExit(String problem) {
        System.err.println(problem);
        System.err.println(USAGE);
        System.exit(1);
    }
}
