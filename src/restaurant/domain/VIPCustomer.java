package restaurant.domain;

/** A VIP customer: loses 5 satisfaction per waiting turn and gets 10% off the bill. */
public class VIPCustomer extends Customer {
    private static final int SATISFACTION_LOSS = 5;
    private static final int DISCOUNT_PERCENT = 10;

    public VIPCustomer(String id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    /** 10% off, rounded to the nearest cent (half a cent rounds up), e.g. $15.00 -> $13.50. */
    @Override
    public int calculateBill(MenuItem item) {
        return (item.getPriceCents() * (100 - DISCOUNT_PERCENT) + 50) / 100;
    }
}
