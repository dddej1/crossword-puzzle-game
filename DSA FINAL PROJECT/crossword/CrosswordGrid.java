// CrosswordGrid.java
package crossword;

import java.util.*;
import java.awt.Point;

public class CrosswordGrid {
    private static final char EMPTY = '.';
    private char[][] grid;
    private int size;
    private Map<String, List<Point>> wordPositions;

    public CrosswordGrid(int size) {
        this.size = size;
        grid = new char[size][size];
        wordPositions = new HashMap<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = EMPTY;
            }
        }
    }

    public int getSize() {
        return size;
    }

    public boolean canPlaceWord(String word, int row, int col, boolean horizontal) {
        if (horizontal && col + word.length() > size) return false;
        if (!horizontal && row + word.length() > size) return false;
        for (int i = 0; i < word.length(); i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            char cur = grid[r][c];
            if (cur != EMPTY && cur != word.charAt(i)) return false;
        }
        return true;
    }

    public void placeWord(String word, int row, int col, boolean horizontal) {
        List<Point> positions = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            grid[r][c] = word.charAt(i);
            positions.add(new Point(r, c));
        }
        wordPositions.put(word.toUpperCase(), positions);
    }

    /** Fill remaining empty cells with random letters */
    public void fillEmptyWithRandomLetters() {
        Random rnd = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == EMPTY) {
                    grid[i][j] = (char) ('A' + rnd.nextInt(26));
                }
            }
        }
    }

    /** Get a deep copy of the grid for undo/redo */
    public char[][] getGridCopy() {
        char[][] copy = new char[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    /** Load grid from a copy (undo/redo) */
    public void loadFromCopy(char[][] copy) {
        int newSize = copy.length;
        this.size = newSize;
        grid = new char[newSize][newSize];
        for (int i = 0; i < newSize; i++) {
            System.arraycopy(copy[i], 0, grid[i], 0, newSize);
        }
    }

    /** Get stored positions of a placed word */
    public List<Point> getWordPositions(String word) {
        return wordPositions.getOrDefault(word.toUpperCase(), Collections.emptyList());
    }
}