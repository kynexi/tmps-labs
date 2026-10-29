package export;

import model.Transaction;

import java.util.List;

public class TextExporter implements Exporter {
    public String format(List<Transaction> list) {
        StringBuilder sb = new StringBuilder("Finance Tracker report\n----------------------\n");
        for (Transaction t : list) {
            sb.append(String.format("%s | %-7s | %9.2f | %s | %s%n",
                    t.getDate(), t.getType(), t.getAmount(), t.getCategory(), t.getDescription()));
        }
        return sb.toString();
    }
    public String extension() { return "txt"; }
}