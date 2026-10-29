package export;

import model.Transaction;

import java.util.List;

public interface Exporter {
    String format(List<Transaction> transactions);
    String extension();
}
