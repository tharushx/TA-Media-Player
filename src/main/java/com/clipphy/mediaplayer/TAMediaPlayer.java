package com.clipphy.mediaplayer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class TAMediaPlayer extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the main FXML layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clipphy/mediaplayer/fxml/main-view.fxml"));
        Parent root = loader.load();
        
        // Set up mouse events for window dragging (custom window decoration)
        setupWindowDragging(primaryStage, root);
        
        // Set up the scene
        Scene scene = new Scene(root);
        
        // Apply dark theme as default
        scene.getStylesheets().add(getClass().getResource("/com/clipphy/mediaplayer/css/dark-theme.css").toExternalForm());
        
        // Configure the stage
        primaryStage.setTitle("TA Media Player Beta");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Configure fullscreen behavior
        primaryStage.setFullScreenExitHint(""); // Remove default exit hint
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.valueOf("ESCAPE"));
        
        // Set application icons
        setApplicationIcons(primaryStage);
        
        // Get controller reference and set the stage AFTER the scene is set up
        MainController controller = loader.getController();
        controller.setStage(primaryStage);
        
        // Show the stage
        primaryStage.show();
    }
    
    /**
     * Sets the application icons with multiple sizes
     */
    private void setApplicationIcons(Stage stage) {
        // First try to load static icon resource
        try {
            // Try to load the static icon image
            Image logoImage = new Image(getClass().getResourceAsStream("/com/clipphy/mediaplayer/images/logoicon.png"));
            if (logoImage != null && !logoImage.isError()) {
                stage.getIcons().add(logoImage);
                return; // Successfully loaded the static image
            }
        } catch (Exception e) {
            System.out.println("Static icon not found, using generated icons: " + e.getMessage());
        }
        
        // Fall back to programmatically generated icons
        try {
            // Generate icons of different sizes
            stage.getIcons().addAll(
                LogoGenerator.generateLogo(16),  // Small icon
                LogoGenerator.generateLogo(32),  // Medium icon
                LogoGenerator.generateDetailedLogo(64),  // Large icon
                LogoGenerator.generateDetailedLogo(128)  // Extra large icon
            );
        } catch (Exception e) {
            System.err.println("Could not generate application icons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupWindowDragging(Stage stage, Parent root) {
        // Allow dragging from anywhere in the header bar
        root.setOnMousePressed(event -> {
            // Only allow dragging from the top part of the window (header)
            if (event.getSceneY() < 40) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        
        root.setOnMouseDragged(event -> {
            // Only drag if the press originated in the header area
            if (yOffset < 40) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
} 