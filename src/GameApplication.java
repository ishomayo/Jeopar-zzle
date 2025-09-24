import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;

public class GameApplication extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Game Application");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);
        primaryStage.setResizable(false);
        
        
        // Start with splash screen
        SplashScreen splashScreen = new SplashScreen(primaryStage);
        Scene splashScene = new Scene(splashScreen.getRoot(), 1000, 600);
        primaryStage.setScene(splashScene);
        primaryStage.show();
        
        // Transition to lobby after splash screen
        splashScreen.setOnSplashComplete(() -> {
            LobbyScreen lobbyScreen = new LobbyScreen(primaryStage);
            Scene lobbyScene = new Scene(lobbyScreen.getRoot(), 1000, 600);
            primaryStage.setScene(lobbyScene);
        });
    }
}