package filter.composite;

import filter.TransactionFilter;

import java.util.ArrayList;
import java.util.List;

// COMPOSITE: a group is itself a TransactionFilter, so it can hold leaves or other groups
public abstract class FilterGroup implements TransactionFilter {
    private final List<TransactionFilter> children = new ArrayList<>();

    protected FilterGroup(TransactionFilter... filters) {
        for (TransactionFilter f : filters) {
            add(f);
        }
    }

    public FilterGroup add(TransactionFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null.");
        }
        children.add(filter);
        return this;
    }

    protected List<TransactionFilter> children() {
        return List.copyOf(children);
    }
}