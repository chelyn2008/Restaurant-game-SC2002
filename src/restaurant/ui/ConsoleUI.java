package restaurant.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.OptionalDouble;
import java.util.StringJoiner;

import restaurant.control.ChefCommand;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.WaiterCommand;
import restaurant.domain.Customer;
import restaurant.domain.CustomerStatus;
import restaurant.domain.Menu;
import restaurant.domain.MenuItem;
import restaurant.domain.Money;
import restaurant.domain.Restaurant;
import restaurant.domain.Table;

/**
 * Command-line interface. Reads one command per line from the input stream, so a
 * run can be replayed with input redirection (java ... < runs/victory.txt).
 */
public class ConsoleUI implements GameUI {
    private final BufferedReader in;
    private final PrintStream out;
    private final boolean echoInput;

    /**
     * @param echoInput print each command after reading it; useful when input is
     *                  redirected from a file, so the output log shows the choices
     */
    public ConsoleUI(InputStream in, PrintStream out, boolean echoInput) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = out;
        this.echoInput = echoInput;
    }

    @Override
    public void showWelcome(GameConfig config, Menu menu) {
        out.println("=== Restaurant Rush (Stage 2) ===");
        out.println("Seed: " + config.seed() + " | Turns: " + config.totalTurns() + " | Tables: " + config.tableCount());
        out.println("Arrivals: " + config.arrivalSchedule().describe());
        out.println("Menu:");
        for (MenuItem item : menu.getItems()) {
            out.println("  " + item);
        }
        out.println("Goal after turn " + config.totalTurns() + ": at least " + config.minPaidCustomers()
                + " paid customers, revenue at least " + Money.format(config.minRevenueCents())
                + ", average satisfaction of paid customers at least " + config.minAverageSatisfaction() + ".");
        out.println("Waiter commands: seat <id> | order <id> | serve <id> | wait");
        out.println("Chef commands:   cook <id> | wait");
    }

    @Override
    public void showTurnHeader(int turn, int totalTurns) {
        out.println();
        out.println("==================== TURN " + turn + " / " + totalTurns + " ====================");
    }

    @Override
    public void showTurnStart(Restaurant restaurant, List<String> waiterTasks, List<String> chefTasks) {
        out.println("Revenue: " + Money.format(restaurant.getRevenueCents())
                + " | Paid customers: " + restaurant.getPaidCount()
                + " | Average satisfaction (paid): " + formatAverage(restaurant.getAverageSatisfactionOfPaid()));

        StringJoiner queue = new StringJoiner(", ");
        for (Customer customer : restaurant.getWaitingQueue()) {
            queue.add(describeCustomer(customer));
        }
        out.println("Waiting queue: " + (queue.length() == 0 ? "(empty)" : queue));

        for (Table table : restaurant.getTables()) {
            Customer customer = table.getOccupant();
            if (customer == null) {
                out.println("Table " + table.getNumber() + ": free");
            } else {
                String order = customer.getOrder() == null ? "no order yet" : "order: " + customer.getOrder().describe();
                out.println("Table " + table.getNumber() + ": " + describeCustomer(customer)
                        + " " + customer.getStatus() + ", " + order);
            }
        }
        out.println("Waiter can: " + joinTasks(waiterTasks));
        out.println("Chef can:   " + joinTasks(chefTasks));
    }

    @Override
    public WaiterCommand askWaiterCommand() {
        while (true) {
            String line = readLine("Waiter> ");
            if (line == null) {
                out.println("(end of input: Waiter waits)");
                return WaiterCommand.WAIT;
            }
            try {
                return WaiterCommand.parse(line);
            } catch (IllegalArgumentException e) {
                out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    @Override
    public ChefCommand askChefCommand() {
        while (true) {
            String line = readLine("Chef> ");
            if (line == null) {
                out.println("(end of input: Chef waits)");
                return ChefCommand.WAIT;
            }
            try {
                return ChefCommand.parse(line);
            } catch (IllegalArgumentException e) {
                out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    @Override
    public void showMessage(String message) {
        out.println("  " + message);
    }

    @Override
    public void showTurnResults(int turn, int totalTurns, Restaurant restaurant) {
        out.println("--- End of turn " + turn + " ---");
        out.println("Served: " + restaurant.getServedCount()
                + " | Unhappy departures: " + restaurant.getUnhappyDepartures()
                + " | Revenue: " + Money.format(restaurant.getRevenueCents())
                + " | Average satisfaction (paid): " + formatAverage(restaurant.getAverageSatisfactionOfPaid())
                + " | Turns used: " + turn + "/" + totalTurns);
        StringJoiner present = new StringJoiner(", ");
        for (Customer customer : restaurant.getCustomers()) {
            switch (customer.getStatus()) {
                case PAID, LEFT -> { }
                default -> present.add(customer.getId() + " " + customer.getSatisfaction() + " (" + customer.getStatus() + ")");
            }
        }
        out.println("Customers in restaurant: " + (present.length() == 0 ? "none" : present));
    }

    @Override
    public void showFinalResult(GameResult result, Restaurant restaurant) {
        out.println();
        out.println("==================== FINAL RESULT: " + (result.victory() ? "VICTORY" : "DEFEAT") + " ====================");
        for (String line : result.targetLines()) {
            out.println(line);
        }
        out.println("Served: " + restaurant.getServedCount() + " | Unhappy departures: " + restaurant.getUnhappyDepartures());
        for (Customer customer : restaurant.getCustomers()) {
            String detail = customer.getStatus() == CustomerStatus.PAID
                    ? "paid " + Money.format(customer.getOrder().getPriceCents())
                    + ", satisfaction at payment " + customer.getSatisfactionAtPayment()
                    : "satisfaction " + customer.getSatisfaction();
            out.println("  " + customer.getId() + ": " + customer.getStatus() + ", " + detail);
        }
    }

    private String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = in.readLine();
            if (line != null && echoInput) {
                out.println(line);
            } else if (line == null) {
                out.println();
            }
            return line;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String describeCustomer(Customer customer) {
        return customer.getId() + " [" + customer.getTypeName() + ", satisfaction " + customer.getSatisfaction() + "]";
    }

    private static String joinTasks(List<String> tasks) {
        StringJoiner joiner = new StringJoiner(" | ");
        tasks.forEach(joiner::add);
        joiner.add("wait");
        return joiner.toString();
    }

    private static String formatAverage(OptionalDouble average) {
        return average.isPresent() ? String.format("%.1f", average.getAsDouble()) : "N/A";
    }
}
