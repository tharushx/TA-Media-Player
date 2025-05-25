package com.clipphy.mediaplayer;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AboutController implements Initializable {

    @FXML private Button closeButton;
    @FXML private ImageView logoImageView;
    @FXML private Label versionLabel;

    private Image logoImage; // Store the logo image for reuse

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set the logo image for ImageView
        if (logoImageView != null) {
            try {
                // Try to load the static logo image
                logoImage = new Image(getClass().getResourceAsStream("/com/clipphy/mediaplayer/images/logoicon.png"));
                if (logoImage == null || logoImage.isError()) {
                    // Fall back to generated logo if static image fails
                    logoImage = LogoGenerator.generateDetailedLogo(150);
                }
            } catch (Exception e) {
                // Fall back to generated logo on exception
                logoImage = LogoGenerator.generateDetailedLogo(150);
            }
            logoImageView.setImage(logoImage);
        }

        // Set version information
        if (versionLabel != null) {
            versionLabel.setText("Version: 1.0.0 Beta (Build 2025)");
        }
    }

    // Method to set the title bar icon when Stage is available
    public void setStageIcon(Stage stage) {
        if (stage != null && logoImage != null) {
            stage.getIcons().add(logoImage);
        }
    }

    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}