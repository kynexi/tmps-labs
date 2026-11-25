package service;
import model.Transaction;
import repository.TransactionRepository;
import filter.TransactionFilter;


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

    public Transaction getTransaction(int index) {
        List<Transaction> all = repository.findAll();
        if (index < 0 || index >= all.size()) {
            throw new IllegalArgumentException("There is no transaction with that number.");
        }
        return all.get(index);
    }

    public void removeTransaction(Transaction transaction) {
        int index = repository.findAll().indexOf(transaction);   // same object, Transaction has no equals()
        if (index < 0) throw new IllegalArgumentException("Transaction not found.");
        repository.remove(index);
    }
}