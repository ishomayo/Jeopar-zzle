package src.main;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen {
    private StackPane root;
    private Stage stage;
    private Runnable onSplashComplete;
    
    public SplashScreen(Stage stage) {
        this.stage = stage;
        initializeUI();
    }
    
    private void initializeUI() {
        root = new StackPane();
        root.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        
        // Background
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream(Constants.SPLASH_BACKGROUND));
            ImageView background = new ImageView(backgroundImage);
            background.setFitWidth(Constants.SCREEN_WIDTH);
            background.setFitHeight(Constants.SCREEN_HEIGHT);
            root.getChildren().add(background);
        } catch (Exception e) {
            // Fallback to colored background if image not found
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");
        }
        
        // Logo
        try {
            Image logoImage = new Image(getClass().getResourceAsStream(Constants.LOGO_IMAGE));
            ImageView logo = new ImageView(logoImage);
            logo.setFitWidth(300);
            logo.setFitHeight(150);
            logo.setPreserveRatio(true);
            root.getChildren().add(logo);
        } catch (Exception e) {
            // Fallback text logo
            Text logoText = new Text("GAME LOGO");
            logoText.setFont(Font.font("Arial", 48));
            logoText.setFill(Color.WHITE);
            root.getChildren().add(logoText);
        }
        
        // Loading text
        Text loadingText = new Text("Loading...");
        loadingText.setFont(Font.font("Arial", 24));
        loadingText.setFill(Color.WHITE);
        loadingText.setTranslateY(100);
        root.getChildren().add(loadingText);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(Constants.FADE_DURATION_MS), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        // Auto transition after splash duration
        PauseTransition pause = new PauseTransition(Duration.millis(Constants.SPLASH_DURATION_MS));
        pause.setOnFinished(event -> transitionToLobby());
        pause.play();
    }
    
    private void transitionToLobby() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(Constants.FADE_DURATION_MS), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            if (onSplashComplete != null) {
                onSplashComplete.run();
            }
        });
        fadeOut.play();
    }
    
    public Parent getRoot() {
        return root;
    }
    
    public void setOnSplashComplete(Runnable onSplashComplete) {
        this.onSplashComplete = onSplashComplete;
    }
}