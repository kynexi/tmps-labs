package observer;

import model.Transaction;
import repository.TransactionRepository;
import repository.decorator.RepositoryDecorator;

import java.util.ArrayList;
import java.util.List;

// SUBJECT, built as one more Decorator from lab 2
public class ObservableRepositoryDecorator extends RepositoryDecorator implements TransactionPublisher {
    private final List<TransactionListener> listeners = new ArrayList<>();

    public ObservableRepositoryDecorator(TransactionRepository wrapped) { super(wrapped); }

    @Override public void subscribe(TransactionListener l) { if (!listeners.contains(l)) listeners.add(l); }
    @Override public void unsubscribe(TransactionListener l) { listeners.remove(l); }

    @Override
    public void add(Transaction t) {
        super.add(t);                                    // if the budget refuses, nobody is notified
        for (TransactionListener l : List.copyOf(listeners)) l.onAdded(t);
    }

    @Override
    public void remove(int index) {
        Transaction removed = wrapped.findAll().get(index);
        super.remove(index);
        for (TransactionListener l : List.copyOf(listeners)) l.onRemoved(removed);
    }
}