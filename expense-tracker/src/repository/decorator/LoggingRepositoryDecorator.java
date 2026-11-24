package repository.decorator;

import model.Transaction;
import repository.TransactionRepository;

import java.util.function.Consumer;

// writes a log line after every successful change
public class LoggingRepositoryDecorator extends RepositoryDecorator {
    private final Consumer<String> log;

    public LoggingRepositoryDecorator(TransactionRepository wrapped) {
        this(wrapped, System.out::println);
    }

    // the log target is injectable so tests can capture the lines
    public LoggingRepositoryDecorator(TransactionRepository wrapped, Consumer<String> log) {
        super(wrapped);
        this.log = log;
    }

    @Override
    public void add(Transaction transaction) {
        super.add(transaction);                       // first the real work...
        log.accept("[LOG] added   " + describe(transaction)); // ...then the extra behaviour
    }

    @Override
    public void remove(int index) {
        Transaction removed = wrapped.findAll().get(index);
        super.remove(index);
        log.accept("[LOG] removed " + describe(removed));
    }

    private static String describe(Transaction t) {
        return t.getDate() + " " + t.getType() + " " + t.getAmount()
                + " [" + t.getCategory() + "] " + t.getDescription();
    }
}