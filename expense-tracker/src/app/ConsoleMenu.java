package app;

import categorization.CategoryChain;
import categorization.CategoryRequest;
import categorization.KeywordCategoryHandler;
import command.AddTransactionCommand;
import command.CommandHistory;
import command.DeleteTransactionCommand;
import command.ImportStatementCommand;
import config.AppSettings;
import export.*;
import facade.FinanceFacade;
import facade.ImportResult;
import filter.*;
import filter.composite.AndFilter;
import filter.composite.FilterGroup;
import filter.composite.OrFilter;
import importer.adapter.BankStatementCsvAdapter;
import model.*;
import observer.TransactionListener;
import observer.TransactionPublisher;
import service.*;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The only class that talks to the user (Scanner + System.out).
 * All the real work is delegated to the services, the facade and the commands.
 */
public class ConsoleMenu {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FinanceFacade facade;
    private final TransactionService transactionService;
    private final SummaryService summaryService;
    private final CommandHistory history;
    private final TransactionPublisher publisher;
    private final CategoryChain categories;
    private final TransactionListener largeExpenseAlert;
    private final TransactionListener budgetWarning;
    private boolean alertsOn = true;   // Main subscribes both listeners at start

    private final Scanner scanner = new Scanner(System.in);
    // SINGLETON in use: the same settings object everywhere
    private final AppSettings settings = AppSettings.getInstance();

    public ConsoleMenu(FinanceFacade facade, TransactionService transactionService, SummaryService summaryService,
                       CommandHistory history, TransactionPublisher publisher, CategoryChain categories,
                       TransactionListener largeExpenseAlert, TransactionListener budgetWarning) {
        this.facade = facade;
        this.transactionService = transactionService;
        this.summaryService = summaryService;
        this.history = history;
        this.publisher = publisher;
        this.categories = categories;
        this.largeExpenseAlert = largeExpenseAlert;
        this.budgetWarning = budgetWarning;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addTransaction();
                    case "2" -> printTransactions(transactionService.getTransactions(), true);
                    case "3" -> deleteTransaction();
                    case "4" -> showSummary();
                    case "5" -> filterTransactions();
                    case "6" -> exportTransactions();
                    case "7" -> changeSettings();
                    case "8" -> importStatement();
                    case "9" -> System.out.println("Undone: " + history.undo());
                    case "10" -> System.out.println("Redone: " + history.redo());
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option.");
                }
            } catch (IllegalArgumentException e) {
                // invalid input or a rule broken (e.g. budget exceeded): show the message and go back
                System.out.println("Error: " + e.getMessage());
            } catch (UncheckedIOException e) {
                System.out.println("File error: " + e.getMessage());
            }
        }
        System.out.println("Bye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Finance Tracker (" + settings.getCurrency() + ") ===");
        System.out.println("1. Add transaction");
        System.out.println("2. View history");
        System.out.println("3. Delete transaction");
        System.out.println("4. Summary (income, expenses, balance)");
        System.out.println("5. Filter transactions");
        System.out.println("6. Export to file");
        System.out.println("7. Settings");
        System.out.println("8. Import bank statement (CSV)");
        System.out.println("9. Undo");
        System.out.println("10. Redo");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    // ---------- actions ----------

    private void addTransaction() {
        TransactionType type = readType();
        double amount = parseAmount(ask("Amount: "));
        String description = ask("Description: ");
        String category = ask("Category (empty = automatic): ");
        String dateText = ask("Date dd/MM/yyyy (empty = today): ");

        // CHAIN OF RESPONSIBILITY: no category typed -> the chain picks one
        if (category.isBlank()) {
            category = categories.resolve(new CategoryRequest(description, amount, type));
        }

        // BUILDER in use
        Transaction transaction = new TransactionBuilder()
                .type(type)
                .amount(amount)
                .description(description)
                .category(category)
                .date(dateText.isBlank() ? null : parseDate(dateText))
                .build();

        // COMMAND: goes into the history, so it can be undone
        history.execute(new AddTransactionCommand(facade, transaction));
        System.out.println("Transaction added (category: " + transaction.getCategory() + ").");
    }

    private void deleteTransaction() {
        List<Transaction> all = transactionService.getTransactions();
        if (all.isEmpty()) {
            System.out.println("Nothing to delete.");
            return;
        }
        printTransactions(all, true);
        int number = parseNumber(ask("Number to delete: "));
        history.execute(new DeleteTransactionCommand(facade, number - 1)); // the user sees 1-based numbers
        System.out.println("Transaction deleted. (Undo with 9)");
    }

    private void showSummary() {
        String c = settings.getCurrency();
        System.out.printf("Total income:   %.2f %s%n", summaryService.getTotalIncome(), c);
        System.out.printf("Total expenses: %.2f %s%n", summaryService.getTotalExpenses(), c);
        System.out.printf("Balance:        %.2f %s%n", summaryService.getBalance(), c);

        System.out.println("Expenses by category:");
        Map<String, Double> byCategory = summaryService.getExpensesByCategory();
        if (byCategory.isEmpty()) {
            System.out.println("  (no expenses yet)");
        }
        for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
            System.out.printf("  %-15s %.2f %s%n", entry.getKey(), entry.getValue(), c);
        }
    }

    private void filterTransactions() {
        TransactionFilter filter = readFilter();
        // numbers are not shown here, because they would not match the numbers used for deleting
        printTransactions(facade.search(filter), false);
    }

    private void exportTransactions() {
        String what = ask("Export: 1) all transactions  2) only filtered: ").trim();
        // an empty AND group matches everything, so "all" is just another filter
        TransactionFilter filter = switch (what) {
            case "1" -> new AndFilter();
            case "2" -> readFilter();
            default -> throw new IllegalArgumentException("Choose 1 or 2.");
        };

        String format = ask("Export format: 1) CSV  2) TXT: ").trim();
        // FACTORY METHOD in use
        ExportCreator creator = switch (format) {
            case "1" -> new CsvExportCreator();
            case "2" -> new TextExportCreator();
            default -> throw new IllegalArgumentException("Unknown format.");
        };

        String fileName = facade.exportMatching(filter, creator, "export");
        System.out.println("Saved to " + fileName);
    }

    private void changeSettings() {
        System.out.println("Currency: " + settings.getCurrency()
                + " | Default category: " + settings.getDefaultCategory()
                + " | Alerts: " + (alertsOn ? "ON" : "OFF"));
        String choice = ask("Change: 1) currency  2) default category  3) add category rule"
                + "  4) turn alerts " + (alertsOn ? "off" : "on") + "  (empty = back): ").trim();
        switch (choice) {
            case "1" -> settings.setCurrency(ask("New currency (e.g. MDL, EUR, USD): "));
            case "2" -> settings.setDefaultCategory(ask("New default category: "));
            case "3" -> addCategoryRule();
            case "4" -> toggleAlerts();
            default -> {
            }
        }
    }

    // CHAIN OF RESPONSIBILITY changed at runtime: the new rule is checked first
    private void addCategoryRule() {
        String keyword = ask("Word in the description (e.g. darwin): ").trim();
        String category = ask("Category for it (e.g. tech): ").trim();
        if (keyword.isEmpty() || category.isEmpty()) {
            throw new IllegalArgumentException("Keyword and category cannot be empty.");
        }
        categories.addFirst(new KeywordCategoryHandler(category.toLowerCase(), keyword));
        System.out.println("Rule added: '" + keyword + "' -> " + category.toLowerCase());
    }

    // OBSERVER changed at runtime: subscribe / unsubscribe the alerts
    private void toggleAlerts() {
        if (alertsOn) {
            publisher.unsubscribe(largeExpenseAlert);
            publisher.unsubscribe(budgetWarning);
        } else {
            publisher.subscribe(largeExpenseAlert);
            publisher.subscribe(budgetWarning);
        }
        alertsOn = !alertsOn;
        System.out.println("Alerts are now " + (alertsOn ? "ON" : "OFF") + ".");
    }

    // COMMAND + FACADE + ADAPTER + CHAIN in one path
    private void importStatement() {
        String path = ask("Path to bank CSV (e.g. samples/bank-statement.csv): ").trim();
        ImportStatementCommand command =
                new ImportStatementCommand(facade, BankStatementCsvAdapter.fromFile(path, categories));
        history.execute(command);

        ImportResult result = command.result();
        System.out.println("Imported: " + result.imported() + " (undo with 9)");
        if (!result.rejected().isEmpty()) {
            System.out.println("Skipped: " + result.rejected().size());
            for (String r : result.rejected()) {
                System.out.println("  - " + r);
            }
        }
    }

    // ---------- filters ----------

    private TransactionFilter readFilter() {
        System.out.println("Filter by: 1) category  2) income/expense  3) date range  4) word in description"
                + "  5) amount range  6) combine several (AND / OR)");
        String choice = ask("Choose: ").trim();

        return switch (choice) {
            case "1" -> new CategoryFilter(ask("Category: "));
            case "2" -> new TypeFilter(readType());
            case "3" -> new DateRangeFilter(
                    parseDate(ask("From dd/MM/yyyy: ")),
                    parseDate(ask("To dd/MM/yyyy: ")));
            case "4" -> new KeywordFilter(ask("Word: "));
            case "5" -> new AmountFilter(
                    parseAmount(ask("Min amount: ")),
                    parseAmount(ask("Max amount: ")));
            case "6" -> readFilterGroup();
            default -> throw new IllegalArgumentException("Unknown filter.");
        };
    }

    // COMPOSITE in use: a group is built from filters, and a filter can again be a group
    private TransactionFilter readFilterGroup() {
        String mode = ask("Combine with 1) AND (all must match)  2) OR (any can match): ").trim();
        FilterGroup group = switch (mode) {
            case "1" -> new AndFilter();
            case "2" -> new OrFilter();
            default -> throw new IllegalArgumentException("Choose 1 or 2.");
        };
        int count = parseNumber(ask("How many filters in the group? "));
        if (count < 2) {
            throw new IllegalArgumentException("A group needs at least 2 filters.");
        }
        for (int i = 1; i <= count; i++) {
            System.out.println("-- filter " + i + " of " + count + " --");
            group.add(readFilter()); // choosing 6 here creates a nested group
        }
        return group;
    }

    // ---------- printing ----------

    private void printTransactions(List<Transaction> list, boolean numbered) {
        if (list.isEmpty()) {
            System.out.println("No transactions.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Transaction t = list.get(i);
            String prefix = numbered ? (i + 1) + "." : "-";
            System.out.printf("%s %s | %-7s | %9.2f %s | %s | %s%n",
                    prefix, t.getDate().format(DATE_FORMAT), t.getType(), t.getAmount(),
                    settings.getCurrency(), t.getCategory(), t.getDescription());
        }
    }

    // ---------- reading input ----------

    private String ask(String question) {
        System.out.print(question);
        return scanner.nextLine();
    }

    private TransactionType readType() {
        String answer = ask("Type (1 = income, 2 = expense): ").trim();
        if (answer.equals("1")) {
            return TransactionType.INCOME;
        }
        if (answer.equals("2")) {
            return TransactionType.EXPENSE;
        }
        throw new IllegalArgumentException("Type must be 1 or 2.");
    }

    private double parseAmount(String text) {
        try {
            return Double.parseDouble(text.trim().replace(',', '.')); // accept 12,5 and 12.5
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a number.");
        }
    }

    private int parseNumber(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a whole number.");
        }
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date must look like 16/09/2026.");
        }
    }
}