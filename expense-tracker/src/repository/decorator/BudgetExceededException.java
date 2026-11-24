package repository.decorator;

// extends IllegalArgumentException so ConsoleMenu already shows it as a normal error
public class BudgetExceededException extends IllegalArgumentException {
    public BudgetExceededException(String message) {
        super(message);
    }
}