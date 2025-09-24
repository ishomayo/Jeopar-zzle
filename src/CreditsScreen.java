import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CreditsScreen {
    private Pane root;
    private Stage stage;
    private MediaPlayer videoPlayer;
    private static AudioPlayer player = new AudioPlayer();
    private static boolean isSFXOn = true;
    private Runnable onBackToLobby;

    public CreditsScreen(Stage stage) {
        this.stage = stage;
        initializeUI();
    }

    private void initializeUI() {
        root = new Pane();
        root.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Video background
        setupVideoBackground();

        // Create back button
        createBackButton();

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(Constants.FADE_DURATION_MS), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void setupVideoBackground() {
        try {
            System.out.println("Looking for Credits video at: " + Constants.CREDITS_VIDEO);
            java.net.URL resourceUrl = getClass().getResource(Constants.CREDITS_VIDEO);
            System.out.println("Credits video URL: " + resourceUrl);

            if (resourceUrl == null) {
                throw new Exception("Credits video resource not found: " + Constants.CREDITS_VIDEO);
            }

            Media media = new Media(resourceUrl.toExternalForm());
            videoPlayer = new MediaPlayer(media);

            videoPlayer.setOnError(() -> {
                System.out.println("Credits MediaPlayer error: " + videoPlayer.getError());
            });

            videoPlayer.setOnReady(() -> {
                System.out.println("Credits video loaded successfully!");
                System.out.println("Video duration: " + media.getDuration());
            });

            // Credits might want to play once or loop depending on preference
            videoPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            MediaView mediaView = new MediaView(videoPlayer);
            mediaView.setFitWidth(Constants.SCREEN_WIDTH);
            mediaView.setFitHeight(Constants.SCREEN_HEIGHT);
            mediaView.setPreserveRatio(false);

            root.getChildren().add(mediaView);
            videoPlayer.play();

        } catch (Exception e) {
            System.out.println("Credits video not found, using fallback");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #8e44ad, #3498db);");
        }
    }

    private void createBackButton() {
        Button backButton = createImageButton(
                loadImage(Constants.BACK_BUTTON),
                loadImage(Constants.BACK_BUTTON_HOVER),
                loadImage(Constants.BACK_BUTTON_CLICK),
                20, 20, // Top-left position
                Constants.BACK_BUTTON_WIDTH, Constants.BACK_BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> onBackClicked()
        );

        root.getChildren().add(backButton);
    }

    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
            return null;
        }
    }

    private Button createImageButton(Image image, Image hoverImage, Image clickImage, double x, double y,
            double width, double height, String soundPath, EventHandler<ActionEvent> action) {
        Button button = new Button();

        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            button.setGraphic(imageView);
        } else {
            // Fallback button style if no image
            button.setText("← Back");
            button.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 5;");
        }

        button.setStyle(button.getStyle() + "-fx-background-color: transparent; -fx-border-color: transparent;");
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(width);
        button.setPrefHeight(height);

        if (hoverImage != null && clickImage != null) {
            button.setOnMouseEntered(event -> setButtonGraphic(button, hoverImage, width, height));
            button.setOnMouseExited(event -> setButtonGraphic(button, image, width, height));
            button.setOnMousePressed(event -> setButtonGraphic(button, clickImage, width, height));
            button.setOnMouseReleased(event -> setButtonGraphic(button, image, width, height));
        }

        if (action != null) {
            button.setOnAction(event -> {
                if (soundPath != null && !soundPath.isEmpty() && isSFXOn) {
                    // player.playSoundEffect(soundPath);
                }
                action.handle(event);
            });
        }

        return button;
    }

    private void setButtonGraphic(Button button, Image image, double width, double height) {
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            button.setGraphic(imageView);
        }
    }

    private void onBackClicked() {
        System.out.println("Back button clicked from Credits!");
        
        // Fade out transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(Constants.FADE_DURATION_MS), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            cleanup();
            if (onBackToLobby != null) {
                onBackToLobby.run();
            }
        });
        fadeOut.play();
    }

    public Parent getRoot() {
        return root;
    }

    public void setOnBackToLobby(Runnable onBackToLobby) {
        this.onBackToLobby = onBackToLobby;
    }

    public void cleanup() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.dispose();
        }
    }
}