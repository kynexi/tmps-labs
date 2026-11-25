package observer;

public interface TransactionPublisher {
    void subscribe(TransactionListener listener);
    void unsubscribe(TransactionListener listener);
}