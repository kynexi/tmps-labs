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
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option.");
                }
            } catch (IllegalArgumentException e) {
                // invalid input from the user: show the message and go back to the menu
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Bye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Finance Tracker ===");
        System.out.println("1. Add transaction");
        System.out.println("2. View history");
        System.out.println("3. Delete transaction");
        System.out.println("4. Summary (income, expenses, balance)");
        System.out.println("5. Filter transactions");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    // ---------- actions ----------

    private void addTransaction() {
        TransactionType type = readType();
        double amount = parseAmount(ask("Amount: "));
        String description = ask("Description: ");
        String category = ask("Category (empty = general): ");
        String dateText = ask("Date dd/MM/yyyy (empty = today): ");
        LocalDate date = dateText.isBlank() ? LocalDate.now() : parseDate(dateText);

        // the Transaction constructor rejects bad amounts / empty descriptions
        transactionService.addTransaction(new Transaction(amount, description, type, date, category));
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
        System.out.printf("Total income:   %.2f%n", summaryService.getTotalIncome());
        System.out.printf("Total expenses: %.2f%n", summaryService.getTotalExpenses());
        System.out.printf("Balance:        %.2f%n", summaryService.getBalance());

        System.out.println("Expenses by category:");
        Map<String, Double> byCategory = summaryService.getExpensesByCategory();
        if (byCategory.isEmpty()) {
            System.out.println("  (no expenses yet)");
        }
        for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
            System.out.printf("  %-15s %.2f%n", entry.getKey(), entry.getValue());
        }
    }

    private void filterTransactions() {
        System.out.println("Filter by: 1) category  2) income/expense  3) date range");
        String choice = ask("Choose: ");

        TransactionFilter filter = switch (choice) {
            case "1" -> new CategoryFilter(ask("Category: "));
            case "2" -> new TypeFilter(readType());
            case "3" -> new DateRangeFilter(
                    parseDate(ask("From dd/MM/yyyy: ")),
                    parseDate(ask("To dd/MM/yyyy: ")));
            default -> throw new IllegalArgumentException("Unknown filter.");
        };

        // numbers are not shown here, because they would not match the numbers used for deleting
        printTransactions(transactionService.filter(filter), false);
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
            System.out.printf("%s %s | %-7s | %9.2f | %s | %s%n",
                    prefix, t.getDate().format(DATE_FORMAT), t.getType(), t.getAmount(),
                    t.getCategory(), t.getDescription());
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