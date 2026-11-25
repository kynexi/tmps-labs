package observer;

import model.Transaction;

public interface TransactionListener {
    void onAdded(Transaction transaction);
    void onRemoved(Transaction transaction);
}