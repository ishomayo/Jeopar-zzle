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
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.util.Duration;

public class LobbyScreen {
    private Pane root;
    private Stage stage;
    private MediaPlayer videoPlayer;
    private static AudioPlayer player = new AudioPlayer(); 
    private static boolean isSFXOn = true; 

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
            System.out.println("Looking for video at: " + Constants.LOBBY_BACKGROUND_VIDEO);
            java.net.URL resourceUrl = getClass().getResource(Constants.LOBBY_BACKGROUND_VIDEO);
            System.out.println("Resource URL: " + resourceUrl);

            if (resourceUrl == null) {
                throw new Exception("Video resource not found: " + Constants.LOBBY_BACKGROUND_VIDEO);
            }

            Media media = new Media(resourceUrl.toExternalForm());
            videoPlayer = new MediaPlayer(media);

            videoPlayer.setOnError(() -> {
                System.out.println("MediaPlayer error: " + videoPlayer.getError());
            });

            videoPlayer.setOnReady(() -> {
                System.out.println("Video loaded successfully!");
                System.out.println("Video duration: " + media.getDuration());
            });

            videoPlayer.setCycleCount(1); 

            MediaView mediaView = new MediaView(videoPlayer);
            mediaView.setFitWidth(Constants.SCREEN_WIDTH);
            mediaView.setFitHeight(Constants.SCREEN_HEIGHT);
            mediaView.setPreserveRatio(false);

            root.getChildren().add(mediaView);
            videoPlayer.play();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            root.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #667db6, #0082c8, #0082c8, #667db6);");
        }
    }

    private void createButtons() {
        Button playButton = createImageButton(
                loadImage(Constants.PLAY_BUTTON),
                loadImage(Constants.PLAY_BUTTON_HOVER),
                loadImage(Constants.PLAY_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 200,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> onPlayClicked());

        Button howToButton = createImageButton(
                loadImage(Constants.HOWTO_BUTTON),
                loadImage(Constants.HOWTO_BUTTON_HOVER),
                loadImage(Constants.HOWTO_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 285,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> HowToClicked());

        Button CreditsButton = createImageButton(
                loadImage(Constants.CREDITS_BUTTON),
                loadImage(Constants.CREDITS_BUTTON_HOVER),
                loadImage(Constants.CREDITS_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 370,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> onCreditsClicked());

        Button exitButton = createImageButton(
                loadImage(Constants.EXIT_BUTTON),
                loadImage(Constants.EXIT_BUTTON_HOVER),
                loadImage(Constants.EXIT_BUTTON_CLICK),
                (Constants.SCREEN_WIDTH - Constants.BUTTON_WIDTH) / 2, 455,
                Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
                Constants.BUTTON_CLICK_SOUND,
                event -> onExitClicked());

        root.getChildren().addAll(playButton, howToButton, CreditsButton, exitButton);
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
                    // player.playSoundEffect(soundPath); // oks n uncomment if oke na audio haha
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

        FadeTransition fadeOut = new FadeTransition(Duration.millis(Constants.FADE_DURATION_MS), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            cleanup();

            GameScreen gameScreen = new GameScreen(stage);

            gameScreen.setOnBackToLobby(() -> {
                LobbyScreen newLobby = new LobbyScreen(stage);
                Scene lobbyScene = new Scene(newLobby.getRoot(), Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
                stage.setScene(lobbyScene);
            });

            Scene gameScene = new Scene(gameScreen.getRoot(), Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
            stage.setScene(gameScene);
        });

        fadeOut.play();
    }

    private void HowToClicked() {
        System.out.println("How to button clicked!");

        FadeTransition fadeOut = new FadeTransition(Duration.millis(Constants.FADE_DURATION_MS), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            cleanup();

            HowToScreen howToScreen = new HowToScreen(stage);

            howToScreen.setOnBackToLobby(() -> {
                LobbyScreen newLobby = new LobbyScreen(stage);
                Scene lobbyScene = new Scene(newLobby.getRoot(), Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
                stage.setScene(lobbyScene);
            });

            Scene howToScene = new Scene(howToScreen.getRoot(), Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
            stage.setScene(howToScene);
        });

        fadeOut.play();
    }

    private void onCreditsClicked() {
        System.out.println("Credits button clicked!");

        FadeTransition fadeOut = new FadeTransition(Duration.millis(Constants.FADE_DURATION_MS), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            cleanup();

            CreditsScreen creditsScreen = new CreditsScreen(stage);

            creditsScreen.setOnBackToLobby(() -> {
                LobbyScreen newLobby = new LobbyScreen(stage);
                Scene lobbyScene = new Scene(newLobby.getRoot(), Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
                stage.setScene(lobbyScene);
            });

            Scene creditsScene = new Scene(creditsScreen.getRoot(), Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
            stage.setScene(creditsScene);
        });

        fadeOut.play();
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