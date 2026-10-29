package filter;

import model.Transaction;

public interface TransactionFilter {
    boolean matches(Transaction transaction);
}