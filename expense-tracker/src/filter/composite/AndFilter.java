package filter.composite;

import filter.TransactionFilter;
import model.Transaction;

// matches only if ALL children match (an empty group matches everything)
public class AndFilter extends FilterGroup {
    public AndFilter(TransactionFilter... filters) {
        super(filters);
    }

    @Override
    public boolean matches(Transaction transaction) {
        for (TransactionFilter child : children()) {
            if (!child.matches(transaction)) {
                return false;
            }
        }
        return true;
    }
}