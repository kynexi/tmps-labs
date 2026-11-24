package repository.decorator;

import model.Transaction;
import model.TransactionType;
import repository.TransactionRepository;

import java.time.YearMonth;

// refuses an expense that would push its month over the limit
public class BudgetLimitDecorator extends RepositoryDecorator {
    private final double monthlyLimit;

    public BudgetLimitDecorator(TransactionRepository wrapped, double monthlyLimit) {
        super(wrapped);
        if (!(monthlyLimit > 0)) {
            throw new IllegalArgumentException("Monthly limit must be positive.");
        }
        this.monthlyLimit = monthlyLimit;
    }

    @Override
    public void add(Transaction transaction) {
        if (transaction.getType() == TransactionType.EXPENSE) {
            YearMonth month = YearMonth.from(transaction.getDate());
            double spent = spentIn(month);
            if (spent + transaction.getAmount() > monthlyLimit) {
                throw new BudgetExceededException(String.format(
                        "Budget exceeded for %s: limit %.2f, already spent %.2f, this expense %.2f.",
                        month, monthlyLimit, spent, transaction.getAmount()));
            }
        }
        super.add(transaction); // only reached when the check passed
    }

    private double spentIn(YearMonth month) {
        double total = 0;
        for (Transaction t : wrapped.findAll()) {
            if (t.getType() == TransactionType.EXPENSE && YearMonth.from(t.getDate()).equals(month)) {
                total += t.getAmount();
            }
        }
        return total;
    }
}