package observer;

import model.Transaction;
import model.TransactionType;

import java.util.function.Consumer;

public class LargeExpenseAlert implements TransactionListener {
    private final double threshold;
    private final Consumer<String> out;

    public LargeExpenseAlert(double threshold, Consumer<String> out) {
        this.threshold = threshold;
        this.out = out;
    }

    @Override
    public void onAdded(Transaction t) {
        if (t.getType() == TransactionType.EXPENSE && t.getAmount() >= threshold) {
            out.accept("[ALERT] Large expense: " + t.getAmount() + " for " + t.getDescription());
        }
    }

    @Override public void onRemoved(Transaction t) { }
}