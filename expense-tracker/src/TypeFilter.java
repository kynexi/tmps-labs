public class TypeFilter implements TransactionFilter {
    private final TransactionType type;

    public TypeFilter(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean matches(Transaction transaction) {
        return transaction.getType() == type;
    }
}