public class FileTransactionRepository implements TransactionRepository {
    private final List<Transaction> transactions = new ArrayList<>();
    private final Path file;

    public FileTransactionRepository(String fileName) {
        this.file = Path.of(fileName);
        load();
    }

    public void add(Transaction t) { transactions.add(t); save(); }      // save after every change
    public void remove(int index)  { transactions.remove(index); save(); }
    public List<Transaction> findAll() { return List.copyOf(transactions); }

    // one line per transaction, fields separated by TAB
    private void save() {
        List<String> lines = new ArrayList<>();
        for (Transaction t : transactions) {
            lines.add(t.getDate() + "\t" + t.getType() + "\t" + t.getAmount() + "\t"
                    + t.getCategory() + "\t" + t.getDescription().replace('\t', ' '));
        }
        try { Files.write(file, lines); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private void load() {
        if (!Files.exists(file)) return;                  // first run
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.split("\t", 5);
                if (p.length < 5) continue;               // skip broken lines
                transactions.add(new Transaction(Double.parseDouble(p[2]), p[4],
                        TransactionType.valueOf(p[1]), LocalDate.parse(p[0]), p[3]));
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}