package importer.adapter;

import categorization.CategoryChain;
import categorization.CategoryRequest;
import importer.TransactionSource;
import model.Transaction;
import model.TransactionBuilder;
import model.TransactionType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ADAPTER: makes Apache Commons CSV (CSVParser / CSVRecord, code I did not write)
 * look like our TransactionSource.
 * Bank format: ';' separator, dates dd.MM.yyyy, amounts like "1 234,50",
 * separate Debit (money out) and Credit (money in) columns, no category column.
 * The category is chosen by the CHAIN OF RESPONSIBILITY (CategoryChain), if one is given.
 * Note: the Reader can be read only once.
 */
public class BankStatementCsvAdapter implements TransactionSource {
    static final String DATE = "Data";
    static final String DETAILS = "Detalii";
    static final String DEBIT = "Debit";
    static final String CREDIT = "Credit";

    private static final DateTimeFormatter BANK_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final CSVFormat BANK_FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader()               // read column names from the first line
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .setIgnoreEmptyLines(true)
            .build();

    private final Reader input;
    private final CategoryChain categories;   // may be null -> the builder's default category

    public BankStatementCsvAdapter(Reader input) {
        this(input, null);
    }

    public BankStatementCsvAdapter(Reader input, CategoryChain categories) {
        this.input = input;
        this.categories = categories;
    }

    public static BankStatementCsvAdapter fromFile(String path) {
        return fromFile(path, null);
    }

    public static BankStatementCsvAdapter fromFile(String path, CategoryChain categories) {
        try {
            return new BankStatementCsvAdapter(
                    Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8), categories);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Transaction> fetch() {
        List<Transaction> result = new ArrayList<>();
        try (CSVParser parser = BANK_FORMAT.parse(input)) {   // call into the adaptee
            for (CSVRecord row : parser) {
                result.add(toTransaction(row));                // translate its data into ours
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }

    private Transaction toTransaction(CSVRecord row) {
        String debit = row.get(DEBIT);
        String credit = row.get(CREDIT);
        boolean isExpense = !debit.isBlank();
        String amountText = isExpense ? debit : credit;
        if (amountText.isBlank()) {
            throw new IllegalArgumentException("Bank row " + row.getRecordNumber() + " has neither debit nor credit.");
        }

        TransactionType type = isExpense ? TransactionType.EXPENSE : TransactionType.INCOME;
        double amount = parseBankAmount(amountText);
        String description = row.get(DETAILS);
        String category = categories == null
                ? null
                : categories.resolve(new CategoryRequest(description, amount, type));

        // BUILDER: a null category keeps the default from AppSettings
        return new TransactionBuilder()
                .type(type)
                .amount(amount)
                .description(description)
                .category(category)
                .date(parseBankDate(row.get(DATE)))
                .build();
    }

    private static double parseBankAmount(String text) {
        String normalized = text.replace("\u00A0", "").replace(" ", "").replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad amount in bank file: " + text);
        }
    }

    private static LocalDate parseBankDate(String text) {
        try {
            return LocalDate.parse(text, BANK_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Bad date in bank file: " + text);
        }
    }
}