package restaurant;

import restaurant.control.GameConfig;
import restaurant.control.GameEngine;
import restaurant.domain.SeededRandomFoodChoice;
import restaurant.ui.ConsoleUI;

/**
 * Starts the game with the base setting.
 * Usage: java -cp out restaurant.Main [--stage 1|2|3] [--seed N] [--echo]
 */
public class Main {
    private static final String USAGE = "Usage: java -cp out restaurant.Main [--stage 1|2|3] [--seed N] [--echo]";

    public static void main(String[] args) {
        long seed = GameConfig.DEFAULT_SEED;
        int stage = 3;
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
                case "--stage" -> {
                    String value = i + 1 < args.length ? args[++i] : "";
                    switch (value) {
                        case "1" -> stage = 1;
                        case "2" -> stage = 2;
                        case "3" -> stage = 3;
                        default -> usageAndExit("--stage must be 1, 2 or 3.");
                    }
                }
                case "--echo" -> echo = true;
                default -> usageAndExit("Unknown argument '" + args[i] + "'.");
            }
        }

        GameConfig config = GameConfig.forStage(stage, seed);
        ConsoleUI ui = new ConsoleUI(System.in, System.out, echo);
        new GameEngine(config, ui, new SeededRandomFoodChoice(seed)).run();
    }

    private static void usageAndExit(String problem) {
        System.err.println(problem);
        System.err.println(USAGE);
        System.exit(1);
    }
}
