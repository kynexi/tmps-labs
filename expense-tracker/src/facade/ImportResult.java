package facade;

import model.Transaction;

import java.util.List;

// keeps the added transactions, so an import can be undone (Command)
public record ImportResult(List<Transaction> added, List<String> rejected) {
    public int imported() {
        return added.size();
    }
}