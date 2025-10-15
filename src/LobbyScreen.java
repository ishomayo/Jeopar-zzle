import java.io.File;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LobbyScreen {
    private StackPane mainContainer;
    private Pane root;
    private Stage stage;
    private MediaPlayer videoPlayer;
    private AudioManager audioManager = AudioManager.getInstance();
    
    private VBox settingsModal;
    private Pane settingsOverlay;
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    
    private static final int MAX_RETRIES = 3;
    private int retryCount = 0;
    private MediaView mediaView;

    public LobbyScreen(Stage stage) {
        this.stage = stage;
        initializeUI();
    }

    private void initializeUI() {
        root = new Pane();
        root.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Start lobby music
        audioManager.playBackgroundMusic(Constants.LOBBY_MUSIC);

        // Video background
        setupVideoBackground();

        // Create buttons
        createButtons();
        
        // Create settings button at top right
        createSettingsButton();
        
        // Wrap in StackPane for modal overlay
        mainContainer = new StackPane();
        mainContainer.getChildren().add(root);
        
        // Create settings modal (initially hidden)
        createSettingsModal();
    }

    private void createSettingsButton() {
        Button settingsButton = createImageButton(
                loadImage(Constants.SETTINGS_BUTTON),
                loadImage(Constants.SETTINGS_BUTTON_HOVER),
                loadImage(Constants.SETTINGS_BUTTON_CLICK),
                Constants.SCREEN_WIDTH - 70, // 20px from right edge
                20, // 20px from top
                50, 50,
                Constants.BUTTON_CLICK_SOUND,
                event -> openSettingsModal());

        root.getChildren().add(settingsButton);
    }
    
    private void createSettingsModal() {
        // Semi-transparent black overlay (same as question modal)
        settingsOverlay = new Pane();
        settingsOverlay.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        settingsOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); // 60% black like question modal
        settingsOverlay.setVisible(false);
        
        // Compact settings modal
        settingsModal = new VBox(20);
        settingsModal.setAlignment(Pos.CENTER);
        settingsModal.setPadding(new Insets(30));
        settingsModal.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        settingsModal.setPrefSize(400, 320); // Smaller, compact size
        settingsModal.setMaxSize(400, 320);
        settingsModal.setVisible(false);
        
        // Title
        Label titleLabel = new Label("SETTINGS");
        titleLabel.setStyle(
                "-fx-font-size: 24px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2c3e50;");
        
        // Music Volume Section
        VBox musicSection = createVolumeSection(
                "Music Volume", 
                audioManager.isMusicEnabled() ? audioManager.getMusicVolume() : 0.0,
                value -> {
                    audioManager.setMusicVolume(value);
                    if (value > 0 && !audioManager.isMusicEnabled()) {
                        audioManager.toggleMusic();
                    } else if (value == 0 && audioManager.isMusicEnabled()) {
                        audioManager.toggleMusic();
                    }
                });
        musicVolumeSlider = (Slider) ((VBox) musicSection.getChildren().get(1)).getChildren().get(0);
        
        // Sound Effects Volume Section
        VBox sfxSection = createVolumeSection(
                "Sound Effects", 
                audioManager.isSFXEnabled() ? audioManager.getSFXVolume() : 0.0,
                value -> {
                    audioManager.setSFXVolume(value);
                    if (value > 0 && !audioManager.isSFXEnabled()) {
                        audioManager.toggleSFX();
                    } else if (value == 0 && audioManager.isSFXEnabled()) {
                        audioManager.toggleSFX();
                    }
                });
        sfxVolumeSlider = (Slider) ((VBox) sfxSection.getChildren().get(1)).getChildren().get(0);
        
        // Close Button
        Button closeButton = new Button("CLOSE");
        closeButton.setStyle(
                "-fx-background-color: #27ae60; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 30px; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;");
        closeButton.setOnAction(e -> {
            audioManager.playSoundEffect(Constants.BUTTON_CLICK_SOUND);
            closeSettingsModal();
        });
        
        closeButton.setOnMouseEntered(e -> 
            closeButton.setStyle(
                "-fx-background-color: #229954; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 30px; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"));
        
        closeButton.setOnMouseExited(e -> 
            closeButton.setStyle(
                "-fx-background-color: #27ae60; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 30px; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"));
        
        settingsModal.getChildren().addAll(titleLabel, musicSection, sfxSection, closeButton);
        
        // Add to main container (overlay first, then modal on top)
        mainContainer.getChildren().addAll(settingsOverlay, settingsModal);
    }
    
    private VBox createVolumeSection(String label, double initialValue, VolumeChangeListener listener) {
        VBox section = new VBox(8);
        section.setAlignment(Pos.CENTER);
        
        Label sectionLabel = new Label(label);
        sectionLabel.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2c3e50;");
        
        VBox sliderBox = new VBox(5);
        sliderBox.setAlignment(Pos.CENTER);
        
        Slider volumeSlider = new Slider(0, 1, initialValue);
        volumeSlider.setPrefWidth(300);
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setShowTickMarks(false);
        volumeSlider.setStyle(
                "-fx-control-inner-background: #ecf0f1; " +
                "-fx-accent: #3498db;");
        
        Label volumeLabel = new Label(String.format("%.0f%%", initialValue * 100));
        volumeLabel.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-text-fill: #7f8c8d;");
        
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
            listener.onChange(newVal.doubleValue());
        });
        
        sliderBox.getChildren().addAll(volumeSlider, volumeLabel);
        section.getChildren().addAll(sectionLabel, sliderBox);
        
        return section;
    }
    
    @FunctionalInterface
    private interface VolumeChangeListener {
        void onChange(double value);
    }
    
    private void openSettingsModal() {
        // Update slider values to current actual volumes from AudioManager
        musicVolumeSlider.setValue(audioManager.isMusicEnabled() ? audioManager.getMusicVolume() : 0.0);
        sfxVolumeSlider.setValue(audioManager.isSFXEnabled() ? audioManager.getSFXVolume() : 0.0);
        
        // Show overlay with fade in
        FadeTransition overlayFadeIn = new FadeTransition(Duration.millis(300), settingsOverlay);
        overlayFadeIn.setFromValue(0.0);
        overlayFadeIn.setToValue(1.0);
        settingsOverlay.setVisible(true);
        overlayFadeIn.play();
        
        // Show modal with fade in
        FadeTransition modalFadeIn = new FadeTransition(Duration.millis(300), settingsModal);
        modalFadeIn.setFromValue(0.0);
        modalFadeIn.setToValue(1.0);
        settingsModal.setVisible(true);
        modalFadeIn.play();
    }
    
    private void closeSettingsModal() {
        // Fade out overlay
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), settingsOverlay);
        overlayFadeOut.setFromValue(1.0);
        overlayFadeOut.setToValue(0.0);
        overlayFadeOut.setOnFinished(e -> settingsOverlay.setVisible(false));
        overlayFadeOut.play();
        
        // Fade out modal
        FadeTransition modalFadeOut = new FadeTransition(Duration.millis(300), settingsModal);
        modalFadeOut.setFromValue(1.0);
        modalFadeOut.setToValue(0.0);
        modalFadeOut.setOnFinished(e -> settingsModal.setVisible(false));
        modalFadeOut.play();
    }

    private void setupVideoBackground() {
        try {
            System.out.println("Looking for video at: " + Constants.LOBBY_BACKGROUND_VIDEO);
            java.net.URL resourceUrl = getClass().getResource(Constants.LOBBY_BACKGROUND_VIDEO);
            System.out.println("Resource URL: " + resourceUrl);

            if (resourceUrl == null) {
                File file = new File(Constants.LOBBY_BACKGROUND_VIDEO);
                if (file.exists()) {
                    resourceUrl = file.toURI().toURL();
                    System.out.println("Loaded from file path instead.");
                } else {
                    throw new Exception("Video resource not found: " + Constants.LOBBY_BACKGROUND_VIDEO);
                }
            }

            Media media = new Media(resourceUrl.toExternalForm());
            videoPlayer = new MediaPlayer(media);

            videoPlayer.setOnError(() -> {
                System.out.println("MediaPlayer error: " + videoPlayer.getError());

                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    System.out.println("Retrying video load... Attempt " + retryCount + " of " + MAX_RETRIES);

                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            javafx.application.Platform.runLater(this::setupVideoBackground);
                        } catch (InterruptedException ignored) {
                        }
                    }).start();

                } else {
                    System.out.println("Max retries reached. Falling back to background color.");
                    root.setStyle(
                            "-fx-background-color: linear-gradient(to bottom right, #667db6, #0082c8, #0082c8, #667db6);");
                }
            });

            videoPlayer.setOnReady(() -> {
                retryCount = 0;
                System.out.println("Video loaded successfully!");
                System.out.println("Video duration: " + media.getDuration());
            });

            videoPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            if (mediaView == null) {
                mediaView = new MediaView(videoPlayer);
                mediaView.setFitWidth(Constants.SCREEN_WIDTH);
                mediaView.setFitHeight(Constants.SCREEN_HEIGHT);
                mediaView.setPreserveRatio(false);

                root.getChildren().add(0, mediaView);
            } else {
                mediaView.setMediaPlayer(videoPlayer);
            }

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
            button.setText("⚙");
            button.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        }

        button.setStyle(button.getStyle() + "-fx-background-color: transparent; -fx-border-color: transparent;");
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(width);
        button.setPrefHeight(height);

        if (hoverImage != null && clickImage != null) {
            button.setOnMouseEntered(event -> {
                setButtonGraphic(button, hoverImage, width, height);
            });
            button.setOnMouseExited(event -> setButtonGraphic(button, image, width, height));
            button.setOnMousePressed(event -> setButtonGraphic(button, clickImage, width, height));
            button.setOnMouseReleased(event -> setButtonGraphic(button, image, width, height));
        }

        if (action != null) {
            button.setOnAction(event -> {
                if (soundPath != null && !soundPath.isEmpty()) {
                    audioManager.playSoundEffect(soundPath);
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
        return mainContainer;
    }

    public void cleanup() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.dispose();
        }
    }
}