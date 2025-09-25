import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Random;

public class GameScreen {
    private BorderPane root;
    private Stage stage;
    private GridPane gameBoard;
    private Label timeLabel;
    private Label scoreLabel;
    private TextField answerField;
    private ProgressBar questionTimer;
    private VBox questionModal;
    private Label questionText;
    private Label questionValueLabel;
    private Button[] answerButtons;
    private Timeline gameTimer;
    private Timeline questionTimerAnimation;
    
    private Image puzzleImage;
    private ImageView[][] puzzlePieces = new ImageView[5][6]; 
    private Button[][] questionButtons = new Button[5][6]; 
    private String currentImageName = ""; 
    private Random random = new Random();

    private int currentScore = 0;
    private int gameTimeLeft = 600;
    private double questionTimeLeft = 15.0;
    private boolean questionActive = false;
    private int selectedAnswerIndex = -1;
    private QuestionManager.Question currentQuestion;
    private int currentQuestionRow = -1;
    private int currentQuestionCol = -1;

    private QuestionManager questionManager;

    private String[] categories = { "KNOWLEDGE", "COMPREHEND", "APPLICATION", "ANALYSIS", "SYNTHESIS", "EVALUATION" };

    private int[][] pointValues = {
            { 100, 100, 100, 100, 100, 100 },
            { 400, 400, 400, 400, 400, 400 },
            { 800, 800, 800, 800, 800, 800 },
            { 1200, 1200, 1200, 1200, 1200, 1200 },
            { 1500, 1500, 1500, 1500, 1500, 1500 }
    };

    private boolean[][] questionAnswered = new boolean[5][6];
    private Runnable onBackToLobby;

    public GameScreen(Stage stage) {
        this.stage = stage;

        System.out.println("Initializing GameScreen with CSV QuestionManager...");
        this.questionManager = new QuestionManager();

        initializePhotoPuzzle();

        System.out.println("=== CSV INTEGRATION DEBUG ===");
        System.out.println("Total questions loaded: " + questionManager.getTotalQuestions());
        System.out.println("Available categories: " + questionManager.getCategories());

        QuestionManager.Question testQuestion = questionManager.getRandomQuestion();
        if (testQuestion != null) {
            System.out.println("Sample question loaded:");
            System.out.println("  Text: "
                    + testQuestion.getQuestionText().substring(0, Math.min(60, testQuestion.getQuestionText().length()))
                    + "...");
            System.out.println("  Correct Answer: " + testQuestion.getCorrectAnswer());
            System.out.println("  Point Value: " + testQuestion.getPointValue());
            System.out.println("  Category: " + testQuestion.getCategory());
        } else {
            System.out.println("WARNING: No questions loaded from CSV!");
        }
        System.out.println("===========================");

        initializeUI();
        startGameTimer();
    }

    private void initializePhotoPuzzle() {
        System.out.println("=== INITIALIZING PHOTO PUZZLE ===");
        
        String[] imageOptions = {"robot.png", "artificialintelligence.png"}; // pang ano lang check haha 2 images p lng
        String selectedImage = imageOptions[random.nextInt(imageOptions.length)];
        currentImageName = selectedImage.replace(".png", "").toUpperCase();
        
        System.out.println("Selected puzzle image: " + selectedImage);
        System.out.println("Display name: " + currentImageName);
        
        try {
            String imagePath = "/images/" + selectedImage; 
            puzzleImage = new Image(getClass().getResourceAsStream(imagePath));
            
            if (puzzleImage.isError()) {
                System.out.println("Error loading image, using fallback...");
                puzzleImage = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
            }
            
            System.out.println("Image loaded successfully");
            System.out.println("Image dimensions: " + puzzleImage.getWidth() + "x" + puzzleImage.getHeight());
            
            double pieceWidth = puzzleImage.getWidth() / 6.0;
            double pieceHeight = puzzleImage.getHeight() / 5.0;
            
            System.out.println("Piece dimensions: " + pieceWidth + "x" + pieceHeight);
            
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 6; col++) {
                    ImageView pieceView = new ImageView(puzzleImage);
                    
                    Rectangle2D viewport = new Rectangle2D(
                        col * pieceWidth, 
                        row * pieceHeight, 
                        pieceWidth, 
                        pieceHeight
                    );
                    pieceView.setViewport(viewport);
                    
                    pieceView.setFitWidth(120);
                    pieceView.setFitHeight(80);
                    pieceView.setPreserveRatio(false);
                    
                    pieceView.setVisible(false);
                    
                    puzzlePieces[row][col] = pieceView;
                    
                    System.out.println("Created piece [" + row + "][" + col + "] with viewport: " + viewport);
                }
            }
            
            System.out.println("Photo puzzle initialized successfully!");
            
        } catch (Exception e) {
            System.out.println("Error initializing photo puzzle: " + e.getMessage());
            e.printStackTrace();
            
            createFallbackPuzzlePieces();
        }
        
        System.out.println("=== PHOTO PUZZLE READY ===");
    }
    
    private void createFallbackPuzzlePieces() {
        System.out.println("Creating fallback puzzle pieces...");
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                Region coloredPiece = new Region();
                coloredPiece.setPrefSize(120, 80);
                
                String[] colors = {"#3498db", "#e74c3c", "#27ae60", "#f39c12", "#9b59b6", "#1abc9c"};
                String color = colors[(row + col) % colors.length];
                coloredPiece.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");
                
                puzzlePieces[row][col] = new ImageView();
                puzzlePieces[row][col].setVisible(false);
            }
        }
    }

    private void initializeUI() {
        root = new BorderPane();
        root.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);



        root.setStyle(
                "-fx-background-image: url('" + Constants.GAME_BACKGROUND_IMAGE + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;" +
                        "-fx-background-color: #2c3e50;" 
        );
        
        VBox leftSidebar = createLeftSidebar();
        root.setLeft(leftSidebar);

        gameBoard = createGameBoard();
        root.setCenter(gameBoard);

        VBox rightSidebar = createRightSidebar();
        root.setRight(rightSidebar);

        createQuestionModal();
    }

    private VBox createLeftSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(40);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(10));

        Button homeBtn = new Button("🏠");
        homeBtn.setStyle(
                "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-size: 20px; -fx-pref-width: 50px; -fx-pref-height: 50px;");
        homeBtn.setOnAction(e -> goBackToLobby());

        // placeholder pa lang di p nagana
        Button musicBtn = new Button("🎵");
        musicBtn.setStyle(
                "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-size: 20px; -fx-pref-width: 50px; -fx-pref-height: 50px;");

        Button soundBtn = new Button("🔊");
        soundBtn.setStyle(
                "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-size: 20px; -fx-pref-width: 50px; -fx-pref-height: 50px;");

        sidebar.getChildren().addAll(homeBtn, musicBtn, soundBtn);
        return sidebar;
    }

    private GridPane createGameBoard() {
        GridPane board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setHgap(8);
        board.setVgap(8);

        for (int col = 0; col < 6; col++) {
            Label categoryLabel = new Label(categories[col]);
            categoryLabel.setStyle(
                    "-fx-background-color: #fad02c; -fx-text-fill: black; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8px; -fx-background-radius: 5; -fx-alignment: center; -fx-wrap-text: true;");
            categoryLabel.setPrefSize(120, 50);
            categoryLabel.setMaxWidth(120);
            board.add(categoryLabel, col, 0);
        }

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                StackPane buttonStack = new StackPane();
                
                buttonStack.getChildren().add(puzzlePieces[row][col]);
                
                Button pointBtn = new Button(String.valueOf(pointValues[row][col]));
                pointBtn.setStyle(
                        "-fx-background-color: #ffffffff; -fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 5;");
                pointBtn.setPrefSize(120, 80);

                questionButtons[row][col] = pointBtn;

                final int currentRow = row;
                final int currentCol = col;

                pointBtn.setOnAction(e -> {
                    if (!questionAnswered[currentRow][currentCol] && !questionActive) {
                        openQuestionFromCSV(currentRow, currentCol, pointValues[currentRow][currentCol]);
                    }
                });

                buttonStack.getChildren().add(pointBtn);
                
                board.add(buttonStack, col, row + 1);
            }
        }

        return board;
    }

    private VBox createRightSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(150);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(10));

        timeLabel = new Label("TIME: 1:54");
        timeLabel.setStyle(
                "-fx-background-color: #ffffffff; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5; -fx-alignment: center;");
        timeLabel.setPrefWidth(180);

        scoreLabel = new Label("SCORE: 0");
        scoreLabel.setStyle(
                "-fx-background-color: #ffffffff; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5; -fx-alignment: center;");
        scoreLabel.setPrefWidth(180);

        answerField = new TextField();
        answerField.setPromptText("Guess the image...");
        answerField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        answerField.setPrefWidth(180);
        
        answerField.setOnAction(e -> checkImageGuess());

        Button guessBtn = new Button("GUESS THE IMAGE");
        guessBtn.setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 15px; -fx-background-radius: 5;");
        guessBtn.setPrefWidth(180);
        guessBtn.setPrefHeight(60);
        guessBtn.setOnAction(e -> checkImageGuess());

        sidebar.getChildren().addAll(timeLabel, scoreLabel, answerField, guessBtn);
        return sidebar;
    }
    
    private void checkImageGuess() {
        String guess = answerField.getText().trim().toLowerCase();
        String correctAnswer = currentImageName.toLowerCase();
        
        System.out.println("Image guess: '" + guess + "' vs correct: '" + correctAnswer + "'");
        
        if (guess.equals(correctAnswer) || 
            (correctAnswer.equals("artificialintelligence") && (guess.equals("artificial intelligence") || guess.equals("ai"))) ||
            (correctAnswer.equals("robot") && guess.equals("robot"))) {
            
            System.out.println("IMAGE GUESS CORRECT!");
            
            int bonusPoints = 2000;
            currentScore += bonusPoints;
            scoreLabel.setText("SCORE: " + currentScore);
            
            revealAllPuzzlePieces();
            
            endGame();
            
        } else {
            System.out.println("Image guess incorrect.");
            showImageGuessFailed();
        }
        
        answerField.clear();
    }
    
    private void revealAllPuzzlePieces() {
        System.out.println("Revealing all puzzle pieces!");
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                if (!questionAnswered[row][col]) {
                    questionAnswered[row][col] = true;
                    

                    questionButtons[row][col].setVisible(false);
                    puzzlePieces[row][col].setVisible(true);
                }
            }
        }
    }
    
    private void showImageGuessSuccess(int bonusPoints) {
        Label feedback = new Label("IMAGE CORRECT!\n+" + bonusPoints + " BONUS!");
        feedback.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-text-alignment: center;");

        StackPane feedbackPane = new StackPane(feedback);
        feedbackPane.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 20;");
        feedbackPane.setPrefSize(200, 100);

        ((StackPane) root.getCenter()).getChildren().add(feedbackPane);

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            ((StackPane) root.getCenter()).getChildren().remove(feedbackPane);
        }));
        delay.play();
    }
    
    private void showImageGuessFailed() {
        Label feedback = new Label("TRY AGAIN!");
        feedback.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        StackPane feedbackPane = new StackPane(feedback);
        feedbackPane.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15;");
        feedbackPane.setPrefSize(120, 60);

        ((StackPane) root.getCenter()).getChildren().add(feedbackPane);

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            ((StackPane) root.getCenter()).getChildren().remove(feedbackPane);
        }));
        delay.play();
    }

    private void createQuestionModal() {
        questionModal = new VBox(20);
        questionModal.setAlignment(Pos.CENTER);
        questionModal.setPadding(new Insets(30));
        questionModal.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        questionModal.setPrefSize(750, 500);
        questionModal.setVisible(false);

        questionValueLabel = new Label("100");
        questionValueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        questionText = new Label("Loading question from CSV...");
        questionText.setStyle(
                "-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-text-alignment: center; -fx-wrap-text: true;");
        questionText.setMaxWidth(690);
        questionText.setPrefHeight(100);

        GridPane answerGrid = new GridPane();
        answerGrid.setAlignment(Pos.CENTER);
        answerGrid.setHgap(15);
        answerGrid.setVgap(15);

        answerButtons = new Button[4];

        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new Button("Answer " + (i + 1));
            answerButtons[i].setStyle(
                    "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 15px; -fx-background-radius: 5; -fx-alignment: center-left; -fx-wrap-text: true;");
            answerButtons[i].setPrefSize(330, 80);
            answerButtons[i].setMaxWidth(330);

            final int answerIndex = i;
            answerButtons[i].setOnAction(e -> selectAnswer(answerIndex));

            answerGrid.add(answerButtons[i], i % 2, i / 2);
        }

        Button enterBtn = new Button("ENTER");
        enterBtn.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15px 30px; -fx-background-radius: 5;");
        enterBtn.setOnAction(e -> submitCSVAnswer());

        questionTimer = new ProgressBar(1.0);
        questionTimer.setPrefWidth(690);
        questionTimer.setPrefHeight(12);
        questionTimer.setStyle("-fx-accent: #e74c3c;");

        questionModal.getChildren().addAll(questionValueLabel, questionText, answerGrid, enterBtn, questionTimer);

        StackPane overlay = new StackPane();
        overlay.getChildren().addAll(root, questionModal);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        root = new BorderPane();
        root.setCenter(overlay);
    }

    private void openQuestionFromCSV(int row, int col, int points) {
        System.out.println("=== Opening CSV Question ===");
        System.out.println("Row: " + row + ", Col: " + col + ", Points: " + points);

        questionActive = true;
        questionTimeLeft = 15.0;
        selectedAnswerIndex = -1;
        currentQuestionRow = row;
        currentQuestionCol = col;

        String category = categories[col];
        System.out.println("Category: " + category);

        currentQuestion = questionManager.getQuestionForCategoryAndPoints(category, points);

        if (currentQuestion == null) {
            System.out.println("ERROR: No question found for category " + category + " and points " + points);
            currentQuestion = questionManager.getRandomQuestion();
        }

        if (currentQuestion != null) {
            System.out.println("Loaded CSV Question:");
            System.out.println("  Text: " + currentQuestion.getQuestionText());
            System.out.println("  Correct Answer: " + currentQuestion.getCorrectAnswer());
            System.out.println("  Correct Index: " + currentQuestion.getCorrectAnswerIndex());

            questionValueLabel.setText(String.valueOf(points));
            questionText.setText(currentQuestion.getQuestionText());

            String[] csvAnswers = currentQuestion.getAnswers();
            for (int i = 0; i < 4; i++) {
                if (i < csvAnswers.length && csvAnswers[i] != null && !csvAnswers[i].trim().isEmpty()) {
                    String buttonText = (char) ('A' + i) + ". " + csvAnswers[i];
                    answerButtons[i].setText(buttonText);
                    answerButtons[i].setVisible(true);
                    System.out.println("  Answer " + (char) ('A' + i) + ": " + csvAnswers[i]);
                } else {
                    answerButtons[i].setVisible(false);
                }

                answerButtons[i].setStyle(
                        "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 15px; -fx-background-radius: 5; -fx-alignment: center-left; -fx-wrap-text: true;");
            }

        } else {
            System.out.println("ERROR: No questions available at all!");
            return;
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), questionModal);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        questionModal.setVisible(true);
        fadeIn.play();

        startQuestionTimer();

        System.out.println("=== Question Modal Opened ===");
    }

    private void selectAnswer(int answerIndex) {
        selectedAnswerIndex = answerIndex;
        System.out.println("Selected answer index: " + answerIndex);

        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i].isVisible()) {
                answerButtons[i].setStyle(
                        "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 15px; -fx-background-radius: 5; -fx-alignment: center-left; -fx-wrap-text: true;");
            }
        }

        if (answerButtons[answerIndex].isVisible()) {
            answerButtons[answerIndex].setStyle(
                    "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 15px; -fx-background-radius: 5; -fx-alignment: center-left; -fx-wrap-text: true;");
        }
    }

    private void submitCSVAnswer() {
        if (selectedAnswerIndex == -1 || currentQuestion == null) {
            System.out.println("No answer selected or no current question");
            return;
        }

        System.out.println("=== Validating CSV Answer ===");
        System.out.println("Selected index: " + selectedAnswerIndex);
        System.out.println("Correct index: " + currentQuestion.getCorrectAnswerIndex());
        System.out.println("Selected answer: " + currentQuestion.getAnswers()[selectedAnswerIndex]);
        System.out.println("Correct answer: " + currentQuestion.getCorrectAnswer());

        boolean isCorrect = (selectedAnswerIndex == currentQuestion.getCorrectAnswerIndex());

        System.out.println("Answer is: " + (isCorrect ? "CORRECT!" : "WRONG!"));
        System.out.println("===========================");

        closeQuestionModal(isCorrect);
    }

    private void closeQuestionModal(boolean correct) {
        if (questionTimerAnimation != null) {
            questionTimerAnimation.stop();
        }

        questionActive = false;

        int points = Integer.parseInt(questionValueLabel.getText());

        if (correct) {
            currentScore += points;
            scoreLabel.setText("SCORE: " + currentScore);
            
            revealPuzzlePiece(currentQuestionRow, currentQuestionCol);
            
            showFeedback(true, points);
            System.out.println("CORRECT! Added " + points + " points. New score: " + currentScore);
        } else {
            currentScore = Math.max(0, currentScore - points);
            scoreLabel.setText("SCORE: " + currentScore);
            
            grayOutTile(currentQuestionRow, currentQuestionCol);
            
            showFeedback(false, points);
            System.out.println("WRONG! Subtracted " + points + " points. New score: " + currentScore);
        }

        questionAnswered[currentQuestionRow][currentQuestionCol] = true;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), questionModal);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> questionModal.setVisible(false));
        fadeOut.play();
    }
    
    private void revealPuzzlePiece(int row, int col) {
        System.out.println("Revealing puzzle piece at [" + row + "][" + col + "]");
        
        questionButtons[row][col].setVisible(false);
        
        puzzlePieces[row][col].setVisible(true);
        
        FadeTransition reveal = new FadeTransition(Duration.millis(500), puzzlePieces[row][col]);
        reveal.setFromValue(0.0);
        reveal.setToValue(1.0);
        reveal.play();
    }
    
    private void grayOutTile(int row, int col) {
        System.out.println("Graying out tile at [" + row + "][" + col + "]");
        
        Button button = questionButtons[row][col];
        
        button.setStyle(
            "-fx-background-color: #bdc3c7; -fx-text-fill: #7f8c8d; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-opacity: 0.6;"
        );
        button.setDisable(true);
        
        FadeTransition grayOut = new FadeTransition(Duration.millis(300), button);
        grayOut.setFromValue(1.0);
        grayOut.setToValue(0.6);
        grayOut.play();
    }

    private void showFeedback(boolean correct, int points) {
        Label feedback = new Label(correct ? "+" + points : "-" + points);
        feedback.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: " +
                (correct ? "#27ae60" : "#e74c3c") + ";");

        StackPane feedbackPane = new StackPane(feedback);
        feedbackPane.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 50;");
        feedbackPane.setPrefSize(120, 120);

        ((StackPane) root.getCenter()).getChildren().add(feedbackPane);

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            ((StackPane) root.getCenter()).getChildren().remove(feedbackPane);
        }));
        delay.play();
    }

    private void startGameTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            gameTimeLeft--;
            int minutes = gameTimeLeft / 60;
            int seconds = gameTimeLeft % 60;
            timeLabel.setText(String.format("TIME: %d:%02d", minutes, seconds));

            if (gameTimeLeft <= 0) {
                endGame();
            }
        }));
        gameTimer.setCycleCount(Animation.INDEFINITE);
        gameTimer.play();
    }

    private void startQuestionTimer() {
        questionTimer.setProgress(1.0);

        questionTimerAnimation = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            questionTimeLeft -= 0.1;
            double progress = questionTimeLeft / 15.0;
            questionTimer.setProgress(Math.max(0, progress));

            if (questionTimeLeft <= 0) {
                System.out.println("Time's up! Auto-submitting wrong answer.");
                closeQuestionModal(false); 
            }
        }));
        questionTimerAnimation.setCycleCount(150); 
        questionTimerAnimation.play();
    }

    private void endGame() {
        System.out.println("Game ended! Final score: " + currentScore);

        if (gameTimer != null) {
            gameTimer.stop();
        }

        if (questionTimerAnimation != null) {
            questionTimerAnimation.stop();
        }

        showGameOverScreen();
    }

    private void showGameOverScreen() {
        VBox gameOverModal = new VBox(20);
        gameOverModal.setAlignment(Pos.CENTER);
        gameOverModal.setPadding(new Insets(40));
        gameOverModal.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        gameOverModal.setPrefSize(500, 400);

        Label timeResult = new Label("TIME: 1:54");
        timeResult.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

        Label scoreResult = new Label("SCORE: " + currentScore);
        scoreResult.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

        Label imageLabel = new Label("IMAGE:\n" + currentImageName);
        imageLabel.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");

        Button newGameBtn = new Button("NEW GAME");
        newGameBtn.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15px 30px; -fx-background-radius: 5;");
        newGameBtn.setOnAction(e -> startNewGame());

        Button exitBtn = new Button("EXIT");
        exitBtn.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15px 30px; -fx-background-radius: 5;");
        exitBtn.setOnAction(e -> goBackToLobby());

        Label congratsLabel = new Label("CONGRATULATIONS!");
        congratsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        gameOverModal.getChildren().addAll(timeResult, scoreResult, imageLabel, newGameBtn, exitBtn, congratsLabel);

        ((StackPane) root.getCenter()).getChildren().add(gameOverModal);
    }

    private void startNewGame() {
        System.out.println("Starting new game...");

        currentScore = 0;
        gameTimeLeft = 114;
        questionAnswered = new boolean[5][6];
        scoreLabel.setText("SCORE: 0");
        timeLabel.setText("TIME: 1:54");
        selectedAnswerIndex = -1;
        currentQuestion = null;
        questionActive = false;
        currentQuestionRow = -1;
        currentQuestionCol = -1;

        StackPane center = (StackPane) root.getCenter();
        if (center.getChildren().size() > 1) {
            center.getChildren().remove(center.getChildren().size() - 1);
        }

        resetPuzzleAndButtons();
        
        initializePhotoPuzzle();
        
        gameBoard = createGameBoard();
        ((BorderPane) ((StackPane) root.getCenter()).getChildren().get(0)).setCenter(gameBoard);

        startGameTimer();
    }
    
    private void resetPuzzleAndButtons() {
        System.out.println("Resetting puzzle pieces and buttons...");
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                if (puzzlePieces[row][col] != null) {
                    puzzlePieces[row][col].setVisible(false);
                }
                
                if (questionButtons[row][col] != null) {
                    questionButtons[row][col].setVisible(true);
                    questionButtons[row][col].setDisable(false);
                    questionButtons[row][col].setStyle(
                        "-fx-background-color: #ffffffff; -fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 5;"
                    );
                    questionButtons[row][col].setOpacity(1.0);
                }
            }
        }
    }

    private void goBackToLobby() {
        System.out.println("Returning to lobby...");
        cleanup();

        if (onBackToLobby != null) {
            onBackToLobby.run();
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setOnBackToLobby(Runnable onBackToLobby) {
        this.onBackToLobby = onBackToLobby;
    }

    public void cleanup() {
        System.out.println("Cleaning up GameScreen resources...");

        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (questionTimerAnimation != null) {
            questionTimerAnimation.stop();
        }
    }
}