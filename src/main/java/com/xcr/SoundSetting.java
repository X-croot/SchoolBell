package com.xcr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
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

public class SoundSetting extends JPanel {
    String name, id, icon;
    Color color;
    JLabel fileLabel, durationLabel, positionLabel;
    JSpinner startSpinner, endSpinner;
    JButton playBtn, pauseBtn, stopBtn;
    JSlider progressSlider;
    File soundFile;
    double totalDuration = 0;
    Clip clip;
    long pausePosition = 0;
    boolean isPlaying = false, isPaused = false;
    Timer progressTimer;

    SoundSetting(String name, String id, Color color, String icon) {
        this.name = name;
        this.id = id;
        this.color = color;
        this.icon = icon;

        setBackground(App.BG_CARD);
        setOpaque(true);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3, true),
            new EmptyBorder(22, 22, 22, 22)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        titlePanel.add(iconLabel);
        JLabel titleLabel = new JLabel(name);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(color);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        JLabel fileLbl = new JLabel("Sound File:");
        fileLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fileLbl.setForeground(App.TEXT_PRIMARY);
        content.add(fileLbl, g);

        g.gridx = 1; g.weightx = 1;
        fileLabel = new JLabel("No sound selected");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fileLabel.setForeground(App.TEXT_SECONDARY);
        content.add(fileLabel, g);

        g.gridx = 2; g.weightx = 0;
        JButton browseBtn = App.createButton("\ud83d\udcc2 Browse", color, 120, 38);
        browseBtn.addActionListener(e -> selectSound());
        content.add(browseBtn, g);

        g.gridx = 0; g.gridy = 1;
        JLabel rangeLbl = new JLabel("Playback Range:");
        rangeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rangeLbl.setForeground(App.TEXT_PRIMARY);
        content.add(rangeLbl, g);

        g.gridx = 1; g.gridwidth = 2;
        JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rangePanel.setOpaque(false);

        rangePanel.add(createSmallLabel("Start:"));
        startSpinner = createSpinner(0.0, 0.0, 7200.0);
        rangePanel.add(startSpinner);
        rangePanel.add(createSmallLabel("sec"));
        rangePanel.add(Box.createHorizontalStrut(20));
        rangePanel.add(createSmallLabel("End:"));
        endSpinner = createSpinner(0.0, 0.0, 7200.0);
        rangePanel.add(endSpinner);
        rangePanel.add(createSmallLabel("sec"));
        rangePanel.add(Box.createHorizontalStrut(20));
        durationLabel = new JLabel("");
        durationLabel.setForeground(App.ACCENT_CYAN);
        durationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rangePanel.add(durationLabel);

        content.add(rangePanel, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 3;
        progressSlider = new JSlider(0, 1000, 0);
        progressSlider.setBackground(App.BG_CARD);
        progressSlider.setEnabled(false);
        content.add(progressSlider, g);

        g.gridy = 3;
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlPanel.setOpaque(false);

        playBtn = App.createButton("\u25b6 Play", App.ACCENT_GREEN, 100, 40);
        pauseBtn = App.createButton("\u23f8 Pause", App.ACCENT_ORANGE, 100, 40);
        stopBtn = App.createButton("\u23f9 Stop", App.ACCENT_RED, 100, 40);
        
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        playBtn.addActionListener(e -> playTest());
        pauseBtn.addActionListener(e -> pauseTest());
        stopBtn.addActionListener(e -> stopTest());

        controlPanel.add(playBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        
        positionLabel = new JLabel("0:00 / 0:00");
        positionLabel.setForeground(App.TEXT_SECONDARY);
        positionLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
        controlPanel.add(positionLabel);

        content.add(controlPanel, g);

        add(content, BorderLayout.CENTER);
    }

    JLabel createSmallLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(App.TEXT_SECONDARY);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    JSpinner createSpinner(double val, double min, double max) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(val, min, max, 0.5));
        sp.setPreferredSize(new Dimension(85, 32));
        sp.setEnabled(false);
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(App.BG_INPUT);
            tf.setForeground(App.TEXT_PRIMARY);
            tf.setCaretColor(App.ACCENT_CYAN);
        }
        return sp;
    }

    void selectSound() {
        FileDialog fd = new FileDialog(App.frame, "Select Sound for " + name, FileDialog.LOAD);
        fd.setVisible(true);

        if (fd.getFile() != null) {
            try {
                File src = new File(fd.getDirectory(), fd.getFile());
                soundFile = new File("data/sounds", id + ".wav");
                App.setStatus("Converting sound...");
                App.convertToWav(src, soundFile);
                totalDuration = getAudioDuration(soundFile);
                
                fileLabel.setText(fd.getFile());
                fileLabel.setForeground(App.ACCENT_GREEN);
                durationLabel.setText("Total: " + formatTime(totalDuration));
                
                startSpinner.setEnabled(true);
                endSpinner.setEnabled(true);
                ((SpinnerNumberModel) startSpinner.getModel()).setMaximum(totalDuration);
                ((SpinnerNumberModel) endSpinner.getModel()).setMaximum(totalDuration);
                endSpinner.setValue(Math.min(30.0, totalDuration));
                
                App.setStatus("System ready");
                App.saveConfig();
                App.msg(name + " imported successfully!\nTotal duration: " + formatTime(totalDuration));
            } catch (Exception e) {
                App.error("Could not import sound: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    void playTest() {
        if (!hasSound()) { App.error("Please select a sound file first!"); return; }
        
        if (isPaused && clip != null) {
            clip.setMicrosecondPosition(pausePosition);
            clip.start();
            isPlaying = true;
            isPaused = false;
            playBtn.setText("\u25b6 Play");
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
                
                double startSec = (double) startSpinner.getValue();
                double endSec = (double) endSpinner.getValue();
                if (endSec <= 0) endSec = totalDuration;
                
                clip.setMicrosecondPosition((long) (startSec * 1000000));
                clip.start();
                
                isPlaying = true;
                isPaused = false;
                pausePosition = 0;
                
                final double finalEndSec = endSec;
                
                SwingUtilities.invokeLater(() -> {
                    playBtn.setEnabled(false);
                    pauseBtn.setEnabled(true);
                    stopBtn.setEnabled(true);
                    progressSlider.setEnabled(true);
                    App.setStatus("\ud83d\udd0a Testing: " + name);
                });
                
                startProgressTimer();

                while (clip != null && clip.isRunning()) {
                    if (clip.getMicrosecondPosition() >= finalEndSec * 1000000) {
                        clip.stop();
                        break;
                    }
                    Thread.sleep(50);
                }

                if (!isPaused) stopTest();

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    App.error("Could not play sound: " + e.getMessage());
                    stopTest();
                });
            }
        });
    }

    void pauseTest() {
        if (clip != null && clip.isRunning()) {
            pausePosition = clip.getMicrosecondPosition();
            clip.stop();
            isPlaying = false;
            isPaused = true;
            
            if (progressTimer != null) {
                progressTimer.cancel();
                progressTimer = null;
            }
            
            playBtn.setText("\u25b6 Resume");
            playBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            App.setStatus("\u23f8 Paused: " + name + " [" + formatTime(pausePosition / 1000000.0) + "]");
        }
    }

    void stopTest() {
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
            playBtn.setText("\u25b6 Play");
            playBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            progressSlider.setValue(0);
            progressSlider.setEnabled(false);
            positionLabel.setText("0:00 / " + formatTime(totalDuration));
            App.setStatus("System ready");
        });
    }

    void playBell() {
        if (!hasSound()) return;
        App.audioPool.submit(() -> {
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(soundFile);
                Clip bellClip = AudioSystem.getClip();
                bellClip.open(stream);
                
                double startSec = (double) startSpinner.getValue();
                double endSec = (double) endSpinner.getValue();
                if (endSec <= 0) endSec = totalDuration;
                
                bellClip.setMicrosecondPosition((long) (startSec * 1000000));
                bellClip.start();
                
                final double finalEndSec = endSec;
                while (bellClip.isRunning()) {
                    if (bellClip.getMicrosecondPosition() >= finalEndSec * 1000000) {
                        bellClip.stop();
                        break;
                    }
                    Thread.sleep(50);
                }
                bellClip.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                        positionLabel.setText(formatTime(pos) + " / " + formatTime(totalDuration));
                    });
                }
            }
        }, 0, 100);
    }

    double getAudioDuration(File file) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();
            double duration = stream.getFrameLength() / format.getFrameRate();
            stream.close();
            return duration;
        } catch (Exception e) {
            return 30.0;
        }
    }

    String formatTime(double seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }

    boolean hasSound() { return soundFile != null && soundFile.exists(); }
    double getStartSec() { return (double) startSpinner.getValue(); }
    double getEndSec() { double end = (double) endSpinner.getValue(); return end > 0 ? end : totalDuration; }

    JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("file", soundFile != null ? soundFile.getAbsolutePath() : "");
        o.put("startSec", getStartSec());
        o.put("endSec", getEndSec());
        o.put("totalDuration", totalDuration);
        return o;
    }

    void fromJSON(JSONObject o) {
        String path = o.optString("file", "");
        if (!path.isEmpty()) {
            soundFile = new File(path);
            if (soundFile.exists()) {
                totalDuration = o.optDouble("totalDuration", getAudioDuration(soundFile));
                fileLabel.setText(soundFile.getName());
                fileLabel.setForeground(App.ACCENT_GREEN);
                durationLabel.setText("Total: " + formatTime(totalDuration));
                startSpinner.setEnabled(true);
                endSpinner.setEnabled(true);
                ((SpinnerNumberModel) startSpinner.getModel()).setMaximum(totalDuration);
                ((SpinnerNumberModel) endSpinner.getModel()).setMaximum(totalDuration);
                startSpinner.setValue(o.optDouble("startSec", 0.0));
                endSpinner.setValue(o.optDouble("endSec", totalDuration));
            }
        }
    }
}