import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioManager {
    private static AudioManager instance;
    private Map<String, MediaPlayer> soundEffects;
    private MediaPlayer backgroundMusicPlayer;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private double musicVolume = 0.5;
    private double sfxVolume = 0.7;
    private String currentMusicPath = null;
    
    private AudioManager() {
        soundEffects = new HashMap<>();
    }
    
    // Singleton instance
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    // Play sound effect
    public void playSoundEffect(String soundPath) {
        if (!sfxEnabled) return;
        
        try {
            if (!soundEffects.containsKey(soundPath)) {
                Media sound = new Media(getClass().getResource(soundPath).toExternalForm());
                MediaPlayer player = new MediaPlayer(sound);
                player.setVolume(sfxVolume);
                soundEffects.put(soundPath, player);
            }
            
            MediaPlayer player = soundEffects.get(soundPath);
            player.stop();
            player.seek(Duration.ZERO);
            player.setVolume(sfxVolume);
            player.play();
            
        } catch (Exception e) {
            System.out.println("Could not play sound: " + soundPath);
            e.printStackTrace();
        }
    }
    
    // Play background music
    public void playBackgroundMusic(String musicPath) {
        // Don't restart if same music is already playing
        if (currentMusicPath != null && currentMusicPath.equals(musicPath) && 
            backgroundMusicPlayer != null && backgroundMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }
        
        try {
            // Stop current music
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.dispose();
            }
            
            Media music = new Media(getClass().getResource(musicPath).toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(music);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(musicEnabled ? musicVolume : 0);
            backgroundMusicPlayer.play();
            
            currentMusicPath = musicPath;
            
            System.out.println("Playing background music: " + musicPath);
            
        } catch (Exception e) {
            System.out.println("Could not play background music: " + musicPath);
            e.printStackTrace();
        }
    }
    
    // Stop background music
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            currentMusicPath = null;
        }
    }
    
    // Toggle music on/off
    public void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(musicEnabled ? musicVolume : 0);
        }
        System.out.println("Music " + (musicEnabled ? "enabled" : "disabled"));
    }
    
    // Toggle sound effects on/off
    public void toggleSFX() {
        sfxEnabled = !sfxEnabled;
        System.out.println("SFX " + (sfxEnabled ? "enabled" : "disabled"));
    }
    
    // Getters for state

    // Getters for state
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public boolean isSFXEnabled() {
        return sfxEnabled;
    }
    
    public double getMusicVolume() {
        return musicVolume;
    }
    
    public double getSFXVolume() {
        return sfxVolume;
    }
    
    // Set volumes
    public void setMusicVolume(double volume) {
        musicVolume = Math.max(0.0, Math.min(1.0, volume));
        if (backgroundMusicPlayer != null && musicEnabled) {
            backgroundMusicPlayer.setVolume(musicVolume);
        }
    }
    
    public void setSFXVolume(double volume) {
        sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        for (MediaPlayer player : soundEffects.values()) {
            player.setVolume(sfxVolume);
        }
    }
    
    // Cleanup
    public void cleanup() {
        for (MediaPlayer player : soundEffects.values()) {
            player.stop();
            player.dispose();
        }
        soundEffects.clear();
        
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
            backgroundMusicPlayer = null;
        }
        
        currentMusicPath = null;
    }
    
}