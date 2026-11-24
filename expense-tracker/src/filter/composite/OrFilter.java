package filter.composite;

import filter.TransactionFilter;
import model.Transaction;

// matches if ANY child matches (an empty group matches nothing)
public class OrFilter extends FilterGroup {
    public OrFilter(TransactionFilter... filters) {
        super(filters);
    }

    @Override
    public boolean matches(Transaction transaction) {
        for (TransactionFilter child : children()) {
            if (child.matches(transaction)) {
                return true;
            }
        }
        return false;
    }
}