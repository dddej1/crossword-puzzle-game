package crossword;

import java.util.*;
import java.awt.Point;

public class CrosswordGrid {
    // represents an empty cell in the grid
    private static final char EMPTY = '.';
    // 2d array to store the grid and to store word positions
    private char[][] grid;
    private int size;
    private Map<String, List<Point>> wordPositions;

    // constructor for iitializing the grid with a given size
    public CrosswordGrid(int size) {
        this.size = size;
        grid = new char[size][size];
        wordPositions = new HashMap<>();
        // this initialize the grid with empty cells
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = EMPTY;
            }
        }
    }

    // retursns the size of the grid
    public int getSize() {
        return size;
    }

    //checks if the word can be placed at the given position.
    public boolean canPlaceWord(String word, int row, int col, boolean horizontal) {
        if (horizontal && col + word.length() > size) return false;
        if (!horizontal && row + word.length() > size) return false;
        //place cahracter of the word on the grid and storre its position
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
        //store the positions of the placed words
        wordPositions.put(word.toUpperCase(), positions);
    }

    // fills remaining empty cells with random letters 
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

    // et a deep copy of the grid for undo and redo 
    public char[][] getGridCopy() {
        char[][] copy = new char[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    // load grid from a copy undo and redo
    public void loadFromCopy(char[][] copy) {
        int newSize = copy.length;
        this.size = newSize;
        grid = new char[newSize][newSize];
        for (int i = 0; i < newSize; i++) {
            System.arraycopy(copy[i], 0, grid[i], 0, newSize);
        }
    }

    // get stored positions of a placed word 
    public List<Point> getWordPositions(String word) {
        return wordPositions.getOrDefault(word.toUpperCase(), Collections.emptyList());
    }
}