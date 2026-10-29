package app;

public class Main {
    public static void main(String[] args) {
        // data is saved in this file (next to where you run the program)
        TransactionRepository repository = new FileTransactionRepository("transactions.txt");

        TransactionService transactionService = new TransactionService(repository);
        SummaryService summaryService = new SummaryService(repository);

        new ConsoleMenu(transactionService, summaryService).run();
    }
}