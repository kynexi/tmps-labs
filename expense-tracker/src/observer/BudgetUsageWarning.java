package observer;

import model.Transaction;
import model.TransactionType;
import service.SummaryService;

import java.time.YearMonth;
import java.util.function.Consumer;

public class BudgetUsageWarning implements TransactionListener {
    private final SummaryService summary;   // only reads, never writes, so no update loops
    private final double monthlyLimit;
    private final double warnAt;            // e.g. 0.8 = warn at 80%
    private final Consumer<String> out;

    public BudgetUsageWarning(SummaryService summary, double monthlyLimit, double warnAt, Consumer<String> out) {
        this.summary = summary;
        this.monthlyLimit = monthlyLimit;
        this.warnAt = warnAt;
        this.out = out;
    }

    @Override
    public void onAdded(Transaction t) {
        if (t.getType() != TransactionType.EXPENSE) return;
        YearMonth month = YearMonth.from(t.getDate());
        double spent = summary.getExpensesIn(month);
        if (spent >= monthlyLimit * warnAt) {
            out.accept(String.format("[WARNING] %.0f%% of the %s budget used (%.2f / %.2f)",
                    spent / monthlyLimit * 100, month, spent, monthlyLimit));
        }
    }

    @Override public void onRemoved(Transaction t) { }
}