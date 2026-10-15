public interface TransactionFilter {
    boolean matches(Transaction transaction);
}