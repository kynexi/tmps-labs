package app;

import facade.FinanceFacade;
import repository.FileTransactionRepository;
import repository.TransactionRepository;
import repository.decorator.BudgetLimitDecorator;
import repository.decorator.LoggingRepositoryDecorator;
import service.SummaryService;
import service.TransactionService;

public class Main {
    private static final double MONTHLY_BUDGET = 20_000;

    public static void main(String[] args) {
        // DECORATOR: the rest of the app only sees TransactionRepository
        TransactionRepository repository =
                new LoggingRepositoryDecorator(
                        new BudgetLimitDecorator(
                                new FileTransactionRepository("transactions.txt"), MONTHLY_BUDGET));

        TransactionService transactionService = new TransactionService(repository);
        SummaryService summaryService = new SummaryService(repository);
        FinanceFacade facade = new FinanceFacade(transactionService, summaryService);

        new ConsoleMenu(facade, transactionService, summaryService).run();
    }
}