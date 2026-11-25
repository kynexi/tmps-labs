package facade;

import export.ExportCreator;
import filter.TransactionFilter;
import importer.TransactionSource;
import model.Transaction;
import service.SummaryService;
import service.TransactionService;

import java.util.ArrayList;
import java.util.List;

/**
 * FACADE: one simple entry point for the subsystem.
 * It only delegates; it does not calculate or store anything itself.
 * It is also the RECEIVER of the commands (add / remove / import).
 */
public class FinanceFacade {
    private final TransactionService transactions;
    private final SummaryService summary;

    public FinanceFacade(TransactionService transactions, SummaryService summary) {
        this.transactions = transactions;
        this.summary = summary;
    }

    public void add(Transaction transaction) {
        transactions.addTransaction(transaction);
    }

    public void remove(Transaction transaction) {
        transactions.removeTransaction(transaction);
    }

    public Transaction transactionAt(int index) {
        return transactions.getTransaction(index);
    }

    // source (Adapter) -> service -> repository (Decorators, Observer)
    public ImportResult importFrom(TransactionSource source) {
        List<Transaction> added = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        for (Transaction t : source.fetch()) {
            try {
                transactions.addTransaction(t);
                added.add(t);
            } catch (IllegalArgumentException e) {
                rejected.add(t.getDescription() + ": " + e.getMessage());
            }
        }
        return new ImportResult(List.copyOf(added), List.copyOf(rejected));
    }

    // works with a single filter or a Composite group
    public List<Transaction> search(TransactionFilter filter) {
        return transactions.filter(filter);
    }

    // Composite filter -> Factory Method export
    public String exportMatching(TransactionFilter filter, ExportCreator creator, String baseName) {
        return creator.exportToFile(transactions.filter(filter), baseName);
    }

    public double balance() {
        return summary.getBalance();
    }
}