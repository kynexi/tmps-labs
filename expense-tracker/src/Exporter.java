
public interface Exporter {
    String format(List<Transaction> transactions);
    String extension();
}
