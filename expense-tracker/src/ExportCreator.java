public abstract class ExportCreator {

    // the factory method: subclasses decide what to create
    protected abstract Exporter createExporter();

    public String exportToFile(List<Transaction> list, String baseName) {
        Exporter exporter = createExporter();
        String fileName = baseName + "." + exporter.extension();
        try {
            Files.writeString(Path.of(fileName), exporter.format(list));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return fileName;
    }
}