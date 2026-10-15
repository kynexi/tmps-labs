import java.util.Map;
import java.util.TreeMap;

// totals, balance, expenses by category
public class SummaryService {
    private final TransactionRepository repository;

    public SummaryService(TransactionRepository repository) {
        this.repository = repository;
    }

    public double getTotalIncome() {
        return totalOf(TransactionType.INCOME);
    }

    public double getTotalExpenses() {
        return totalOf(TransactionType.EXPENSE);
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    // category -> total spent, sorted alphabetically by category
    public Map<String, Double> getExpensesByCategory() {
        Map<String, Double> result = new TreeMap<>();
        for (Transaction t : repository.findAll()) {
            if (t.getType() == TransactionType.EXPENSE) {
                result.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return result;
    }

    // one loop instead of the same loop copy-pasted in several methods
    private double totalOf(TransactionType type) {
        double total = 0;
        for (Transaction t : repository.findAll()) {
            if (t.getType() == type) {
                total += t.getAmount();
            }
        }
        return total;
    }
}