package repository.decorator;

import model.Transaction;
import repository.TransactionRepository;

import java.util.List;

// DECORATOR base: same interface as the repository, forwards everything to the wrapped one
public abstract class RepositoryDecorator implements TransactionRepository {
    protected final TransactionRepository wrapped;

    protected RepositoryDecorator(TransactionRepository wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Wrapped repository cannot be null.");
        }
        this.wrapped = wrapped;
    }

    @Override
    public void add(Transaction transaction) {
        wrapped.add(transaction);
    }

    @Override
    public void remove(int index) {
        wrapped.remove(index);
    }

    @Override
    public List<Transaction> findAll() {
        return wrapped.findAll();
    }
}