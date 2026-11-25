package command;

import facade.FinanceFacade;
import model.Transaction;

public class AddTransactionCommand implements Command {
    private final FinanceFacade receiver;
    private final Transaction transaction;

    public AddTransactionCommand(FinanceFacade receiver, Transaction transaction) {
        this.receiver = receiver;
        this.transaction = transaction;
    }

    @Override public void execute() { receiver.add(transaction); }
    @Override public void undo()    { receiver.remove(transaction); }
    @Override public String description() { return "add '" + transaction.getDescription() + "'"; }
}