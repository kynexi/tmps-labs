package filter;

import model.Transaction;

public class KeywordFilter implements TransactionFilter {
    private final String keyword;
    public KeywordFilter(String keyword) { this.keyword = keyword.trim().toLowerCase(); }
    public boolean matches(Transaction t) {
        return t.getDescription().toLowerCase().contains(keyword);
    }
}