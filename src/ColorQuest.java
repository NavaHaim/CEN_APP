import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class ColorQuest extends Application {

    private static final int ROWS = 8; // Change this to increase attempts
    private static final int COLUMNS = 5; // Change this to increase the number of answers per guess
    private static final int FEEDBACK_CIRCLES = COLUMNS;
    private static final List<Color> AVAILABLE_COLORS = Arrays.asList(
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PURPLE, Color.CYAN
    );


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
        // Show the welcome screen and wait for the "OK" button click
        if (showWelcomeScreen(primaryStage)) {
            initializeGame();

            GridPane root = createGameBoard();
            Scene scene = new Scene(root, 500, 400);

            primaryStage.setTitle("ColorQuest");
            primaryStage.setScene(scene);
            primaryStage.show();
        } else {
            System.exit(0);
        }
    }

    private boolean showWelcomeScreen(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Welcome to ColorQuest");
        alert.setHeaderText("Welcome to ColorQuest!" +
                "\nHere is how to play the game:" +
                "\nThere is a secret code of colors you have to guess" +
                "\nSelect a color from the color select section, then click the circle you want to place it in" +
                "\nWhen all circles are filled with your guess, press the submit button" +
                "\nYou will find 4 circles to the right indicating the result of your guess" +
                "\nGreen = Correct Color + Correct Spot, Yellow = Correct Color + Incorrect Spot, Red = Incorrect color" +
                "\n" +
                "\nPress OK to start the game.");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okButton, exitButton);

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == okButton;
    }

    private void initializeGame() {
        secretCode = generateSecretCode();
        guesses = new ArrayList<>();
        feedbackBars = createFeedbackBars();
        currentRow = 0;
        selectedColorCircle = null;
    }

    private List<Color> generateSecretCode() {
        List<Color> code = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < COLUMNS; i++) {
            int index = random.nextInt(AVAILABLE_COLORS.size());
            code.add(AVAILABLE_COLORS.get(index));
        }

        return code;
    }

    private GridPane createGameBoard() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        VBox colorSelectionBar = createColorSelectionBar();
        gridPane.add(colorSelectionBar, 0, 0, 1, ROWS);

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

            Button submitButton = new Button("Submit");
            final int currentRow = row;
            submitButton.setOnAction(event -> checkGuess(currentRow));
            gridPane.add(submitButton, COLUMNS + 1, row);
        }

        addFeedbackBarsToGrid(gridPane); // Add feedback bars to the grid

        return gridPane;
    }

    private VBox createColorSelectionBar() {
        VBox colorSelectionBar = new VBox(10);
        colorSelectionBar.setAlignment(Pos.CENTER_LEFT);

        for (Color color : AVAILABLE_COLORS) {
            Circle colorCircle = createColorCircle(color);
            colorSelectionBar.getChildren().add(colorCircle);
        }

        // Add a border around the color selection bar
        colorSelectionBar.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 10px;");

        return colorSelectionBar;
    }

    private Circle createColorCircle(Color color) {
        Circle colorCircle = new Circle(15);
        colorCircle.setFill(color);

        colorCircle.setOnMouseClicked(event -> {
            setSelectedColor(colorCircle);
        });

        return colorCircle;
    }

    private void setSelectedColor(Circle colorCircle) {
        // Remove outline from the previously selected color
        if (selectedColorCircle != null) {
            selectedColorCircle.setStroke(Color.TRANSPARENT);
        }

        // Set outline for the newly selected color
        colorCircle.setStroke(Color.BLACK);
        selectedColorCircle = colorCircle;
    }

    private Color selectedColor() {
        return (Color) selectedColorCircle.getFill();
    }

    private List<HBox> createFeedbackBars() {
        List<HBox> feedbackBars = new ArrayList<>();

        for (int row = 0; row < ROWS; row++) {
            HBox feedbackBar = createFeedbackBar();
            feedbackBars.add(feedbackBar);
        }

        return feedbackBars;
    }

    private HBox createFeedbackBar() {
        HBox feedbackBar = new HBox(5);
        feedbackBar.setAlignment(Pos.CENTER);

        for (int i = 0; i < FEEDBACK_CIRCLES; i++) {
            Circle feedbackCircle = new Circle(8);
            feedbackCircle.setFill(Color.WHITE);
            feedbackBar.getChildren().add(feedbackCircle);
        }

        return feedbackBar;
    }

    private void addFeedbackBarsToGrid(GridPane gridPane) {
        for (int row = 0; row < ROWS; row++) {
            HBox feedbackBar = feedbackBars.get(row);
            gridPane.add(feedbackBar, COLUMNS + 2, row); // Add to the column after the guess circles
            gridPane.add(feedbackBar, COLUMNS + 2, row); // Add to the column after the guess circles
        }
    }

    private List<Circle> createGuess() {
        List<Circle> guess = new ArrayList<>();
        for (int i = 0; i < COLUMNS; i++) {
            Circle circle = new Circle(20);
            circle.setFill(Color.GRAY);
            guess.add(circle);
        }
        return guess;
    }

    private void checkGuess(int row) {
        boolean playerWin; // Set True for win set False for loss

        if (row != currentRow) {
            return; // Ignore submissions for rows other than the current one
        }

        List<Circle> currentGuess = guesses.get(row);
        List<Circle> feedbackCircles = feedbackBars.get(row).getChildren().stream()
                .map(child -> (Circle) child)
                .collect(Collectors.toList());

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

        if (isGuessCorrect(currentGuess)) {
            // Player won
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

    private void askToPlayAgain(boolean playerWin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        if (playerWin) {

            alert.setHeaderText("Congratulations! You've guessed the correct code.\nDo you want to play again?");
            //alert.setHeaderText("Congratulations! You've guessed the correct code.\nDo you want to play again?");
        } else {
            alert.setHeaderText("Game over! You've run out of attempts. " + "\n" + "" + "\nwas the correct code.\nDo you want to play again?");
            //alert.setHeaderText("Game over! You've run out of attempts.\nDo you want to play again?");
        }
        //alert.setHeaderText("Do you want to play again?");

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            restartGame();
        } else {
            System.exit(1);
        }
    }

    private void restartGame() {
        // Close the current window
        Stage currentStage = (Stage) feedbackBars.get(0).getScene().getWindow();
        currentStage.close();

        // Create and start a new window
        start(new Stage());
        // Add any additional setup logic if needed
    }

    private void updateFeedbackBar(int row) {
        HBox feedbackBar = feedbackBars.get(row);
        List<Circle> feedbackCircles = new ArrayList<>((List<Circle>) (List<?>) feedbackBar.getChildren());

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

    private boolean isGuessCorrect(List<Circle> guess) {
        List<Color> guessColors = new ArrayList<>();
        for (Circle circle : guess) {
            guessColors.add((Color) circle.getFill());
        }
        return guessColors.equals(secretCode);
    }


}
