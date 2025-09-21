import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public class AudioPlayer {
    private Map<String, MediaPlayer> soundEffects;
    private MediaPlayer backgroundMusicPlayer;
    
    public AudioPlayer() {
        soundEffects = new HashMap<>();
    }
    
    public void playSoundEffect(String soundPath) {
        try {
            // Check if sound is already loaded
            if (!soundEffects.containsKey(soundPath)) {
                Media sound = new Media(getClass().getResource(soundPath).toExternalForm());
                MediaPlayer player = new MediaPlayer(sound);
                soundEffects.put(soundPath, player);
            }
            
            MediaPlayer player = soundEffects.get(soundPath);
            player.stop(); // Stop any current playback
            player.seek(Duration.ZERO); // Reset to beginning
            player.play();
            
        } catch (Exception e) {
            System.out.println("Could not play sound: " + soundPath);
            e.printStackTrace();
        }
    }
    
    public void playBackgroundMusic(String musicPath) {
        try {
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.stop();
            }
            
            Media music = new Media(getClass().getResource(musicPath).toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(music);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.5); // 50% volume for background music
            backgroundMusicPlayer.play();
            
        } catch (Exception e) {
            System.out.println("Could not play background music: " + musicPath);
            e.printStackTrace();
        }
    }
    
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }
    
    public void setBackgroundMusicVolume(double volume) {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume)));
        }
    }
    
    public void setSFXVolume(double volume) {
        for (MediaPlayer player : soundEffects.values()) {
            player.setVolume(Math.max(0.0, Math.min(1.0, volume)));
        }
    }
    
    public void cleanup() {
        // Stop and dispose all sound effects
        for (MediaPlayer player : soundEffects.values()) {
            player.stop();
            player.dispose();
        }
        soundEffects.clear();
        
        // Stop and dispose background music
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
        }
    }
}