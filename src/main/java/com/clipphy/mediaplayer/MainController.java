package com.clipphy.mediaplayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class MainController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button fullscreenButton;
    @FXML private Button muteButton;
    @FXML private StackPane mediaPane;
    @FXML private MediaView mediaView;
    @FXML private Slider timeSlider;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Slider volumeSlider;
    @FXML private ListView<String> playlistView;
    @FXML private Label mediaInfoLabel;
    @FXML private Label statusLabel;
    @FXML private TabPane settingsTabPane;
    @FXML private ToggleButton playlistToggle;
    @FXML private VBox playlistPanel;
    @FXML private ToggleGroup themeToggleGroup;
    @FXML private Slider playbackSpeedSlider;
    @FXML private ComboBox<String> equalizerPresetComboBox;
    @FXML private Label subtitleLabel;
    @FXML private RadioButton darkThemeRadio;
    @FXML private RadioButton lightThemeRadio;
    @FXML private Button maximizeButton;
    @FXML private Button themeToggleButton;
    @FXML private Label themeIcon;
    @FXML private VBox controlBar;
    @FXML private Button settingsButton;
    @FXML private Button openFileButton;
    @FXML private Button subtitleButton;
    @FXML private Button aboutButton;

    @FXML private ImageView openFileIcon;
    @FXML private ImageView subtitleIcon;
    @FXML private ImageView fullscreenIcon;
    @FXML private ImageView aboutIcon;
    @FXML private ImageView settingsIcon;
    @FXML private ImageView playlistIcon;

    private Stage stage;
    private MediaPlayer mediaPlayer;
    private boolean isFullScreen = false;
    private boolean isMaximized = false;
    private boolean isMuted = false;
    private final List<File> playlist = new ArrayList<>();
    private int currentPlaylistIndex = -1;
    private final FileChooser fileChooser = new FileChooser();
    private FileChooser subtitleChooser = new FileChooser();
    private boolean isPlaying = false;
    private double currentVolume = 0.5;
    private SubtitleManager subtitleManager;
    private String currentTheme = "dark-theme.css"; // Default to dark theme
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> cursorHideTask;
    private static final long CURSOR_HIDE_DELAY_MS = 3000; // Hide cursor after 3 seconds of inactivity
    private boolean isDarkTheme = true; // Track if we're using dark theme

    // Animation-related fields
    private FadeTransition controlBarFade;
    private Timeline controlBarHideTimeline;
    private boolean controlBarVisible = true;

    // Window size constants
    private static final double SMALL_WINDOW_WIDTH = 600.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize file chooser
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.mp3", "*.wav", "*.avi"),
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi"),
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Initialize subtitle chooser
        subtitleChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Subtitle Files", "*.srt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Initialize subtitle manager
        subtitleManager = new SubtitleManager(subtitleLabel);

        // Initialize playlist toggle
        playlistToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            playlistPanel.setVisible(newVal);
            playlistPanel.setManaged(newVal);
        });

        // Initialize volume slider
        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                currentVolume = newVal.doubleValue() / 100.0;
                mediaPlayer.setVolume(currentVolume);
            }
        });

        // Initialize equalizer presets
        equalizerPresetComboBox.getItems().addAll(
                "Flat", "Bass Boost", "Treble Boost", "Vocal", "Rock", "Pop", "Jazz", "Classical"
        );
        equalizerPresetComboBox.setValue("Flat");

        // Initialize playback speed slider
        playbackSpeedSlider.setValue(1.0);
        playbackSpeedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setRate(newVal.doubleValue());
                statusLabel.setText("Playback Speed: " + String.format("%.1fx", newVal.doubleValue()));
            }
        });

        // Initialize theme selection
        darkThemeRadio.setSelected(true);
        darkThemeRadio.setUserData("dark-theme.css");
        lightThemeRadio.setUserData("light-theme.css");

        themeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String themeName = newVal.getUserData().toString();
                applyTheme(themeName);
                updateThemeToggleButton(themeName);
            } else {
                // If no theme is selected, reselect the previous one
                themeToggleGroup.selectToggle(oldVal);
            }
        });

        // Set up double-click event on media pane to toggle fullscreen
        mediaPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleFullScreen();
                event.consume();
            }
        });

        // Set up drag and drop support with enhanced visual feedback
        setupEnhancedDragAndDrop();

        // Set up key bindings
        setupKeyBindings();

        // Initialize executor service for cursor hiding
        executorService = new ScheduledThreadPoolExecutor(1);

        // Initialize theme toggle button icons
        updateThemeToggleButton(currentTheme);
        
        // Initialize timeline slider click-to-seek functionality
        setupTimelineClickSeek();
        
        // Initialize animations for control bar
        setupControlBarAnimations();

        // Initialize icons based on default theme
        updateIconsForTheme();
    }


    private void setupTimelineClickSeek() {
        // Handle mouse press on the timeline to seek
        timeSlider.setOnMousePressed(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            }
        });
        
        // Handle mouse drag on the timeline for continuous seeking
        timeSlider.setOnMouseDragged(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            }
        });
        
        // Handle direct clicks on the track (not just the thumb)
        timeSlider.setOnMouseClicked(event -> {
            if (mediaPlayer != null) {
                // Calculate the position based on the click location
                double percent = event.getX() / timeSlider.getWidth();
                double newValue = percent * timeSlider.getMax();
                
                // Update the slider value
                timeSlider.setValue(newValue);
                
                // Seek to the new position
                mediaPlayer.seek(Duration.seconds(newValue));
                
                // Update the time label immediately
                currentTimeLabel.setText(formatTime(Duration.seconds(newValue)));
            }
        });
    }

    private void setupControlBarAnimations() {
        // Create fade transition for control bar
        controlBarFade = new FadeTransition(Duration.millis(300), controlBar);
        controlBarFade.setFromValue(0.0);
        controlBarFade.setToValue(1.0);
        
        // Create timeline for hiding control bar in fullscreen
        controlBarHideTimeline = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (isFullScreen && controlBarVisible) {
                    hideControlBar();
                }
            })
        );
        controlBarHideTimeline.setCycleCount(1);
    }

    private void showControlBar() {
        if (!controlBarVisible) {
            controlBarFade.setFromValue(0.0);
            controlBarFade.setToValue(1.0);
            controlBarFade.play();
            controlBar.setVisible(true);
            controlBarVisible = true;
            
            // Reset the hide timeline
            controlBarHideTimeline.playFromStart();
        } else {
            // If already visible, just reset the hide timeline
            controlBarHideTimeline.playFromStart();
        }
    }

    private void hideControlBar() {
        if (controlBarVisible) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), controlBar);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> controlBar.setVisible(false));
            fade.play();
            controlBarVisible = false;
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        // Apply default theme on startup
        applyTheme(currentTheme);

        // Set up full-screen bindings with enhanced transitions
        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            isFullScreen = newVal;
            updateFullscreenButton();

            if (isFullScreen) {
                // Set up mouse movement listener in fullscreen mode
                setupCursorHiding();
                setupFullscreenControlBar();
                
                // Initially hide control bar after a delay in fullscreen
                controlBarHideTimeline.playFromStart();
            } else {
                // Make sure cursor is visible when exiting fullscreen
                mainPane.setCursor(null);
                cancelCursorHideTask();
                
                // Ensure control bar is visible when exiting fullscreen
                controlBar.setOpacity(1.0);
                controlBar.setVisible(true);
                controlBarVisible = true;
            }
            
            // Create a fade transition for the entire scene when entering/exiting fullscreen
            Scene scene = stage.getScene();
            if (scene != null) {
                FadeTransition sceneFade = new FadeTransition(Duration.millis(200), mainPane);
                sceneFade.setFromValue(0.9);
                sceneFade.setToValue(1.0);
                sceneFade.play();
            }
        });

        // Set up maximized state tracking with smooth transition
        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            isMaximized = newVal;
            updateMaximizeButton();
            
            // Create a scale transition for maximize/restore
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), mainPane);
            scaleTransition.setFromX(0.95);
            scaleTransition.setFromY(0.95);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });

        // Handle window state change events to ensure proper fullscreen/maximize/minimize behavior
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
            // Make sure window state is initialized correctly
            updateFullscreenButton();
            updateMaximizeButton();
            
            // Set up mouse move event for the entire scene to show/hide controls in fullscreen
            // Do this after the window is shown to ensure scene is not null
            Scene scene = stage.getScene();
            if (scene != null) {
                scene.setOnMouseMoved(e -> {
                    if (isFullScreen) {
                        // Show cursor when mouse moves
                        mainPane.setCursor(null);
                        
                        // Reset cursor hide task
                        scheduleCursorHide();
                        
                        // Show control bar
                        showControlBar();
                        
                        // If mouse is near the bottom of the screen, keep control bar visible
                        if (e.getSceneY() > scene.getHeight() - 100) {
                            controlBarHideTimeline.stop();
                        } else {
                            controlBarHideTimeline.playFromStart();
                        }
                    }
                });
                
                // Set up window size listener for responsive UI
                setupWindowSizeListener(scene);
            }
        });
    }

    /**
     * Sets up listeners to handle responsive UI based on window width
     */
    private void setupWindowSizeListener(Scene scene) {
        // Apply initial state based on current window width
        updateResponsiveUI(scene.getWidth());

        // Listen for width changes
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            updateResponsiveUI(newWidth.doubleValue());
        });
    }
    
    /**
     * Updates UI elements based on window width
     */
    private void updateResponsiveUI(double width) {
        boolean isSmallWindow = width < SMALL_WINDOW_WIDTH;

        // Apply or remove the window-small class to the main pane
        if (isSmallWindow) {
            if (!mainPane.getStyleClass().contains("window-small")) {
                mainPane.getStyleClass().add("window-small");
            }
        } else {
            mainPane.getStyleClass().remove("window-small");
        }

        updatePlaybackControlButtons(isSmallWindow);
    }
    
    /**
     * Updates playback control buttons for better visibility in small window mode
     */
    private void updatePlaybackControlButtons(boolean isSmallWindow) {
        // Set font size for control buttons
        String fontSize = isSmallWindow ? "16px" : "14px";
        
        // Apply style to playback buttons
        if (playButton != null) playButton.setStyle("-fx-font-size: " + fontSize);
        if (pauseButton != null) pauseButton.setStyle("-fx-font-size: " + fontSize);
        if (stopButton != null) stopButton.setStyle("-fx-font-size: " + fontSize);
        if (previousButton != null) previousButton.setStyle("-fx-font-size: " + fontSize);
        if (nextButton != null) nextButton.setStyle("-fx-font-size: " + fontSize);
        
        // Update mute button with text or icon based on window size
        if (muteButton != null) {
            muteButton.setText(isSmallWindow ? (isMuted ? "🔇" : "🔊") : (isMuted ? "Unmute" : "Mute"));
        }
    }
    
    /**
     * Updates a button's text visibility based on window size
     * 
     * @param button The button to update
     * @param text The text to show/hide
     * @param icon The icon to use
     * @param isSmallWindow Whether we're in small window mode
     */
    private void updateButtonVisibility(Button button, String text, String icon, boolean isSmallWindow) {

        if (button == null) {
            System.out.println("Button is null");
            return;
        }

        HBox content = (HBox) button.getGraphic();
        if (content == null) {
            System.out.println("Button graphic (HBox) is null for button: " + button.getId());
            return;
        }

        ImageView iconView = null;
        Label textLabel = null;

        for (Node node : content.getChildren()) {
            if (node instanceof ImageView) {
                iconView = (ImageView) node;
            } else if (node instanceof Label && node.getStyleClass().contains("button-text")) {
                textLabel = (Label) node;
            }
        }

        if (textLabel != null) {
            textLabel.setVisible(!isSmallWindow);
            textLabel.setManaged(!isSmallWindow);
            System.out.println("Text label set to visible: " + !isSmallWindow + " for button: " + button.getId());
        } else {
            System.out.println("Text label is null for button: " + button.getId());
        }

        if (iconView != null) {
            iconView.setVisible(isSmallWindow);
            iconView.setManaged(isSmallWindow);
            iconView.setFitWidth(isSmallWindow ? 18 : 16);
            iconView.setFitHeight(isSmallWindow ? 18 : 16);
            System.out.println("Icon view set to visible: " + isSmallWindow + " for button: " + button.getId());
        } else {
            System.out.println("Icon view is null for button: " + button.getId());
        }

        content.setSpacing(isSmallWindow ? 0 : 4);
    }

    /**
     * Updates a toggle button's text visibility based on window size
     *
     * @param button The toggle button to update
     * @param text The text to show/hide
     * @param iconName Name The icon identifier (not used here since we're using images)
     * @param isSmallWindow Whether we're in small window mode
     */
    private void updateToggleButtonVisibility(ToggleButton button, String text, String iconName, boolean isSmallWindow) {
        if (button == null) {
            System.out.println("ToggleButton is null");
            return;
        }

        HBox content = (HBox) button.getGraphic();
        if (content == null) {
            System.out.println("ToggleButton graphic (HBox) is null for button: " + button.getId());
            return;
        }

        ImageView iconView = null;
        Label textLabel = null;

        for (Node node : content.getChildren()) {
            if (node instanceof ImageView) {
                iconView = (ImageView) node;
            } else if (node instanceof Label && node.getStyleClass().contains("button-text")) {
                textLabel = (Label) node;
            }
        }

        if (textLabel != null) {
            textLabel.setVisible(!isSmallWindow);
            textLabel.setManaged(!isSmallWindow);
            System.out.println("Text label set to visible: " + !isSmallWindow + " for toggle button: " + button.getId());
        } else {
            System.out.println("Text label is null for toggle button: " + button.getId());
        }

        if (iconView != null) {
            iconView.setVisible(isSmallWindow);
            iconView.setManaged(isSmallWindow);
            iconView.setFitWidth(isSmallWindow ? 18 : 16);
            iconView.setFitHeight(isSmallWindow ? 18 : 16);
            System.out.println("Icon view set to visible: " + isSmallWindow + " for toggle button: " + button.getId());
        } else {
            System.out.println("Icon view is null for toggle button: " + button.getId());
        }

        content.setSpacing(isSmallWindow ? 0 : 4);
    }

    @FXML
    private void handleSettingsButton() {
        // Toggle playlist panel visibility
        boolean isVisible = playlistPanel.isVisible();
        playlistPanel.setVisible(!isVisible);
        playlistPanel.setManaged(!isVisible);
        
        // Synchronize with playlist toggle button
        playlistToggle.setSelected(!isVisible);
        
        // Update settings button appearance
        settingsButton.getStyleClass().remove("selected-button");
        if (!isVisible) {
            settingsButton.getStyleClass().add("selected-button");
        }
    }

    /**
     * Updates all icons based on the current theme
     */
    private void updateIconsForTheme() {
        String iconPath = isDarkTheme ? "/com/clipphy/mediaplayer/images/white/" : "/com/clipphy/mediaplayer/images/black/";
        try {
            openFileIcon.setImage(new Image(getClass().getResource(iconPath + "folder" + (isDarkTheme ? "-white.png" : "-black.png")).toString()));
            subtitleIcon.setImage(new Image(getClass().getResource(iconPath + "subtitle" + (isDarkTheme ? "-white.png" : "-black.png")).toString()));
            fullscreenIcon.setImage(new Image(getClass().getResource(iconPath + "fullscreen" + (isDarkTheme ? "-white.png" : "-black.png")).toString()));
            aboutIcon.setImage(new Image(getClass().getResource(iconPath + "info" + (isDarkTheme ? "-white.png" : "-black.png")).toString()));
            settingsIcon.setImage(new Image(getClass().getResource(iconPath + "settings" + (isDarkTheme ? "-white.png" : "-black.png")).toString()));
            playlistIcon.setImage(new Image(getClass().getResource(iconPath + "playlist" + (isDarkTheme ? "-white.png" : "-black.png")).toString()));
        } catch (NullPointerException e) {
            System.out.println("Error loading icon: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFullscreenControlBar() {
        // Make sure the control bar starts visible then fades out
        controlBar.setVisible(true);
        controlBarVisible = true;
        
        // Add mouse moved and mouse exited listeners for control bar
        Scene scene = stage.getScene();
        if (scene != null) {
            scene.setOnMouseMoved(e -> {
                if (isFullScreen) {
                    // Calculate if mouse is near the bottom of the screen
                    boolean nearBottom = e.getSceneY() > scene.getHeight() - 100;
                    
                    // Always show cursor when mouse moves
                    mainPane.setCursor(null);
                    
                    // Reset cursor hide task
                    scheduleCursorHide();
                    
                    // Show control bar
                    showControlBar();
                    
                    // If mouse is near the bottom, keep control bar visible
                    if (nearBottom) {
                        controlBarHideTimeline.stop();
                    } else {
                        // Otherwise start the hide timer
                        controlBarHideTimeline.playFromStart();
                    }
                }
            });
        }
    }

    private void setupEnhancedDragAndDrop() {
        // Style class for drag-over visual feedback
        String dragOverClass = "drag-over";
        
        mediaPane.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()) {
                // Add visual feedback class
                mediaPane.getStyleClass().add(dragOverClass);
                event.consume();
            }
        });
        
        mediaPane.setOnDragExited(event -> {
            // Remove visual feedback class
            mediaPane.getStyleClass().remove(dragOverClass);
            event.consume();
        });
        
        mediaPane.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        
        mediaPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasFiles()) {
                success = true;
                List<File> mediaFiles = new ArrayList<>();
                List<File> subtitleFiles = new ArrayList<>();
                
                // Sort files into media and subtitle categories
                for (File file : db.getFiles()) {
                    if (isMediaFile(file)) {
                        mediaFiles.add(file);
                    } else if (file.getName().toLowerCase().endsWith(".srt")) {
                        subtitleFiles.add(file);
                    }
                }
                
                // Process media files
                for (File file : mediaFiles) {
                    addToPlaylist(file);
                }
                
                // If we have media files, play the first one
                if (!mediaFiles.isEmpty()) {
                    // If the playlist was empty, set the index to 0
                    boolean firstFile = (currentPlaylistIndex == -1);
                    
                    if (firstFile) {
                        currentPlaylistIndex = 0;
                        // Play the first dropped media file
                        playMedia(mediaFiles.get(0));
                        
                        // Show success animation
                        showDropSuccessAnimation();
                    }
                }
                
                // Process subtitle files if a media is playing
                if (!subtitleFiles.isEmpty() && mediaPlayer != null) {
                    loadSubtitleFile(subtitleFiles.get(0));
                }
            }
            
            // Remove visual feedback
            mediaPane.getStyleClass().remove(dragOverClass);
            
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupKeyBindings() {
        mainPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                togglePlayPause();
                event.consume();
            } else if (event.getCode() == KeyCode.F) {
                toggleFullScreen();
                event.consume();
            } else if (event.getCode() == KeyCode.M) {
                toggleMute();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE && isFullScreen) {
                // Exit fullscreen when ESC key is pressed
                stage.setFullScreen(false);
                event.consume();
            } else if (event.getCode() == KeyCode.F11) {
                // Toggle fullscreen with F11 key
                toggleFullScreen();
                event.consume();
            } else if (event.getCode() == KeyCode.F10) {
                // Toggle maximized with F10 key
                toggleMaximize();
                event.consume();
            }
        });
    }

    private void showDropSuccessAnimation() {
        // Create a short pulse animation to indicate successful media load
        ScaleTransition pulse = new ScaleTransition(Duration.millis(200), mediaView);
        pulse.setFromX(0.95);
        pulse.setFromY(0.95);
        pulse.setToX(1.0);
        pulse.setToY(1.0);
        pulse.setCycleCount(1);
        pulse.setAutoReverse(true);
        pulse.play();
        
        // Flash the status message
        String originalText = statusLabel.getText();
        statusLabel.setText("Media loaded successfully");
        
        // Reset status message after a delay
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> statusLabel.setText(originalText));
        pause.play();
    }

    private void applyTheme(String themeName) {
        if (stage != null) {
            Scene scene = stage.getScene();
            
            // Only update if the theme is actually changing
            if (!themeName.equals(currentTheme)) {
                // Create fade transition for theme change
                FadeTransition fade = new FadeTransition(Duration.millis(200), mainPane);
                fade.setFromValue(0.8);
                fade.setToValue(1.0);
                
                // When fade starts, change the stylesheet
                fade.setOnFinished(e -> {
                    // Remove the current stylesheet
                    scene.getStylesheets().clear();
                    
                    // Add the new stylesheet
                    scene.getStylesheets().add(getClass().getResource("/com/clipphy/mediaplayer/css/" + themeName).toExternalForm());
                });
                
                // Start the fade animation
                fade.play();
                
                currentTheme = themeName;
                isDarkTheme = themeName.contains("dark");
                String themeType = isDarkTheme ? "Dark" : "Light";
                
                // Update the theme toggle button appearance
                updateThemeToggleButton(themeName);
                
                statusLabel.setText("Theme changed to: " + themeType);
            }
        }
    }

    private void updateThemeToggleButton(String themeName) {
        if (themeIcon == null) return;
        
        // Set the icon based on current theme - moon for dark mode, sun for light mode
        boolean isDark = themeName.contains("dark");
        themeIcon.setText(isDark ? "☀" : "☾"); // Sun for dark mode (to switch to light), Moon for light mode
        
        // Update tooltip text
        themeToggleButton.setTooltip(new Tooltip(
            isDark ? "Switch to Light Theme" : "Switch to Dark Theme"));
    }

    @FXML
    private void handleThemeToggle() {
        // Toggle between light and dark theme
        String newTheme = currentTheme.contains("dark") ? "light-theme.css" : "dark-theme.css";
        
        // Update radio buttons to match
        if (newTheme.equals("dark-theme.css")) {
            darkThemeRadio.setSelected(true);
        } else {
            lightThemeRadio.setSelected(true);
        }
        
        applyTheme(newTheme);
        updateIconsForTheme();
    }

    @FXML
    private void handleOpenFile() {
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            boolean firstFile = playlist.isEmpty();

            for (File file : selectedFiles) {
                addToPlaylist(file);
            }

            if (firstFile) {
                currentPlaylistIndex = 0;
                playMedia(playlist.get(0));
            }
        }
    }

    @FXML
    private void handleLoadSubtitle() {
        File subtitleFile = subtitleChooser.showOpenDialog(stage);
        if (subtitleFile != null) {
            loadSubtitleFile(subtitleFile);
        }
    }

    private void loadSubtitleFile(File file) {
        if (file != null && file.exists() && file.getName().toLowerCase().endsWith(".srt")) {
            if (mediaPlayer != null) {
                boolean success = subtitleManager.loadSubtitle(file);
                if (success) {
                    statusLabel.setText("Subtitle loaded: " + file.getName());
                } else {
                    statusLabel.setText("Failed to load subtitle");
                }
            } else {
                statusLabel.setText("Please load a video first");
            }
        }
    }

    private void addToPlaylist(File file) {
        if (!playlist.contains(file)) {
            playlist.add(file);
            playlistView.getItems().add(file.getName());
        }
    }

    private void playMedia(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Link subtitle manager to new media player
            subtitleManager.setMediaPlayer(mediaPlayer);

            // Make media view resize with the window
            DoubleProperty width = mediaView.fitWidthProperty();
            DoubleProperty height = mediaView.fitHeightProperty();
            width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
            height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
            mediaView.setPreserveRatio(true);

            // Set up time labels and slider
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!timeSlider.isValueChanging()) {
                    timeSlider.setValue(newTime.toSeconds());
                }
                currentTimeLabel.setText(formatTime(newTime));
            });

            mediaPlayer.setOnReady(() -> {
                Duration total = media.getDuration();
                timeSlider.setMax(total.toSeconds());
                totalTimeLabel.setText(formatTime(total));
                mediaInfoLabel.setText("Playing: " + file.getName());

                // Apply saved volume
                mediaPlayer.setVolume(currentVolume);

                // Apply saved mute status
                mediaPlayer.setMute(isMuted);
            });

            // Set up time slider
            timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (!isChanging) {
                    mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                }
            });

            timeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (timeSlider.isValueChanging()) {
                    mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                }
            });

            // Auto play next file when done
            mediaPlayer.setOnEndOfMedia(() -> {
                if (currentPlaylistIndex < playlist.size() - 1) {
                    currentPlaylistIndex++;
                    playlistView.getSelectionModel().select(currentPlaylistIndex);
                    playMedia(playlist.get(currentPlaylistIndex));
                } else {
                    // Reached end of playlist
                    stopMedia();
                }
            });

            // Start playback
            mediaPlayer.play();
            isPlaying = true;
            updatePlayPauseButtons();

            // Select the current item in playlist
            playlistView.getSelectionModel().select(currentPlaylistIndex);

            // Apply current playback speed
            mediaPlayer.setRate(playbackSpeedSlider.getValue());

            // Auto-detect and load subtitle file with the same name
            String mediaPath = file.getAbsolutePath();
            String subtitlePath = mediaPath.substring(0, mediaPath.lastIndexOf('.')) + ".srt";
            File subtitleFile = new File(subtitlePath);
            if (subtitleFile.exists()) {
                loadSubtitleFile(subtitleFile);
            }

        } catch (Exception e) {
            statusLabel.setText("Error: Unable to play media file");
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePlayButton() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
            updatePlayPauseButtons();
        } else if (!playlist.isEmpty()) {
            // If we have something in the playlist but no media is playing
            currentPlaylistIndex = 0;
            playMedia(playlist.get(currentPlaylistIndex));
        }
    }

    @FXML
    private void handlePauseButton() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            updatePlayPauseButtons();
        }
    }

    @FXML
    private void handleStopButton() {
        stopMedia();
    }

    private void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            timeSlider.setValue(0);
            updatePlayPauseButtons();
        }
    }

    @FXML
    private void handlePreviousButton() {
        if (currentPlaylistIndex > 0) {
            currentPlaylistIndex--;
            playlistView.getSelectionModel().select(currentPlaylistIndex);
            playMedia(playlist.get(currentPlaylistIndex));
        }
    }

    @FXML
    private void handleNextButton() {
        if (currentPlaylistIndex < playlist.size() - 1) {
            currentPlaylistIndex++;
            playlistView.getSelectionModel().select(currentPlaylistIndex);
            playMedia(playlist.get(currentPlaylistIndex));
        }
    }

    @FXML
    private void handleFullscreenButton() {
        toggleFullScreen();
    }

    @FXML
    private void handleMaximizeButton() {
        toggleMaximize();
    }

    @FXML
    private void handleMuteButton() {
        toggleMute();
    }

    @FXML
    private void handlePlaylistItemClicked() {
        int selectedIndex = playlistView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < playlist.size()) {
            currentPlaylistIndex = selectedIndex;
            playMedia(playlist.get(currentPlaylistIndex));
        }
    }

    @FXML
    private void handleRemoveFromPlaylist() {
        int selectedIndex = playlistView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < playlist.size()) {
            playlist.remove(selectedIndex);
            playlistView.getItems().remove(selectedIndex);

            // Adjust currentPlaylistIndex if needed
            if (playlist.isEmpty()) {
                currentPlaylistIndex = -1;
                stopMedia();
            } else if (selectedIndex == currentPlaylistIndex) {
                // If the currently playing item was removed, play the next one
                if (currentPlaylistIndex >= playlist.size()) {
                    currentPlaylistIndex = playlist.size() - 1;
                }
                playMedia(playlist.get(currentPlaylistIndex));
            } else if (selectedIndex < currentPlaylistIndex) {
                currentPlaylistIndex--;
            }
        }
    }

    @FXML
    private void handleClearPlaylist() {
        playlist.clear();
        playlistView.getItems().clear();
        stopMedia();
        currentPlaylistIndex = -1;
    }

    @FXML
    private void handleAboutButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clipphy/mediaplayer/fxml/about-view.fxml"));
            Parent root = loader.load();

            // Get the AboutController instance
            AboutController aboutController = loader.getController();

            Stage aboutStage = new Stage();
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.initStyle(StageStyle.DECORATED);
            aboutStage.setTitle("About TA Media Player");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/clipphy/mediaplayer/css/" + currentTheme).toExternalForm());
            aboutStage.setScene(scene);
            aboutStage.setResizable(false);

            // Set the title bar icon
            aboutController.setStageIcon(aboutStage);

            aboutStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExitButton() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        // Shut down executor service
        if (executorService != null) {
            executorService.shutdown();
        }

        Platform.exit();
    }

    @FXML
    private void handleMinimizeButton() {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                handlePauseButton();
            } else {
                handlePlayButton();
            }
        }
    }

    private void toggleFullScreen() {
        if (stage != null) {
            // If maximized and going to fullscreen, first restore to normal
            if (isMaximized && !isFullScreen) {
                stage.setMaximized(false);
            }
            
            isFullScreen = !isFullScreen;
            
            // Create a fade transition for entering/exiting fullscreen
            FadeTransition fade = new FadeTransition(Duration.millis(200), mainPane);
            fade.setFromValue(0.8);
            fade.setToValue(1.0);
            fade.play();
            
            stage.setFullScreen(isFullScreen);
            
            // Set fullscreen exit hint to empty to remove the default "Press ESC to exit fullscreen" message
            stage.setFullScreenExitHint("");
            
            // But still allow ESC to exit fullscreen
            stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("ESCAPE"));
            
            updateFullscreenButton();
        }
    }

    private void toggleMaximize() {
        if (stage != null) {
            // If in fullscreen, exit fullscreen first
            if (isFullScreen) {
                stage.setFullScreen(false);
                isFullScreen = false;
                updateFullscreenButton();
            }
            
            // Create a scale transition for maximize/restore
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), mainPane);
            scaleTransition.setFromX(0.95);
            scaleTransition.setFromY(0.95);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            
            isMaximized = !isMaximized;
            stage.setMaximized(isMaximized);
            
            scaleTransition.play();
            updateMaximizeButton();
        }
    }

    private void toggleMute() {
        if (mediaPlayer != null) {
            isMuted = !isMuted;
            mediaPlayer.setMute(isMuted);
            
            // Update mute button icon
            themeIcon.setText(isMuted ? "☾" : "☀");
        }
    }

    private void updatePlayPauseButtons() {
        playButton.setVisible(!isPlaying);
        playButton.setManaged(!isPlaying);
        pauseButton.setVisible(isPlaying);
        pauseButton.setManaged(isPlaying);
    }

    private void updateFullscreenButton() {
        // Update fullscreen button appearance based on state
        fullscreenButton.setText(isFullScreen ? "Exit Fullscreen" : "Fullscreen");

        // Additionally, we can make sure that the fullscreen state is synchronized
        // between the stage and our internal variable
        if (stage != null && stage.isFullScreen() != isFullScreen) {
            isFullScreen = stage.isFullScreen();
        }
    }

    private void updateMaximizeButton() {
        // Update maximize button appearance based on state
        if (maximizeButton != null) {
            maximizeButton.setText(isMaximized ? "Restore" : "Maximize");
        }

        // Keep internal state in sync with actual window state
        if (stage != null && stage.isMaximized() != isMaximized) {
            isMaximized = stage.isMaximized();
        }
    }

    private void updateMuteButton() {
        // Update mute button appearance based on state
        muteButton.setText(isMuted ? "Unmute" : "Mute");
    }

    private String formatTime(Duration time) {
        int hours = (int) time.toHours();
        int minutes = (int) time.toMinutes() % 60;
        int seconds = (int) time.toSeconds() % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private boolean isMediaFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".wav");
    }

    private void setupCursorHiding() {
        // Reset cursor to visible first
        mainPane.setCursor(null);

        // Add mouse movement listener
        mainPane.setOnMouseMoved(event -> {
            // Show cursor when mouse moves
            mainPane.setCursor(null);
            // Schedule cursor hiding
            scheduleCursorHide();
        });

        mainPane.setOnMouseExited(event -> {
            // Show cursor when mouse exits
            mainPane.setCursor(null);
            cancelCursorHideTask();
        });

        // Initial schedule
        scheduleCursorHide();
    }

    private void scheduleCursorHide() {
        // Cancel any existing task
        cancelCursorHideTask();

        // Schedule new task
        cursorHideTask = executorService.schedule(() -> {
            if (isFullScreen) {
                Platform.runLater(() -> mainPane.setCursor(Cursor.NONE));
            }
        }, CURSOR_HIDE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void cancelCursorHideTask() {
        if (cursorHideTask != null && !cursorHideTask.isDone()) {
            cursorHideTask.cancel(true);
        }
    }
}