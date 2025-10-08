import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen {
    private StackPane root;
    private Stage stage;
    private Runnable onSplashComplete;
    private MediaPlayer videoPlayer;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    public SplashScreen(Stage stage) {
        this.stage = stage;
        initializeUI();
    }

    private void initializeUI() {
        root = new StackPane();
        root.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");

        setupSplashVideo();

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

    private void setupSplashVideo() {
        try {
            System.out.println("Looking for splash video at: " + Constants.SPLASH_VIDEO);
            java.net.URL resourceUrl = getClass().getResource(Constants.SPLASH_VIDEO);
            System.out.println("Splash video URL: " + resourceUrl);

            if (resourceUrl == null) throw new Exception("Splash video not found: " + Constants.SPLASH_VIDEO);

            Media media = new Media(resourceUrl.toExternalForm());
            videoPlayer = new MediaPlayer(media);

            videoPlayer.setOnError(() -> {
                System.out.println("Splash video MediaPlayer error: " + videoPlayer.getError());
                retryVideoLoad(); // ✅ retry logic
            });

            videoPlayer.setOnReady(() -> {
                System.out.println("Splash video loaded successfully!");
                System.out.println("Video duration: " + media.getDuration());
            });

            videoPlayer.setCycleCount(1);

            MediaView mediaView = new MediaView(videoPlayer);
            mediaView.setFitWidth(Constants.SCREEN_WIDTH);
            mediaView.setFitHeight(Constants.SCREEN_HEIGHT);
            mediaView.setPreserveRatio(false);

            // ✅ Keep video at back, no duplicates
            if (!root.getChildren().contains(mediaView)) {
                root.getChildren().add(0, mediaView);
            } else {
                mediaView.toBack();
            }

            videoPlayer.play();

        } catch (Exception e) {
            System.out.println("Splash video not found, using static background");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");
        }
    }

    private void retryVideoLoad() {
        if (retryCount >= MAX_RETRIES) {
            System.out.println("Max retries reached. Using static background instead.");
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");
            return;
        }

        retryCount++;
        Platform.runLater(() -> {
            System.out.println("Retrying splash video load... Attempt " + retryCount);
            cleanup();
            try {
                Thread.sleep(500); // short delay to stabilize retry
            } catch (InterruptedException ignored) {}
            setupSplashVideo();
        });
    }

    private void transitionToLobby() {
        System.out.println("Transitioning to lobby...");

        if (videoPlayer != null) {
            videoPlayer.stop();
        }

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

    public void cleanup() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.dispose();
        }
    }
}
