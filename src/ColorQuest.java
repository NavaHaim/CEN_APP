// Import necessary JavaFX and Java libraries
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class ColorQuest_Commented extends Application {

    // Constants for the game setup
    private static final int ROWS = 8; // Number of attempts
    private static final int COLUMNS = 5; // Number of answers per guess
    private static final int FEEDBACK_CIRCLES = COLUMNS; // Feedback circles per row
    private static final List<Color> AVAILABLE_COLORS = Arrays.asList(
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PURPLE, Color.CYAN
    );

    // Variables to track the game state
    private List<Color> secretCode;
    private List<List<Circle>> guesses;
    private List<HBox> feedbackBars;
    private int currentRow;
    private Circle selectedColorCircle;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Show the welcome screen and initialize the game if the user clicks "OK"
        if (showWelcomeScreen(primaryStage)) {
            initializeGame();

            // Create the game board and display it
            GridPane root = createGameBoard();
            Scene scene = new Scene(root, 500, 400);

            primaryStage.setTitle("ColorQuest");
            primaryStage.setScene(scene);
            primaryStage.show();
        } else {
            // If the user clicks "Exit" on the welcome screen, exit the application
            System.exit(0);
        }
    }

    // Method to display the welcome screen and return true if the user clicks "OK"
    private boolean showWelcomeScreen(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Welcome to ColorQuest");
        alert.setHeaderText("Welcome to ColorQuest!" +
                "\nHere is how to play the game:" +
                "\nThere is a secret code of colors you have to guess" +
                "\nSelect a color from the color select section, then click the circle you want to place it in" +
                "\nWhen all circles are filled with your guess, press the submit button" +
                "\nYou will find " + COLUMNS + " circles to the right indicating the result of your guess" +
                "\nGreen = Correct Color + Correct Spot, Yellow = Correct Color + Incorrect Spot, Red = Incorrect color" +
                "\n" +
                "\nPress OK to start the game.");

        // Set up "OK" and "Exit" buttons
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okButton, exitButton);

        // Show the alert and return true if "OK" is clicked, false otherwise
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == okButton;
    }

    // Method to initialize the game state
    private void initializeGame() {
        secretCode = generateSecretCode();
        guesses = new ArrayList<>();
        feedbackBars = createFeedbackBars();
        currentRow = 0;
        selectedColorCircle = null;
    }

    // Method to generate a random secret code
    private List<Color> generateSecretCode() {
        List<Color> code = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < COLUMNS; i++) {
            int index = random.nextInt(AVAILABLE_COLORS.size());
            code.add(AVAILABLE_COLORS.get(index));
        }

        return code;
    }

    // Method to create the main game board layout
    private GridPane createGameBoard() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Create the color selection bar on the left
        VBox colorSelectionBar = createColorSelectionBar();
        gridPane.add(colorSelectionBar, 0, 0, 1, ROWS);

        // Create the rows of guess circles and submit buttons
        for (int row = 0; row < ROWS; row++) {
            List<Circle> guess = createGuess();
            guesses.add(guess);

            for (int col = 0; col < COLUMNS; col++) {
                Circle spot = guess.get(col);
                gridPane.add(spot, col + 1, row);

                // Add event handler to change the color of the spot when clicked
                final int clickedRow = row;
                spot.setOnMouseClicked(event -> {
                    if (clickedRow == currentRow) {
                        spot.setFill(selectedColor());
                    }
                });
            }

            // Create and set up the submit button for the current row
            Button submitButton = new Button("Submit");
            final int currentRow = row;
            submitButton.setOnAction(event -> checkGuess(currentRow));
            gridPane.add(submitButton, COLUMNS + 1, row);
        }

        // Add feedback bars to the grid
        addFeedbackBarsToGrid(gridPane);

        return gridPane;
    }

    // Method to create the color selection bar on the left
    private VBox createColorSelectionBar() {
        VBox colorSelectionBar = new VBox(10);
        colorSelectionBar.setAlignment(Pos.CENTER_LEFT);

        // Add color circles to the color selection bar
        for (Color color : AVAILABLE_COLORS) {
            Circle colorCircle = createColorCircle(color);
            colorSelectionBar.getChildren().add(colorCircle);
        }

        // Add a border around the color selection bar
        colorSelectionBar.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 10px;");

        return colorSelectionBar;
    }

    // Method to create a color circle for the color selection bar
    private Circle createColorCircle(Color color) {
        Circle colorCircle = new Circle(15);
        colorCircle.setFill(color);

        // Add event handler to set the selected color when clicked
        colorCircle.setOnMouseClicked(event -> {
            setSelectedColor(colorCircle);
        });

        return colorCircle;
    }

    // Method to set the selected color and update the UI
    private void setSelectedColor(Circle colorCircle) {
        // Remove outline from the previously selected color
        if (selectedColorCircle != null) {
            selectedColorCircle.setStroke(Color.TRANSPARENT);
        }

        // Set outline for the newly selected color
        colorCircle.setStroke(Color.BLACK);
        selectedColorCircle = colorCircle;
    }

    // Method to get the selected color from the color selection bar
    private Color selectedColor() {
        return (Color) selectedColorCircle.getFill();
    }

    // Method to create the feedback bars for each row
    private List<HBox> createFeedbackBars() {
        List<HBox> feedbackBars = new ArrayList<>();

        for (int row = 0; row < ROWS; row++) {
            HBox feedbackBar = createFeedbackBar();
            feedbackBars.add(feedbackBar);
        }

        return feedbackBars;
    }

    // Method to create a single feedback bar with feedback circles
    private HBox createFeedbackBar() {
        HBox feedbackBar = new HBox(5);
        feedbackBar.setAlignment(Pos.CENTER);

        // Add feedback circles to the feedback bar
        for (int i = 0; i < FEEDBACK_CIRCLES; i++) {
            Circle feedbackCircle = new Circle(8);
            feedbackCircle.setFill(Color.WHITE);
            feedbackBar.getChildren().add(feedbackCircle);
        }

        return feedbackBar;
    }

    // Method to add feedback bars to the grid
    private void addFeedbackBarsToGrid(GridPane gridPane) {
        for (int row = 0; row < ROWS; row++) {
            HBox feedbackBar = feedbackBars.get(row);
            gridPane.add(feedbackBar, COLUMNS + 2, row); // Add to the column after the guess circles
        }
    }

    // Method to create a row of guess circles
    private List<Circle> createGuess() {
        List<Circle> guess = new ArrayList<>();
        for (int i = 0; i < COLUMNS; i++) {
            Circle circle = new Circle(20);
            circle.setFill(Color.GRAY);
            guess.add(circle);
        }
        return guess;
    }

    // Method to check the guess against the secret code
    private void checkGuess(int row) {
        boolean playerWin;

        // Ignore submissions for rows other than the current one
        if (row != currentRow) {
            return;
        }

        List<Circle> currentGuess = guesses.get(row);
        List<Circle> feedbackCircles = feedbackBars.get(row).getChildren().stream()
                .map(child -> (Circle) child)
                .collect(Collectors.toList());

        // Compare each guess color with the corresponding secret code color
        for (int i = 0; i < COLUMNS; i++) {
            Color guessColor = (Color) currentGuess.get(i).getFill();
            Color secretColor = secretCode.get(i);

            if (guessColor.equals(secretColor)) {
                // Correct color and position
                feedbackCircles.get(i).setFill(Color.GREEN);
            } else if (secretCode.contains(guessColor)) {
                // Correct color but wrong position
                feedbackCircles.get(i).setFill(Color.YELLOW);
            } else {
                // Incorrect color
                feedbackCircles.get(i).setFill(Color.RED);
            }
        }

        updateFeedbackBar(row);

        // Check if the guess is correct, player won
        if (isGuessCorrect(currentGuess)) {
            playerWin = true;
            System.out.println("Congratulations! You've guessed the correct code.");
            askToPlayAgain(playerWin);
        } else if (row == ROWS - 1) {
            // No more attempts left, player lost
            playerWin = false;
            System.out.println("Game over! You've run out of attempts. The correct code was: " + secretCode);
            askToPlayAgain(playerWin);
        } else {
            // Move to the next row
            currentRow++;
        }
    }

    // Method to ask the player if they want to play again
    private void askToPlayAgain(boolean playerWin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        if (playerWin) {
            alert.setHeaderText("Congratulations! You've guessed the correct code.\nDo you want to play again?");
        } else {
            alert.setHeaderText("Game over! You've run out of attempts.\nDo you want to play again?");
        }

        // Set up "Yes" and "No" buttons
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(yesButton, noButton);

        // Show the alert and handle the player's choice
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            // If the player chooses to play again, restart the game
            restartGame();
        } else {
            // If the player chooses not to play again, exit the application
            System.exit(1);
        }
    }

    // Method to restart the game by closing the current window and creating a new one
    private void restartGame() {
        // Close the current window
        Stage currentStage = (Stage) feedbackBars.get(0).getScene().getWindow();
        currentStage.close();

        // Create and start a new window
        start(new Stage());
        // Add any additional setup logic if needed
    }

    // Method to update the feedback bar based on the current guess
    private void updateFeedbackBar(int row) {
        HBox feedbackBar = feedbackBars.get(row);
        List<Circle> feedbackCircles = new ArrayList<>((List<Circle>) (List<?>) feedbackBar.getChildren());

        // Compare each guess color with the corresponding secret code color
        for (int i = 0; i < COLUMNS; i++) {
            Color guessColor = (Color) guesses.get(row).get(i).getFill();
            Color secretColor = secretCode.get(i);

            if (guessColor.equals(secretColor)) {
                // Correct color and position
                feedbackCircles.get(i).setFill(Color.GREEN);
            } else if (secretCode.contains(guessColor)) {
                // Correct color but wrong position
                feedbackCircles.get(i).setFill(Color.YELLOW);
            } else {
                // Incorrect color
                feedbackCircles.get(i).setFill(Color.RED);
            }
        }
    }

    // Method to check if the current guess is correct
    private boolean isGuessCorrect(List<Circle> guess) {
        List<Color> guessColors = new ArrayList<>();
        for (Circle circle : guess) {
            guessColors.add((Color) circle.getFill());
        }
        return guessColors.equals(secretCode);
    }
}
