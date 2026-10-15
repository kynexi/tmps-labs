import java.util.List;

// Interface for "where transactions are stored".

public interface TransactionRepository {
    void add(Transaction transaction);

    void remove(int index);

    List<Transaction> findAll();
}