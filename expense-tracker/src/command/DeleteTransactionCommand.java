package command;

import facade.FinanceFacade;
import model.Transaction;

public class DeleteTransactionCommand implements Command {
    private final FinanceFacade receiver;
    private final int index;
    private Transaction removed;   // remembered so undo can bring it back

    public DeleteTransactionCommand(FinanceFacade receiver, int index) {
        this.receiver = receiver;
        this.index = index;
    }

    @Override
    public void execute() {
        removed = receiver.transactionAt(index);
        receiver.remove(removed);
    }

    // note: the transaction comes back at the end of the list, not at its old position
    @Override
    public void undo() {
        receiver.add(removed);
    }

    @Override
    public String description() {
        return "delete '" + removed.getDescription() + "'";
    }
}