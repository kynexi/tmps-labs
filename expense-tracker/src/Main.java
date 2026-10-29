public class Main {
    public static void main(String[] args) {
        TransactionRepository repository = new InMemoryTransactionRepository();

        TransactionService transactionService = new TransactionService(repository);
        SummaryService summaryService = new SummaryService(repository);

        new ConsoleMenu(transactionService, summaryService).run();

    }
}