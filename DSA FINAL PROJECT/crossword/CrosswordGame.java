// CrosswordGame.java
package crossword;

import java.util.List;           
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.Point;

public class CrosswordGame extends JFrame {
    private final CrosswordGrid grid;
    private final WordPlacer placer;
    private final GameLogic logic;

    private final JEditorPane gridPane;
    private final DefaultListModel<String> clueModel;
    private final JTextField guessField;
    private final JLabel scoreLabel;
    private final JLabel msgLabel;

    private final Stack<char[][]> undoStack = new Stack<>();
    private final Stack<char[][]> redoStack = new Stack<>();

    public CrosswordGame(List<String> words) {
        int maxLen = words.stream().mapToInt(String::length).max().orElse(5);
        int size = Math.max(maxLen, words.size()) + 5;

        grid   = new CrosswordGrid(size);
        placer = new WordPlacer();
        placer.placeWords(grid, words);
        grid.fillEmptyWithRandomLetters();
        logic  = new GameLogic(words);

        setTitle("Crossword Puzzle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gridPane = new JEditorPane();
        gridPane.setContentType("text/html");
        gridPane.setEditable(false);
        add(new JScrollPane(gridPane), BorderLayout.CENTER);

        clueModel = new DefaultListModel<>();
        words.forEach(w -> clueModel.addElement(w.toUpperCase()));
        JList<String> clueList = new JList<>(clueModel);
        add(new JScrollPane(clueList), BorderLayout.EAST);

        JPanel controls = new JPanel(new FlowLayout());
        guessField = new JTextField(10);
        JButton submitBtn = new JButton("Submit");
        JButton undoBtn   = new JButton("Undo");
        JButton redoBtn   = new JButton("Redo");
        scoreLabel = new JLabel("Score: 0");
        msgLabel   = new JLabel(" ");

        controls.add(new JLabel("Guess:"));
        controls.add(guessField);
        controls.add(submitBtn);
        controls.add(undoBtn);
        controls.add(redoBtn);
        controls.add(scoreLabel);
        controls.add(msgLabel);
        add(controls, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> SwingUtilities.invokeLater(this::onSubmit));
        undoBtn  .addActionListener(e -> SwingUtilities.invokeLater(this::onUndo));
        redoBtn  .addActionListener(e -> SwingUtilities.invokeLater(this::onRedo));

        pack();
        updateDisplay();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void onSubmit() {
        String guess = guessField.getText().trim().toUpperCase();
        if (guess.isEmpty()) {
            msgLabel.setText("Please enter a word.");
            return;
        }

        undoStack.push(grid.getGridCopy());
        redoStack.clear();

        if (logic.guess(guess)) {
            clueModel.removeElement(guess);
            msgLabel.setText("Correct!");
            if(clueModel.isEmpty()){

                int finalScore = logic.getScore();
                String message = "Congratulations! You found all the words.\nYour final score is: " + finalScore;
                JOptionPane.showMessageDialog(this, message, "Game Over",  JOptionPane.INFORMATION_MESSAGE);

                System.exit(0);
            }
        } else {
            msgLabel.setText("Wrong or duplicate.");
        }
        scoreLabel.setText("Score: " + logic.getScore());
        updateDisplay();
        guessField.setText("");
    }

    private void onUndo() {
        if (!logic.canUndo() || undoStack.isEmpty()) {
            msgLabel.setText("Nothing to undo.");
            return;
        }
        redoStack.push(grid.getGridCopy());
        char[][] prev = undoStack.pop();
        grid.loadFromCopy(prev);
        logic.undo();
        msgLabel.setText("Undone.");
        scoreLabel.setText("Score: " + logic.getScore());
        updateDisplay();
    }

    private void onRedo() {
        if (!logic.canRedo() || redoStack.isEmpty()) {
            msgLabel.setText("Nothing to redo.");
            return;
        }
        undoStack.push(grid.getGridCopy());
        char[][] next = redoStack.pop();
        grid.loadFromCopy(next);
        logic.redo();
        msgLabel.setText("Redone.");
        scoreLabel.setText("Score: " + logic.getScore());
        updateDisplay();
    }

    private void updateDisplay() {
        char[][] g = grid.getGridCopy();
        Set<Point> highlights = new HashSet<>();
        for (String w : logic.getFoundWords()) {
            highlights.addAll(grid.getWordPositions(w));
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:monospace;'><pre>");
        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g[i].length; j++) {
                char c = g[i][j];
                if (highlights.contains(new Point(i, j))) {
                    html.append("<span style='color:blue;font-weight:bold;'>").append(c).append("</span>");
                } else {
                    html.append(c);
                }
                html.append(' ');
            }
            html.append("<br>");
        }
        html.append("</pre></body></html>");
        gridPane.setText(html.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String input = JOptionPane.showInputDialog(null,
                "Enter words (comma-separated):", "Crossword Setup", JOptionPane.QUESTION_MESSAGE);
            if (input == null || input.trim().isEmpty()) return;
            List<String> words = Arrays.asList(input.toUpperCase().split("\\s*,\\s*"));
            new CrosswordGame(words);
        });
    }
}
