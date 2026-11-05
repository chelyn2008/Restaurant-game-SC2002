package restaurant.domain;

import java.util.Comparator;
import java.util.Optional;

/**
 * Extra feature (Stage 3, switched on with --hints): warns how long each waiting customer
 * has before leaving, and suggests the most urgent customer and their next step
 * It only reads the game state; it never changes it, so hints cannot change the rules
 *
 * Each customer type answers through its own getSatisfactionLossPerTurn(), so Regular,
 * VIP and Critic customers get different warnings without any type checks -> polymorphism concept
 */
public class ServiceAdvisor {

    /**
      How many turn ends, counting this one, the customer can stay unserved before leaving,
      e.g. a Critic at 28 loses 12 per turn: 16, 4, 0, so 3 turns
      Happy Hour recovery is not counted, so the warning is on the safe side
     */
    public int turnsUntilLeaving(Customer customer) {
        int loss = customer.getSatisfactionLossPerTurn();
        return (customer.getSatisfaction() + loss - 1) / loss; // rounds up
    }

    // The turn at whose end the customer leaves if still not served 
    public int leavingTurn(Customer customer, int turn) {
        return turn + turnsUntilLeaving(customer) - 1;
    }

    // The waiting customer who will leave soonest; on a tie, the earlier arrival 
    public Optional<Customer> mostUrgent(Restaurant restaurant) {
        return restaurant.getCustomers().stream()
                .filter(Customer::isWaitingForService)
                .min(Comparator.comparingInt(this::turnsUntilLeaving)
                        .thenComparingInt(Customer::getArrivalTurn));
    }

    //The staff command that moves this customer one step closer to being served. */
    public String nextStep(Customer customer) {
        return switch (customer.getStatus()) {
            case WAITING -> "seat " + customer.getId();
            case SEATED -> "order " + customer.getId();
            case ORDERED -> (customer.getOrder().isReady() ? "serve " : "cook ") + customer.getId();
            default -> "nothing; " + customer.getId() + " has already been served";
        };
    }
}
