package restaurant.control;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import restaurant.domain.CriticCustomer;
import restaurant.domain.Customer;
import restaurant.domain.RegularCustomer;
import restaurant.domain.VIPCustomer;

/**
 * Which kind of customer arrives on which turn. Customers are named C1, C2, ... in arrival order.
 * This is the only place that names the concrete customer classes.
 */
public class ArrivalSchedule {

    /** One scheduled arrival: the turn and how to create that customer. */
    public record Arrival(int turn, CustomerFactory factory) {
    }

    private final List<Arrival> arrivals;

    public ArrivalSchedule(List<Arrival> arrivals) {
        this.arrivals = List.copyOf(arrivals);
    }

    /** Stage 1 schedule: six Regular customers on turns 1, 4, 7, 10, 13, 16. */
    public static ArrivalSchedule allRegular() {
        return new ArrivalSchedule(List.of(
                new Arrival(1, RegularCustomer::new),
                new Arrival(4, RegularCustomer::new),
                new Arrival(7, RegularCustomer::new),
                new Arrival(10, RegularCustomer::new),
                new Arrival(13, RegularCustomer::new),
                new Arrival(16, RegularCustomer::new)));
    }

    /** Stage 2 schedule: same six turns, with one VIP and one Critic replacing two Regulars. */
    public static ArrivalSchedule stageTwoMix() {
        return new ArrivalSchedule(List.of(
                new Arrival(1, RegularCustomer::new),
                new Arrival(4, CriticCustomer::new),
                new Arrival(7, VIPCustomer::new),
                new Arrival(10, RegularCustomer::new),
                new Arrival(13, RegularCustomer::new),
                new Arrival(16, RegularCustomer::new)));
    }

    public List<Arrival> getArrivals() {
        return arrivals;
    }

    /** The customer who arrives on this turn, if any. */
    public Optional<Customer> customerArrivingOn(int turn) {
        for (int i = 0; i < arrivals.size(); i++) {
            Arrival arrival = arrivals.get(i);
            if (arrival.turn() == turn) {
                return Optional.of(arrival.factory().create("C" + (i + 1), turn));
            }
        }
        return Optional.empty();
    }

    /** For display, e.g. "turn 1: C1 Regular (-8/turn), turn 4: C2 Critic (-12/turn)". */
    public String describe() {
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < arrivals.size(); i++) {
            Arrival arrival = arrivals.get(i);
            Customer sample = arrival.factory().create("C" + (i + 1), arrival.turn());
            joiner.add("turn " + arrival.turn() + ": " + sample.getId() + " " + sample.getTypeName()
                    + " (-" + sample.getSatisfactionLossPerTurn() + "/turn)");
        }
        return joiner.toString();
    }
}
