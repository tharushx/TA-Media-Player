package com.clipphy.mediaplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class SubtitleManager {
    
    private final List<Subtitle> subtitles = new ArrayList<>();
    private final Label subtitleLabel;
    private MediaPlayer mediaPlayer;
    private boolean isActive = false;
    
    public SubtitleManager(Label subtitleLabel) {
        this.subtitleLabel = subtitleLabel;
    }
    
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        if (isActive && mediaPlayer != null) {
            setupSubtitleListener();
        }
    }
    
    public boolean loadSubtitle(File file) {
        if (file == null || !file.exists() || !file.getName().toLowerCase().endsWith(".srt")) {
            return false;
        }
        
        try {
            parseSubtitleFile(file);
            isActive = !subtitles.isEmpty();
            
            if (isActive && mediaPlayer != null) {
                setupSubtitleListener();
            }
            
            return isActive;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void disable() {
        isActive = false;
        subtitles.clear();
        subtitleLabel.setText("");
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    private void parseSubtitleFile(File file) throws IOException {
        subtitles.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int index = 0;
            String text = "";
            Duration start = Duration.ZERO;
            Duration end = Duration.ZERO;
            
            // A basic state machine to parse SRT files
            int state = 0; // 0: expecting index, 1: expecting time, 2: expecting text, 3: expecting blank line
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                switch (state) {
                    case 0: // Expecting index number
                        if (!line.isEmpty()) {
                            try {
                                index = Integer.parseInt(line);
                                state = 1;
                            } catch (NumberFormatException e) {
                                // Not a number, skip this line
                            }
                        }
                        break;
                        
                    case 1: // Expecting time range
                        if (!line.isEmpty()) {
                            Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})\\s+-->\\s+(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})");
                            Matcher matcher = pattern.matcher(line);
                            
                            if (matcher.find()) {
                                int startHours = Integer.parseInt(matcher.group(1));
                                int startMinutes = Integer.parseInt(matcher.group(2));
                                int startSeconds = Integer.parseInt(matcher.group(3));
                                int startMillis = Integer.parseInt(matcher.group(4));
                                
                                int endHours = Integer.parseInt(matcher.group(5));
                                int endMinutes = Integer.parseInt(matcher.group(6));
                                int endSeconds = Integer.parseInt(matcher.group(7));
                                int endMillis = Integer.parseInt(matcher.group(8));
                                
                                start = Duration.hours(startHours).add(Duration.minutes(startMinutes))
                                        .add(Duration.seconds(startSeconds)).add(Duration.millis(startMillis));
                                        
                                end = Duration.hours(endHours).add(Duration.minutes(endMinutes))
                                        .add(Duration.seconds(endSeconds)).add(Duration.millis(endMillis));
                                
                                state = 2;
                                text = "";
                            }
                        }
                        break;
                        
                    case 2: // Expecting subtitle text
                        if (line.isEmpty()) {
                            // Empty line indicates end of this subtitle
                            subtitles.add(new Subtitle(index, start, end, text.trim()));
                            state = 0;
                        } else {
                            // Append the line to the subtitle text
                            if (!text.isEmpty()) {
                                text += "\n";
                            }
                            text += line;
                        }
                        break;
                }
            }
            
            // Add the last subtitle if we have a complete one
            if (state == 2 && !text.isEmpty()) {
                subtitles.add(new Subtitle(index, start, end, text.trim()));
            }
        }
    }
    
    private void setupSubtitleListener() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            updateSubtitle(newTime);
        });
    }
    
    private void updateSubtitle(Duration currentTime) {
        if (!isActive) return;
        
        for (Subtitle subtitle : subtitles) {
            if (currentTime.greaterThanOrEqualTo(subtitle.getStartTime()) && 
                currentTime.lessThan(subtitle.getEndTime())) {
                Platform.runLater(() -> subtitleLabel.setText(subtitle.getText()));
                return;
            }
        }
        
        // No subtitle for current time
        Platform.runLater(() -> subtitleLabel.setText(""));
    }
    
    private static class Subtitle {
        private final int index;
        private final Duration startTime;
        private final Duration endTime;
        private final String text;
        
        public Subtitle(int index, Duration startTime, Duration endTime, String text) {
            this.index = index;
            this.startTime = startTime;
            this.endTime = endTime;
            this.text = text;
        }
        
        public int getIndex() {
            return index;
        }
        
        public Duration getStartTime() {
            return startTime;
        }
        
        public Duration getEndTime() {
            return endTime;
        }
        
        public String getText() {
            return text;
        }
    }
} 