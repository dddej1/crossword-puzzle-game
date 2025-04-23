// GameLogic.java
package crossword;

import java.util.*;

public class GameLogic {
    private final Set<String> targets;
    private final Set<String> found = new HashSet<>();
    private final Stack<String> undoStack = new Stack<>();
    private final Stack<String> redoStack = new Stack<>();
    private int score = 0;

    public GameLogic(List<String> words) {
        targets = new HashSet<>();
        for (String w : words) {
            targets.add(w.toUpperCase());
        }
    }

    /** Attempt a guess; returns true if newly correct */
    public boolean guess(String word) {
        word = word.toUpperCase();
        if (targets.contains(word) && found.add(word)) {
            score += 10;
            undoStack.push(word);
            redoStack.clear();
            return true;
        }
        return false;
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public String undo() {
        if (!canUndo()) return null;
        String w = undoStack.pop();
        found.remove(w);
        redoStack.push(w);
        score -= 10;
        return w;
    }

    public String redo() {
        if (!canRedo()) return null;
        String w = redoStack.pop();
        found.add(w);
        undoStack.push(w);
        score += 10;
        return w;
    }

    public Set<String> getFoundWords() {
        return Collections.unmodifiableSet(found);
    }

    public int getScore() {
        return score;
    }
}
    