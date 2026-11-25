package app;

import categorization.CategoryChain;
import categorization.CategoryHandler;
import categorization.IncomeCategoryHandler;
import categorization.KeywordCategoryHandler;
import command.CommandHistory;
import facade.FinanceFacade;
import observer.BudgetUsageWarning;
import observer.LargeExpenseAlert;
import observer.ObservableRepositoryDecorator;
import observer.TransactionListener;
import repository.FileTransactionRepository;
import repository.TransactionRepository;
import repository.decorator.BudgetLimitDecorator;
import repository.decorator.LoggingRepositoryDecorator;
import service.SummaryService;
import service.TransactionService;

public class Main {
    private static final double MONTHLY_BUDGET = 20_000;
    private static final double LARGE_EXPENSE = 3_000;
    private static final double WARN_AT = 0.8;   // warn at 80% of the budget

    public static void main(String[] args) {
        // DECORATORS: Logging -> Observable (subject) -> Budget -> File
        TransactionRepository file = new FileTransactionRepository("transactions.txt");
        ObservableRepositoryDecorator observable =
                new ObservableRepositoryDecorator(new BudgetLimitDecorator(file, MONTHLY_BUDGET));
        TransactionRepository repository = new LoggingRepositoryDecorator(observable);

        TransactionService transactionService = new TransactionService(repository);
        SummaryService summaryService = new SummaryService(repository);
        FinanceFacade facade = new FinanceFacade(transactionService, summaryService);

        // OBSERVER: subscribed at start, can be switched off in Settings
        TransactionListener largeExpenseAlert = new LargeExpenseAlert(LARGE_EXPENSE, System.out::println);
        TransactionListener budgetWarning =
                new BudgetUsageWarning(summaryService, MONTHLY_BUDGET, WARN_AT, System.out::println);
        observable.subscribe(largeExpenseAlert);
        observable.subscribe(budgetWarning);

        // CHAIN OF RESPONSIBILITY: the order is decided here, not inside the handlers
        CategoryHandler head = new IncomeCategoryHandler();
        head.linkWith(new KeywordCategoryHandler("food", "linella", "kaufland", "nr1", "fourchette"))
                .linkWith(new KeywordCategoryHandler("transport", "taxi", "yandex", "bolt", "trolleybus"))
                .linkWith(new KeywordCategoryHandler("utilities", "orange", "moldcell", "premier energy"))
                .linkWith(new KeywordCategoryHandler("rent", "chirie", "rent"));
        CategoryChain categories = new CategoryChain(head);

        new ConsoleMenu(facade, transactionService, summaryService, new CommandHistory(),
                observable, categories, largeExpenseAlert, budgetWarning).run();
    }
}