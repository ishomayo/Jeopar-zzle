package src.main;
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
            Media media = new Media(getClass().getResource(Constants.LOBBY_BACKGROUND_VIDEO).toExternalForm());
            videoPlayer = new MediaPlayer(media);
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
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667db6, #0082c8, #0082c8, #667db6);");
        }
    }
    
    private void createButtons() {
        // Play Button
        Button playButton = createImageButton(
            loadImage(Constants.PLAY_BUTTON),
            loadImage(Constants.PLAY_BUTTON_HOVER),
            loadImage(Constants.PLAY_BUTTON_CLICK),
            400, 250,
            Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
            Constants.BUTTON_CLICK_SOUND,
            event -> onPlayClicked()
        );
        
        // Options Button
        Button optionsButton = createImageButton(
            loadImage(Constants.OPTIONS_BUTTON),
            loadImage(Constants.OPTIONS_BUTTON_HOVER),
            loadImage(Constants.OPTIONS_BUTTON_CLICK),
            400, 330,
            Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
            Constants.BUTTON_CLICK_SOUND,
            event -> onOptionsClicked()
        );
        
        // Exit Button
        Button exitButton = createImageButton(
            loadImage(Constants.EXIT_BUTTON),
            loadImage(Constants.EXIT_BUTTON_HOVER),
            loadImage(Constants.EXIT_BUTTON_CLICK),
            400, 410,
            Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT,
            Constants.BUTTON_CLICK_SOUND,
            event -> onExitClicked()
        );
        
        root.getChildren().addAll(playButton, optionsButton, exitButton);
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
            button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
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
                    // player.playSoundEffect(soundPath); // Uncomment when AudioPlayer is implemented
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
    
    private void onOptionsClicked() {
        System.out.println("Options button clicked!");
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