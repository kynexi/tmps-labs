package command;

import java.util.ArrayDeque;
import java.util.Deque;

// INVOKER: runs commands and keeps the history
public class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();          // if it throws (e.g. budget), it never enters the history
        undoStack.push(command);
        redoStack.clear();
    }

    public String undo() {
        if (undoStack.isEmpty()) throw new IllegalArgumentException("Nothing to undo.");
        Command c = undoStack.pop();
        c.undo();
        redoStack.push(c);
        return c.description();
    }

    public String redo() {
        if (redoStack.isEmpty()) throw new IllegalArgumentException("Nothing to redo.");
        Command c = redoStack.pop();
        c.redo();
        undoStack.push(c);
        return c.description();
    }
}