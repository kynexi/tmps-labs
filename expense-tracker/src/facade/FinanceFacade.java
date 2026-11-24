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
 * FACADE: one simple entry point for import / search / export.
 * It only delegates to the subsystem; it does not calculate or store anything itself.
 */
public class FinanceFacade {
    private final TransactionService transactions;
    private final SummaryService summary;

    public FinanceFacade(TransactionService transactions, SummaryService summary) {
        this.transactions = transactions;
        this.summary = summary;
    }

    // source (Adapter) -> service -> repository (Decorators)
    public ImportResult importFrom(TransactionSource source) {
        int imported = 0;
        List<String> rejected = new ArrayList<>();
        for (Transaction t : source.fetch()) {
            try {
                transactions.addTransaction(t);
                imported++;
            } catch (IllegalArgumentException e) {
                rejected.add(t.getDescription() + ": " + e.getMessage());
            }
        }
        return new ImportResult(imported, List.copyOf(rejected));
    }

    // works with a single filter or a Composite group, the facade doesn't care
    public List<Transaction> search(TransactionFilter filter) {
        return transactions.filter(filter);
    }

    // Composite filter -> Factory Method export (Lab 1)
    public String exportMatching(TransactionFilter filter, ExportCreator creator, String baseName) {
        return creator.exportToFile(transactions.filter(filter), baseName);
    }

    public double balance() {
        return summary.getBalance();
    }
}