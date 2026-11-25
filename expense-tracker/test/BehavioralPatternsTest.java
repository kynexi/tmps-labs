import categorization.CategoryChain;
import categorization.CategoryHandler;
import categorization.CategoryRequest;
import categorization.IncomeCategoryHandler;
import categorization.KeywordCategoryHandler;
import command.AddTransactionCommand;
import command.CommandHistory;
import command.DeleteTransactionCommand;
import command.ImportStatementCommand;
import config.AppSettings;
import facade.FinanceFacade;
import importer.adapter.BankStatementCsvAdapter;
import model.Transaction;
import model.TransactionBuilder;
import model.TransactionType;
import observer.BudgetUsageWarning;
import observer.LargeExpenseAlert;
import observer.ObservableRepositoryDecorator;
import observer.TransactionListener;
import org.junit.jupiter.api.Test;
import repository.InMemoryTransactionRepository;
import repository.TransactionRepository;
import repository.decorator.BudgetLimitDecorator;
import service.SummaryService;
import service.TransactionService;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BehavioralPatternsTest {

    // ---------- helpers ----------

    private Transaction expense(String description, double amount) {
        return new TransactionBuilder()
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .description(description)
                .date(LocalDate.of(2026, 11, 10))
                .build();
    }

    private FinanceFacade facadeOver(TransactionRepository repo) {
        return new FinanceFacade(new TransactionService(repo), new SummaryService(repo));
    }

    // a listener that just remembers what it heard
    private static class RecordingListener implements TransactionListener {
        final List<String> events = new ArrayList<>();
        public void onAdded(Transaction t) { events.add("added " + t.getDescription()); }
        public void onRemoved(Transaction t) { events.add("removed " + t.getDescription()); }
    }

    // ---------- COMMAND ----------

    @Test
    void command_undoAddRemovesTheTransaction() {
        TransactionRepository repo = new InMemoryTransactionRepository();
        CommandHistory history = new CommandHistory();

        history.execute(new AddTransactionCommand(facadeOver(repo), expense("Linella", 100)));
        assertEquals(1, repo.findAll().size());

        history.undo();
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void command_undoDeleteBringsTheTransactionBack() {
        TransactionRepository repo = new InMemoryTransactionRepository();
        FinanceFacade facade = facadeOver(repo);
        Transaction t = expense("Rent", 5000);
        facade.add(t);
        CommandHistory history = new CommandHistory();

        history.execute(new DeleteTransactionCommand(facade, 0));
        assertTrue(repo.findAll().isEmpty());

        history.undo();
        assertSame(t, repo.findAll().get(0));
    }

    @Test
    void command_redoAfterUndoDoesItAgain() {
        TransactionRepository repo = new InMemoryTransactionRepository();
        CommandHistory history = new CommandHistory();

        history.execute(new AddTransactionCommand(facadeOver(repo), expense("Taxi", 80)));
        history.undo();
        history.redo();

        assertEquals(1, repo.findAll().size());
    }

    @Test
    void command_refusedCommandIsNotAddedToHistory() {
        TransactionRepository repo = new BudgetLimitDecorator(new InMemoryTransactionRepository(), 1000);
        FinanceFacade facade = facadeOver(repo);
        CommandHistory history = new CommandHistory();

        history.execute(new AddTransactionCommand(facade, expense("Groceries", 600)));
        assertThrows(IllegalArgumentException.class,
                () -> history.execute(new AddTransactionCommand(facade, expense("Phone", 500))));

        history.undo();                                   // undoes the groceries, not the phone
        assertTrue(repo.findAll().isEmpty());
        assertThrows(IllegalArgumentException.class, history::undo);   // nothing left
    }

    @Test
    void command_undoImportRemovesEverythingThatWasImported() {
        TransactionRepository repo = new InMemoryTransactionRepository();
        CommandHistory history = new CommandHistory();
        ImportStatementCommand importCommand = new ImportStatementCommand(facadeOver(repo),
                () -> List.of(expense("A", 10), expense("B", 20)));   // stub source

        history.execute(importCommand);
        assertEquals(2, repo.findAll().size());

        history.undo();
        assertTrue(repo.findAll().isEmpty());
    }

    // ---------- OBSERVER ----------

    @Test
    void observer_subscribedListenerIsNotifiedUnsubscribedIsNot() {
        ObservableRepositoryDecorator repo = new ObservableRepositoryDecorator(new InMemoryTransactionRepository());
        RecordingListener listener = new RecordingListener();

        repo.subscribe(listener);
        repo.add(expense("A", 10));
        repo.unsubscribe(listener);
        repo.add(expense("B", 20));

        assertEquals(List.of("added A"), listener.events);
    }

    @Test
    void observer_refusedExpenseNotifiesNobody() {
        ObservableRepositoryDecorator repo = new ObservableRepositoryDecorator(
                new BudgetLimitDecorator(new InMemoryTransactionRepository(), 1000));
        RecordingListener listener = new RecordingListener();
        repo.subscribe(listener);

        repo.add(expense("Groceries", 600));
        assertThrows(IllegalArgumentException.class, () -> repo.add(expense("Phone", 500)));

        assertEquals(List.of("added Groceries"), listener.events);
    }

    @Test
    void observer_largeExpenseAlertOnlyAboveThreshold() {
        ObservableRepositoryDecorator repo = new ObservableRepositoryDecorator(new InMemoryTransactionRepository());
        List<String> messages = new ArrayList<>();
        repo.subscribe(new LargeExpenseAlert(3000, messages::add));

        repo.add(expense("Coffee", 50));
        repo.add(expense("Laptop", 15000));

        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("Laptop"));
    }

    @Test
    void observer_budgetWarningAppearsAt80Percent() {
        ObservableRepositoryDecorator repo = new ObservableRepositoryDecorator(new InMemoryTransactionRepository());
        List<String> messages = new ArrayList<>();
        repo.subscribe(new BudgetUsageWarning(new SummaryService(repo), 1000, 0.8, messages::add));

        repo.add(expense("A", 500));     // 50% -> no warning
        assertTrue(messages.isEmpty());

        repo.add(expense("B", 400));     // 90% -> warning
        assertEquals(1, messages.size());
    }

    // ---------- CHAIN OF RESPONSIBILITY ----------

    private CategoryChain sampleChain() {
        CategoryHandler head = new IncomeCategoryHandler();
        head.linkWith(new KeywordCategoryHandler("food", "linella"))
                .linkWith(new KeywordCategoryHandler("transport", "taxi"));
        return new CategoryChain(head);
    }

    @Test
    void chain_matchingHandlerGivesTheCategory() {
        CategoryChain chain = sampleChain();

        assertEquals("food", chain.resolve(new CategoryRequest("Linella Botanica", 300, TransactionType.EXPENSE)));
        assertEquals("transport", chain.resolve(new CategoryRequest("Taxi home", 80, TransactionType.EXPENSE)));
    }

    @Test
    void chain_unhandledRequestEndsWithDefaultCategory() {
        String category = sampleChain().resolve(new CategoryRequest("Something new", 10, TransactionType.EXPENSE));

        assertEquals(AppSettings.getInstance().getDefaultCategory(), category);
    }

    @Test
    void chain_firstHandlerThatKnowsWins() {
        // income handler is first, so an income with "linella" in it is still "income"
        String category = sampleChain().resolve(new CategoryRequest("Refund from Linella", 50, TransactionType.INCOME));

        assertEquals("income", category);
    }

    @Test
    void chain_ruleAddedAtRuntimeIsCheckedFirst() {
        CategoryChain chain = sampleChain();
        chain.addFirst(new KeywordCategoryHandler("groceries", "linella"));

        assertEquals("groceries", chain.resolve(new CategoryRequest("Linella", 300, TransactionType.EXPENSE)));
    }

    // ---------- PATTERNS TOGETHER ----------

    @Test
    void importGoesThroughCommandAdapterChainAndObserver() {
        ObservableRepositoryDecorator repo = new ObservableRepositoryDecorator(new InMemoryTransactionRepository());
        RecordingListener listener = new RecordingListener();
        repo.subscribe(listener);
        CommandHistory history = new CommandHistory();
        String csv = """
                Data;Detalii;Debit;Credit
                03.11.2026;Linella Botanica;342,75;
                """;

        history.execute(new ImportStatementCommand(facadeOver(repo),
                new BankStatementCsvAdapter(new StringReader(csv), sampleChain())));

        assertEquals("food", repo.findAll().get(0).getCategory());          // chain picked the category
        assertEquals(List.of("added Linella Botanica"), listener.events);   // observer heard it

        history.undo();                                                     // command undone
        assertTrue(repo.findAll().isEmpty());
        assertEquals("removed Linella Botanica", listener.events.get(1));
    }
}