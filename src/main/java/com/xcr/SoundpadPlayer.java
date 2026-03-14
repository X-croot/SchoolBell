package com.xcr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

public class SoundpadPlayer extends JPanel {
    String name, soundId;
    Color color;
    File soundFile;
    double startSec = 0, endSec = 0, totalDuration = 0;
    JButton playBtn, pauseBtn, stopBtn;
    JSlider progressSlider;
    JLabel timeLabel, nameLabel;
    JSpinner startSpinner, endSpinner;
    Clip clip;
    Timer progressTimer;
    boolean isPlaying = false, isPaused = false;
    long pausePosition = 0;

    SoundpadPlayer(String name, String soundId, Color color, File soundFile) {
        this.name = name;
        this.soundId = soundId;
        this.color = color;
        this.soundFile = soundFile;
        calculateDuration();
        endSec = totalDuration;

        setBackground(App.BG_CARD);
        setOpaque(true);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3, true),
            new EmptyBorder(18, 18, 18, 18)
        ));
        setPreferredSize(new Dimension(400, 270));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        nameLabel = new JLabel("\ud83c\udfb5 " + name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        nameLabel.setForeground(color);
        topPanel.add(nameLabel, BorderLayout.WEST);
        
        JButton deleteBtn = new JButton("\u2715");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deleteBtn.setForeground(App.ACCENT_RED);
        deleteBtn.setBackground(App.BG_CARD);
        deleteBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deletePlayer());
        topPanel.add(deleteBtn, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        progressSlider = new JSlider(0, 1000, 0);
        progressSlider.setBackground(App.BG_CARD);
        progressSlider.setOpaque(true);
        progressSlider.setPreferredSize(new Dimension(340, 28));
        progressSlider.addChangeListener(e -> {
            if (progressSlider.getValueIsAdjusting() && clip != null && !isPlaying) {
                pausePosition = (long) (progressSlider.getValue() / 1000.0 * totalDuration * 1000000);
            }
        });
        centerPanel.add(progressSlider);
        centerPanel.add(Box.createVerticalStrut(8));

        timeLabel = new JLabel("0:00 / " + formatTime(totalDuration));
        timeLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
        timeLabel.setForeground(App.TEXT_SECONDARY);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(timeLabel);
        centerPanel.add(Box.createVerticalStrut(12));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        controlPanel.setOpaque(false);
        
        playBtn = App.createButton("\u25b6", App.ACCENT_GREEN, 55, 48);
        playBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pauseBtn = App.createButton("\u23f8", App.ACCENT_ORANGE, 55, 48);
        pauseBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pauseBtn.setEnabled(false);
        stopBtn = App.createButton("\u23f9", App.ACCENT_RED, 55, 48);
        stopBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        stopBtn.setEnabled(false);
        
        playBtn.addActionListener(e -> playSound());
        pauseBtn.addActionListener(e -> pauseSound());
        stopBtn.addActionListener(e -> stopSound());
        
        controlPanel.add(playBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stopBtn);
        centerPanel.add(controlPanel);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        bottomPanel.setOpaque(false);
        
        JLabel startLbl = new JLabel("Start:");
        startLbl.setForeground(App.TEXT_MUTED);
        startLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bottomPanel.add(startLbl);
        
        startSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, totalDuration, 0.5));
        startSpinner.setPreferredSize(new Dimension(70, 28));
        styleSpinner(startSpinner);
        startSpinner.addChangeListener(e -> startSec = (double) startSpinner.getValue());
        bottomPanel.add(startSpinner);
        
        bottomPanel.add(new JLabel("sec"));
        bottomPanel.add(Box.createHorizontalStrut(15));
        
        JLabel endLbl = new JLabel("End:");
        endLbl.setForeground(App.TEXT_MUTED);
        endLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bottomPanel.add(endLbl);
        
        endSpinner = new JSpinner(new SpinnerNumberModel(totalDuration, 0.0, totalDuration, 0.5));
        endSpinner.setPreferredSize(new Dimension(70, 28));
        styleSpinner(endSpinner);
        endSpinner.addChangeListener(e -> endSec = (double) endSpinner.getValue());
        bottomPanel.add(endSpinner);
        
        bottomPanel.add(new JLabel("sec"));

        add(bottomPanel, BorderLayout.SOUTH);
    }

    void styleSpinner(JSpinner sp) {
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(App.BG_INPUT);
            tf.setForeground(App.TEXT_PRIMARY);
            tf.setCaretColor(App.ACCENT_CYAN);
        }
    }

    void calculateDuration() {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = stream.getFormat();
            totalDuration = stream.getFrameLength() / format.getFrameRate();
            stream.close();
        } catch (Exception e) {
            totalDuration = 30;
        }
    }

    void playSound() {
        if (isPaused && clip != null) {
            clip.setMicrosecondPosition(pausePosition);
            clip.start();
            isPlaying = true;
            isPaused = false;
            playBtn.setText("\u25b6");
            playBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
            stopBtn.setEnabled(true);
            startProgressTimer();
            App.setStatus("\ud83d\udd0a Resuming: " + name);
            return;
        }

        App.audioPool.submit(() -> {
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(soundFile);
                AudioFormat format = stream.getFormat();
                AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(), 16, format.getChannels(),
                    format.getChannels() * 2, format.getSampleRate(), false);
                
                AudioInputStream audioStream;
                if (AudioSystem.isConversionSupported(targetFormat, format)) {
                    audioStream = AudioSystem.getAudioInputStream(targetFormat, stream);
                } else {
                    audioStream = stream;
                }

                clip = AudioSystem.getClip();
                clip.open(audioStream);

                double actualStart = Math.max(0, startSec);
                double actualEnd = endSec > 0 ? Math.min(endSec, totalDuration) : totalDuration;
                
                clip.setMicrosecondPosition((long) (actualStart * 1000000));
                clip.start();
                
                isPlaying = true;
                isPaused = false;
                pausePosition = 0;
                
                final double finalEndSec = actualEnd;
                
                SwingUtilities.invokeLater(() -> {
                    playBtn.setEnabled(false);
                    pauseBtn.setEnabled(true);
                    stopBtn.setEnabled(true);
                    App.setStatus("\ud83d\udd0a Playing: " + name);
                });
                
                startProgressTimer();
                
                while (clip != null && clip.isRunning()) {
                    if (clip.getMicrosecondPosition() >= finalEndSec * 1000000) {
                        clip.stop();
                        break;
                    }
                    Thread.sleep(50);
                }
                
                if (!isPaused) stopSound();
                
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    App.error("Could not play sound: " + e.getMessage());
                    stopSound();
                });
            }
        });
    }

    void pauseSound() {
        if (clip != null && clip.isRunning()) {
            pausePosition = clip.getMicrosecondPosition();
            clip.stop();
            isPlaying = false;
            isPaused = true;
            
            if (progressTimer != null) {
                progressTimer.cancel();
                progressTimer = null;
            }
            
            playBtn.setText("\u25b6");
            playBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            App.setStatus("\u23f8 Paused: " + name + " [" + formatTime(pausePosition / 1000000.0) + "]");
        }
    }

    void stopSound() {
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer = null;
        }
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        isPlaying = false;
        isPaused = false;
        pausePosition = 0;
        
        SwingUtilities.invokeLater(() -> {
            progressSlider.setValue(0);
            timeLabel.setText("0:00 / " + formatTime(totalDuration));
            playBtn.setText("\u25b6");
            playBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            App.setStatus("System ready");
        });
    }

    void startProgressTimer() {
        if (progressTimer != null) progressTimer.cancel();
        progressTimer = new Timer();
        progressTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (clip != null && clip.isRunning()) {
                    double pos = clip.getMicrosecondPosition() / 1000000.0;
                    SwingUtilities.invokeLater(() -> {
                        progressSlider.setValue((int) (pos / totalDuration * 1000));
                        timeLabel.setText(formatTime(pos) + " / " + formatTime(totalDuration));
                    });
                }
            }
        }, 0, 100);
    }

    void deletePlayer() {
        if (App.confirm("Delete '" + name + "'?")) {
            stopSound();
            App.soundpadPlayers.remove(this);
            App.soundpadGrid.remove(this);
            App.soundpadGrid.revalidate();
            App.soundpadGrid.repaint();
            soundFile.delete();
            App.saveConfig();
        }
    }

    void setRange(double start, double end) {
        this.startSec = start;
        this.endSec = end > 0 ? end : totalDuration;
        startSpinner.setValue(startSec);
        endSpinner.setValue(endSec);
    }

    String formatTime(double seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }

    JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("name", name);
        o.put("id", soundId);
        o.put("color", color.getRGB());
        o.put("startSec", startSec);
        o.put("endSec", endSec);
        return o;
    }
}