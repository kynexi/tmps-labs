package filter;

import model.Transaction;

public class CategoryFilter implements TransactionFilter {
    private final String category;

    public CategoryFilter(String category) {
        this.category = category.trim();
    }

    @Override
    public boolean matches(Transaction transaction) {
        return transaction.getCategory().equalsIgnoreCase(category);
    }
}