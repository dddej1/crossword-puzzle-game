package crossword;

import java.util.List;           
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.Point;

public class CrosswordGame extends JFrame {
    private final CrosswordGrid grid; //grid for the puzzle
    private final WordPlacer placer; //word placer for the grid
    private final GameLogic logic; //game logic to handle the guesses and scorees

    private final JEditorPane gridPane;
    private final DefaultListModel<String> clueModel; //manage the clue words
    private final JTextField guessField; //input guesses
    private final JLabel scoreLabel; //score display
    private final JLabel msgLabel; //display's message

    private final Stack<char[][]> undoStack = new Stack<>(); //stack for undo functionality
    private final Stack<char[][]> redoStack = new Stack<>();

    // this will initialize the game window and all the components
    public CrosswordGame(List<String> words) {
        // this will serve as the calculator for the grid size based on the word list
        int maxLen = words.stream().mapToInt(String::length).max().orElse(5);
        int size = Math.max(maxLen, words.size()) + 5;

        grid   = new CrosswordGrid(size);
        placer = new WordPlacer();
        placer.placeWords(grid, words);
        grid.fillEmptyWithRandomLetters();
        logic  = new GameLogic(words);

        // sets the game window ttiel and default close operation
        setTitle("Crossword Puzzle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gridPane = new JEditorPane();
        gridPane.setContentType("text/html");
        gridPane.setEditable(false);
        add(new JScrollPane(gridPane), BorderLayout.CENTER);

        // initialize the clue list and add words to it
        clueModel = new DefaultListModel<>();
        words.forEach(w -> clueModel.addElement(w.toUpperCase()));
        JList<String> clueList = new JList<>(clueModel);
        add(new JScrollPane(clueList), BorderLayout.EAST);

        // this is basically the controls wherein you have guess, submit, undo, redo, and scoring system
        JPanel controls = new JPanel(new FlowLayout());
        guessField = new JTextField(10);
        JButton submitBtn = new JButton("Submit");
        JButton undoBtn   = new JButton("Undo");
        JButton redoBtn   = new JButton("Redo");
        scoreLabel = new JLabel("Score: 0");
        msgLabel   = new JLabel(" ");

        // adds all the controls
        controls.add(new JLabel("Guess:"));
        controls.add(guessField);
        controls.add(submitBtn);
        controls.add(undoBtn);
        controls.add(redoBtn);
        controls.add(scoreLabel);
        controls.add(msgLabel);
        add(controls, BorderLayout.SOUTH);

        // action listeners for buttons
        submitBtn.addActionListener(e -> SwingUtilities.invokeLater(this::onSubmit));
        undoBtn  .addActionListener(e -> SwingUtilities.invokeLater(this::onUndo));
        redoBtn  .addActionListener(e -> SwingUtilities.invokeLater(this::onRedo));

        pack(); // adjust window size to fit the contents
        updateDisplay(); // updates the grid display
        setLocationRelativeTo(null); // centers the window on the screen
        setVisible(true); //makes the window visible
    }
 
    //method for handling the user guesses
    private void onSubmit() {
        String guess = guessField.getText().trim().toUpperCase();
        if (guess.isEmpty()) {
            msgLabel.setText("Please enter a word.");
            return;
        }

        undoStack.push(grid.getGridCopy());
        redoStack.clear();

        // error checking
        if (logic.guess(guess)) {
            clueModel.removeElement(guess); // remove the word from the clue list if guessed
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
        scoreLabel.setText("Score: " + logic.getScore()); //score updater
        updateDisplay();
        guessField.setText(""); // clears the guess input field
    }

    // undo method
    private void onUndo() {
        if (!logic.canUndo() || undoStack.isEmpty()) {
            msgLabel.setText("Nothing to undo.");
            return;
        }
        redoStack.push(grid.getGridCopy()); // save the current grid state for redo
        char[][] prev = undoStack.pop();
        grid.loadFromCopy(prev); // load the prev grid state
        logic.undo();
        msgLabel.setText("Undone.");
        scoreLabel.setText("Score: " + logic.getScore()); //update score
        updateDisplay();
    }

    // redo method
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

    // display the updated grid
    private void updateDisplay() {
        char[][] g = grid.getGridCopy();
        Set<Point> highlights = new HashSet<>();
        for (String w : logic.getFoundWords()) {
            highlights.addAll(grid.getWordPositions(w));
        }

        // this build the html string for displaying the grid
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
