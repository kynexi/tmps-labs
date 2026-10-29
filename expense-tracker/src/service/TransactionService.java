package service;

import model.Transaction;

import java.util.ArrayList;
import java.util.List;

// Transaction management: add, delete, list, filter.
// No printing and no data (SRP + DIP).

public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public void addTransaction(Transaction transaction) {
        repository.add(transaction);
    }

    public void removeTransaction(int index) {
        if (index < 0 || index >= repository.findAll().size()) {
            throw new IllegalArgumentException("There is no transaction with that number.");
        }
        repository.remove(index);
    }

    public List<Transaction> getTransactions() {
        return repository.findAll();
    }

    public List<Transaction> filter(TransactionFilter filter) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : repository.findAll()) {
            if (filter.matches(t)) {
                result.add(t);
            }
        }
        return result;
    }
}