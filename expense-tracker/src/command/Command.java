package command;

public interface Command {
    void execute();
    void undo();
    default void redo() { execute(); }
    String description();
}