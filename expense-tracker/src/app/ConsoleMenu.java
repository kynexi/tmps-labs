package app;

import config.AppSettings;
import export.*;
import filter.*;
import model.*;
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
 * All the real work is delegated to the services.
 */
public class ConsoleMenu {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TransactionService transactionService;
    private final SummaryService summaryService;
    private final Scanner scanner = new Scanner(System.in);
    // SINGLETON in use: the same settings object everywhere
    private final AppSettings settings = AppSettings.getInstance();

    public ConsoleMenu(TransactionService transactionService, SummaryService summaryService) {
        this.transactionService = transactionService;
        this.summaryService = summaryService;
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
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option.");
                }
            } catch (IllegalArgumentException e) {
                // invalid input from the user: show the message and go back to the menu
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
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    // ---------- actions ----------

    private void addTransaction() {
        TransactionType type = readType();
        double amount = parseAmount(ask("Amount: "));
        String description = ask("Description: ");
        String category = ask("Category (empty = " + settings.getDefaultCategory() + "): ");
        String dateText = ask("Date dd/MM/yyyy (empty = today): ");

        // BUILDER in use: each value has a name, empty category/date get the defaults
        Transaction transaction = new TransactionBuilder()
                .type(type)
                .amount(amount)
                .description(description)
                .category(category)
                .date(dateText.isBlank() ? null : parseDate(dateText))
                .build();

        transactionService.addTransaction(transaction);
        System.out.println("Transaction added.");
    }

    private void deleteTransaction() {
        List<Transaction> all = transactionService.getTransactions();
        if (all.isEmpty()) {
            System.out.println("Nothing to delete.");
            return;
        }
        printTransactions(all, true);
        int number = parseNumber(ask("Number to delete: "));
        transactionService.removeTransaction(number - 1); // the user sees 1-based numbers
        System.out.println("Transaction deleted.");
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
        System.out.println("Filter by: 1) category  2) income/expense  3) date range  4) word in description  5) amount range");
        String choice = ask("Choose: ");

        TransactionFilter filter = switch (choice) {
            case "1" -> new CategoryFilter(ask("Category: "));
            case "2" -> new TypeFilter(readType());
            case "3" -> new DateRangeFilter(
                    parseDate(ask("From dd/MM/yyyy: ")),
                    parseDate(ask("To dd/MM/yyyy: ")));
            case "4" -> new KeywordFilter(ask("Word: "));
            case "5" -> new AmountFilter(
                    parseAmount(ask("Min amount: ")),
                    parseAmount(ask("Max amount: ")));
            default -> throw new IllegalArgumentException("Unknown filter.");
        };

        // numbers are not shown here, because they would not match the numbers used for deleting
        printTransactions(transactionService.filter(filter), false);
    }

    private void exportTransactions() {
        String choice = ask("Export format: 1) CSV  2) TXT: ").trim();

        // FACTORY METHOD in use: we pick a creator, it makes the right exporter by itself
        ExportCreator creator = switch (choice) {
            case "1" -> new CsvExportCreator();
            case "2" -> new TextExportCreator();
            default -> throw new IllegalArgumentException("Unknown format.");
        };

        String fileName = creator.exportToFile(transactionService.getTransactions(), "export");
        System.out.println("Saved to " + fileName);
    }

    private void changeSettings() {
        System.out.println("Currency: " + settings.getCurrency()
                + " | Default category: " + settings.getDefaultCategory());
        String choice = ask("Change: 1) currency  2) default category  (empty = back): ").trim();
        switch (choice) {
            case "1" -> settings.setCurrency(ask("New currency (e.g. MDL, EUR, USD): "));
            case "2" -> settings.setDefaultCategory(ask("New default category: "));
            default -> {
            }
        }
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