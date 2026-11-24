package importer;

import model.Transaction;

import java.util.List;

// TARGET interface: what our app wants from any outside source of transactions
public interface TransactionSource {
    List<Transaction> fetch();
}