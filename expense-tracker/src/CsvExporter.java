public class CsvExporter implements Exporter {
    public String format(List<Transaction> list) {
        StringBuilder sb = new StringBuilder("date,type,amount,category,description\n");
        for (Transaction t : list) {
            // quotes around description, because it may contain commas
            String desc = "\"" + t.getDescription().replace("\"", "\"\"") + "\"";
            sb.append(t.getDate()).append(',').append(t.getType()).append(',')
                    .append(t.getAmount()).append(',').append(t.getCategory()).append(',')
                    .append(desc).append('\n');
        }
        return sb.toString();
    }
    public String extension() { return "csv"; }
}