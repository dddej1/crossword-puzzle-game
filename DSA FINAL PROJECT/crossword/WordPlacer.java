// WordPlacer.java
package crossword;

import java.util.*;

public class WordPlacer {
    private final Random rnd = new Random();

    public void placeWords(CrosswordGrid grid, List<String> words) {
        for (String w : words) {
            boolean placed = false;
            for (int attempt = 0; attempt < 100 && !placed; attempt++) {
                int size = grid.getSize();
                int row = rnd.nextInt(size);
                int col = rnd.nextInt(size);
                boolean horizontal = rnd.nextBoolean();
                if (grid.canPlaceWord(w, row, col, horizontal)) {
                    grid.placeWord(w, row, col, horizontal);
                    placed = true;
                }
            }
        }
    }
}