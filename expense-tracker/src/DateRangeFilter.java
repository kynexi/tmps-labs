import java.time.LocalDate;

/** Matches transactions between two dates, both ends included. */
public class DateRangeFilter implements TransactionFilter {
    private final LocalDate from;
    private final LocalDate to;

    public DateRangeFilter(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'From' date cannot be after 'To' date.");
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean matches(Transaction transaction) {
        LocalDate date = transaction.getDate();
        return !date.isBefore(from) && !date.isAfter(to);
    }
}