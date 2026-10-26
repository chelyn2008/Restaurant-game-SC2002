package restaurant.domain;

/** loses 8 satisfaction per waiting turn and pays full price */
public class RegularCustomer extends Customer {
    private static final int SATISFACTION_LOSS = 8;

    public RegularCustomer(String id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "Regular";
    }
}
