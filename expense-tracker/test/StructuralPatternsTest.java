import facade.FinanceFacade;
import facade.ImportResult;
import filter.CategoryFilter;
import filter.TransactionFilter;
import filter.TypeFilter;
import filter.composite.AndFilter;
import filter.composite.OrFilter;
import importer.adapter.BankStatementCsvAdapter;
import model.Transaction;
import model.TransactionBuilder;
import model.TransactionType;
import org.junit.jupiter.api.Test;
import repository.InMemoryTransactionRepository;
import repository.TransactionRepository;
import repository.decorator.BudgetLimitDecorator;
import repository.decorator.LoggingRepositoryDecorator;
import service.SummaryService;
import service.TransactionService;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructuralPatternsTest {

    // helper: a simple expense in November 2026
    private Transaction expense(String description, double amount, String category) {
        return new TransactionBuilder()
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .description(description)
                .category(category)
                .date(LocalDate.of(2026, 11, 10))
                .build();
    }

    // ---------- COMPOSITE ----------

    @Test
    void composite_leafAndGroupWorkTheSameWay() {
        Transaction food = expense("Linella", 100, "food");

        TransactionFilter leaf = new CategoryFilter("food");
        TransactionFilter group = new AndFilter(new CategoryFilter("food"));

        // both are just TransactionFilter, no instanceof needed
        assertTrue(leaf.matches(food));
        assertTrue(group.matches(food));
    }

    @Test
    void composite_groupsCanBeNested() {
        Transaction food = expense("Linella", 100, "food");
        Transaction rent = expense("Rent", 5000, "rent");

        // EXPENSE AND (food OR transport)
        TransactionFilter filter = new AndFilter(
                new TypeFilter(TransactionType.EXPENSE),
                new OrFilter(new CategoryFilter("food"), new CategoryFilter("transport")));

        assertTrue(filter.matches(food));
        assertFalse(filter.matches(rent));
    }

    // ---------- DECORATOR ----------

    @Test
    void decorator_budgetBlocksExpenseOverTheLimit() {
        TransactionRepository repo = new BudgetLimitDecorator(new InMemoryTransactionRepository(), 1000);

        repo.add(expense("Groceries", 600, "food"));

        assertThrows(IllegalArgumentException.class, () -> repo.add(expense("Phone", 500, "tech")));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void decorator_orderOfStackingDoesNotMatter() {
        List<String> log1 = new ArrayList<>();
        List<String> log2 = new ArrayList<>();

        TransactionRepository logOutside = new LoggingRepositoryDecorator(
                new BudgetLimitDecorator(new InMemoryTransactionRepository(), 1000), log1::add);
        TransactionRepository budgetOutside = new BudgetLimitDecorator(
                new LoggingRepositoryDecorator(new InMemoryTransactionRepository(), log2::add), 1000);

        for (TransactionRepository repo : List.of(logOutside, budgetOutside)) {
            repo.add(expense("Groceries", 600, "food"));
            assertThrows(IllegalArgumentException.class, () -> repo.add(expense("Phone", 500, "tech")));
            assertEquals(1, repo.findAll().size());
        }

        // in both orders only the accepted expense was logged
        assertEquals(1, log1.size());
        assertEquals(1, log2.size());
    }

    // ---------- ADAPTER ----------

    @Test
    void adapter_translatesBankRowToTransaction() {
        String csv = """
                Data;Detalii;Debit;Credit
                03.11.2026;Darwin;1 234,50;
                """;

        Transaction t = new BankStatementCsvAdapter(new StringReader(csv)).fetch().get(0);

        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals(1234.50, t.getAmount(), 0.001);
        assertEquals(LocalDate.of(2026, 11, 3), t.getDate());
        assertEquals("Darwin", t.getDescription());
    }

    @Test
    void adapter_creditColumnBecomesIncome() {
        String csv = """
                Data;Detalii;Debit;Credit
                01.11.2026;Salariu;;18 000,00
                """;

        Transaction t = new BankStatementCsvAdapter(new StringReader(csv)).fetch().get(0);

        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals(18000.00, t.getAmount(), 0.001);
    }

    // ---------- FACADE ----------

    @Test
    void facade_importSavesTransactionsFromTheSource() {
        TransactionRepository repo = new InMemoryTransactionRepository();
        FinanceFacade facade = new FinanceFacade(new TransactionService(repo), new SummaryService(repo));

        // a stub source instead of a real file
        ImportResult result = facade.importFrom(() -> List.of(
                expense("A", 10, "food"),
                expense("B", 20, "food")));

        assertEquals(2, result.imported());
        assertEquals(2, repo.findAll().size());
    }

    @Test
    void facade_reportsRejectedTransactionsInsteadOfCrashing() {
        // full path: Facade -> Service -> Budget Decorator -> Repository
        TransactionRepository repo = new BudgetLimitDecorator(new InMemoryTransactionRepository(), 1000);
        FinanceFacade facade = new FinanceFacade(new TransactionService(repo), new SummaryService(repo));

        ImportResult result = facade.importFrom(() -> List.of(
                expense("A", 600, "food"),
                expense("B", 500, "food")));

        assertEquals(1, result.imported());
        assertEquals(1, result.rejected().size());
    }
}