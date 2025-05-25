module com.clipphy.mediaplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    
    opens com.clipphy.mediaplayer to javafx.fxml;
    exports com.clipphy.mediaplayer;
} 