package filter;

import model.Transaction;

public class AmountFilter implements TransactionFilter {
    private final double min, max;
    public AmountFilter(double min, double max) {
        if (min > max) throw new IllegalArgumentException("'Min' cannot be bigger than 'Max'.");
        this.min = min; this.max = max;
    }
    public boolean matches(Transaction t) {
        return t.getAmount() >= min && t.getAmount() <= max;
    }
}