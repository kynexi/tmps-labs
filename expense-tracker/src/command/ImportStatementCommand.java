package command;

import facade.FinanceFacade;
import facade.ImportResult;
import importer.TransactionSource;
import model.Transaction;

public class ImportStatementCommand implements Command {
    private final FinanceFacade receiver;
    private final TransactionSource source;
    private ImportResult result;

    public ImportStatementCommand(FinanceFacade receiver, TransactionSource source) {
        this.receiver = receiver;
        this.source = source;
    }

    @Override public void execute() { result = receiver.importFrom(source); }

    @Override
    public void undo() {
        for (Transaction t : result.added()) receiver.remove(t);
    }

    // the CSV can only be read once, so redo re-adds what was imported instead of reading again
    @Override
    public void redo() {
        for (Transaction t : result.added()) receiver.add(t);
    }

    public ImportResult result() { return result; }
    @Override public String description() { return "import of " + result.imported() + " transactions"; }
}