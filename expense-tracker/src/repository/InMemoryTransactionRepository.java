package repository;

import model.Transaction;

import java.util.ArrayList;
import java.util.List;

// Keeps transactions in a list in memory
public class InMemoryTransactionRepository implements TransactionRepository {
    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public void add(Transaction transaction) {
        transactions.add(transaction);
    }

    @Override
    public void remove(int index) {
        transactions.remove(index);
    }

    @Override
    public List<Transaction> findAll() {
        // return a copy so nobody can change our list from outside
        return List.copyOf(transactions);
    }
}