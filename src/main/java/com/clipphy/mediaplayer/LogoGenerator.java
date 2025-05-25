package com.clipphy.mediaplayer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Utility class to generate a logo for the TA Media Player application.
 * This creates a dynamic logo if a static resource isn't available.
 */
public class LogoGenerator {
    
    /**
     * Generates a logo image of the specified size.
     * 
     * @param size The width and height of the logo in pixels
     * @return The generated logo as an Image
     */
    public static Image generateLogo(int size) {
        // Create a canvas to draw on
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Background - dark blue to royal blue gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, size, size,
            false, null,
            new Stop(0, Color.rgb(20, 20, 50)),
            new Stop(1, Color.rgb(40, 40, 120))
        );
        
        gc.setFill(gradient);
        gc.fillRoundRect(0, 0, size, size, size * 0.2, size * 0.2);
        
        // Add a subtle glow/border
        gc.setStroke(Color.rgb(100, 150, 255, 0.7));
        gc.setLineWidth(size * 0.05);
        gc.strokeRoundRect(size * 0.05, size * 0.05, size * 0.9, size * 0.9, size * 0.15, size * 0.15);
        
        // Add text
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        
        // Font size is proportional to the logo size
        double fontSize = size * 0.5;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        
        // Draw the text "TA" centered in the icon
        gc.fillText("TA", size / 2, size * 0.65);
        
        // Convert the canvas to an image
        WritableImage image = canvas.snapshot(null, null);
        return image;
    }
    
    /**
     * Generates a more detailed logo with a play button for larger icons.
     * 
     * @param size The width and height of the logo in pixels
     * @return The generated logo as an Image
     */
    public static Image generateDetailedLogo(int size) {
        if (size < 64) {
            // For smaller sizes, use the simple logo
            return generateLogo(size);
        }
        
        // Create a canvas to draw on
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Background - a more sophisticated gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, size, size,
            false, null,
            new Stop(0, Color.rgb(30, 30, 70)),
            new Stop(0.5, Color.rgb(40, 40, 100)),
            new Stop(1, Color.rgb(50, 50, 150))
        );
        
        gc.setFill(gradient);
        gc.fillRoundRect(0, 0, size, size, size * 0.2, size * 0.2);
        
        // Add a glossy effect
        Paint glossGradient = new LinearGradient(
            0, 0, 0, size / 2,
            false, null,
            new Stop(0, Color.rgb(255, 255, 255, 0.3)),
            new Stop(1, Color.rgb(255, 255, 255, 0.0))
        );
        
        gc.setFill(glossGradient);
        gc.fillRoundRect(size * 0.1, size * 0.1, size * 0.8, size * 0.4, size * 0.1, size * 0.1);
        
        // Draw the text "TA" at the top
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        double fontSize = size * 0.35;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        gc.fillText("TA", size / 2, size * 0.45);
        
        // Draw a play triangle in the bottom half
        gc.setFill(Color.WHITE);
        double triangleSize = size * 0.25;
        double[] xPoints = {
            size / 2 - triangleSize / 2,
            size / 2 - triangleSize / 2,
            size / 2 + triangleSize / 2
        };
        double[] yPoints = {
            size * 0.6,
            size * 0.6 + triangleSize,
            size * 0.6 + triangleSize / 2
        };
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Add a subtle border
        gc.setStroke(Color.rgb(100, 150, 255, 0.7));
        gc.setLineWidth(size * 0.03);
        gc.strokeRoundRect(size * 0.05, size * 0.05, size * 0.9, size * 0.9, size * 0.15, size * 0.15);
        
        // Convert the canvas to an image
        WritableImage image = canvas.snapshot(null, null);
        return image;
    }
    
    /**
     * Generates a light themed logo for when the user selects light mode
     * 
     * @param size The width and height of the logo in pixels
     * @return The generated logo as an Image
     */
    public static Image generateLightThemeLogo(int size) {
        // Create a canvas to draw on
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Background - light blue gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, size, size,
            false, null,
            new Stop(0, Color.rgb(220, 240, 255)),
            new Stop(1, Color.rgb(180, 210, 240))
        );
        
        gc.setFill(gradient);
        gc.fillRoundRect(0, 0, size, size, size * 0.2, size * 0.2);
        
        // Add a subtle border
        gc.setStroke(Color.rgb(40, 100, 180, 0.7));
        gc.setLineWidth(size * 0.05);
        gc.strokeRoundRect(size * 0.05, size * 0.05, size * 0.9, size * 0.9, size * 0.15, size * 0.15);
        
        // Add text
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.rgb(20, 60, 120)); // Dark blue text for light theme
        
        // Font size is proportional to the logo size
        double fontSize = size * 0.5;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        
        // Draw the text "TA" centered in the icon
        gc.fillText("TA", size / 2, size * 0.65);
        
        // Convert the canvas to an image
        WritableImage image = canvas.snapshot(null, null);
        return image;
    }
} 