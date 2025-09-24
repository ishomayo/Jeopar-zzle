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

public class LobbyScreen {
    private Pane root;
    private Stage stage;
    private MediaPlayer videoPlayer;
    private static AudioPlayer player = new AudioPlayer(); // Assuming you have this class
    private static boolean isSFXOn = true; // Game setting

    public LobbyScreen(Stage stage) {
        this.stage = stage;
        initializeUI();
    }

    private void initializeUI() {
        root = new Pane();
        root.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Video background
        setupVideoBackground();

        // Create buttons
        createButtons();
    }

    private void setupVideoBackground() {
        try {
            // Debug: Check if resource exists
            System.out.println("Looking for video at: " + Constants.LOBBY_BACKGROUND_VIDEO);
            java.net.URL resourceUrl = getClass().getResource(Constants.LOBBY_BACKGROUND_VIDEO);
            System.out.println("Resource URL: " + resourceUrl);

            if (resourceUrl == null) {
                throw new Exception("Video resource not found: " + Constants.LOBBY_BACKGROUND_VIDEO);
            }

            Media media = new Media(resourceUrl.toExternalForm());
            videoPlayer = new MediaPlayer(media);

            // Add error handling for MediaPlayer
            videoPlayer.setOnError(() -> {
                System.out.println("MediaPlayer error: " + videoPlayer.getError());
            });

            videoPlayer.setOnReady(() -> {
                System.out.println("Video loaded successfully!");
                System.out.println("Video duration: " + media.getDuration());
            });

            videoPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            MediaView mediaView = new MediaView(videoPlayer);
            mediaView.setFitWidth(Constants.SCREEN_WIDTH);
            mediaView.setFitHeight(Constants.SCREEN_HEIGHT);
            mediaView.setPreserveRatio(false);

            root.getChildren().add(mediaView);
            videoPlayer.play();

        } catch (Exception e) {
            // Fallback to static background
            System.out.println("Video background not found, using fallback");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            root.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #667db6, #0082c8, #0082c8, #667db6);");
        }
    }

    private void createButtons() {
        // Play Button - centered
        Button playButton = createImageButton(
                loadImage(Constants.PLAY_BUTTON),
                loadImage(Constants.PLAY_BUTTON_HOVER),
                loadImage(Constants.PLAY_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 200,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> onPlayClicked());

        // howToButton - centered
        Button howToButton = createImageButton(
                loadImage(Constants.HOWTO_BUTTON),
                loadImage(Constants.HOWTO_BUTTON_HOVER),
                loadImage(Constants.HOWTO_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 285,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> HowToClicked());

        // Credits Button - centered
        Button CreditsButton = createImageButton(
                loadImage(Constants.CREDITS_BUTTON),
                loadImage(Constants.CREDITS_BUTTON_HOVER),
                loadImage(Constants.CREDITS_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 370,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> onCreditsClicked());

        // Exit Button - centered
        Button exitButton = createImageButton(
                loadImage(Constants.EXIT_BUTTON),
                loadImage(Constants.EXIT_BUTTON_HOVER),
                loadImage(Constants.EXIT_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 455,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> onExitClicked());

        root.getChildren().addAll(playButton, howToButton,CreditsButton, exitButton);
    }

    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
            // Return a placeholder or null - the button creation method handles null images
            return null;
        }
    }

    // Overloaded method to create an image button with hover and click effects
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
            button.setText("Button");
            button.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
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
                    // player.playSoundEffect(soundPath); // Uncomment when AudioPlayer is
                    // implemented
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

    private void onPlayClicked() {
        System.out.println("Play button clicked!");
        // Stop video before transitioning
        if (videoPlayer != null) {
            videoPlayer.stop();
        }
        // TODO: Transition to game screen
    }

    private void HowToClicked() {
        System.out.println("How To button clicked!");
        // TODO: Show options screen
    }

    private void onCreditsClicked() {
        System.out.println("Credits button clicked!");
        // TODO: Show options screen
    }

    private void onExitClicked() {
        System.out.println("Exit button clicked!");
        if (videoPlayer != null) {
            videoPlayer.stop();
        }
        Platform.exit();
    }

    public Parent getRoot() {
        return root;
    }

    public void cleanup() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.dispose();
        }
    }
}