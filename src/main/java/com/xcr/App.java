package com.xcr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.json.JSONArray;
import org.json.JSONObject;

public class App {

    private static final String APP_NAME = "XCR School Bell";
    private static final String VERSION = "6.0";
    private static final String DATA_DIR = "data";
    private static final String SOUNDS_DIR = "data/sounds";
    private static final String BACKUPS_DIR = "data/backups";
    private static final String CONFIG_FILE = "data/config.json";
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    static final Color BG_MAIN = new Color(8, 8, 14);
    static final Color BG_PANEL = new Color(14, 14, 22);
    static final Color BG_CARD = new Color(22, 22, 34);
    static final Color BG_CARD_LIGHT = new Color(30, 30, 44);
    static final Color BG_INPUT = new Color(18, 18, 28);
    static final Color BG_INPUT_FOCUS = new Color(28, 35, 50);
    static final Color ACCENT_BLUE = new Color(59, 130, 246);
    static final Color ACCENT_GREEN = new Color(34, 197, 94);
    static final Color ACCENT_RED = new Color(239, 68, 68);
    static final Color ACCENT_ORANGE = new Color(249, 115, 22);
    static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    static final Color ACCENT_CYAN = new Color(6, 182, 212);
    static final Color ACCENT_PINK = new Color(236, 72, 153);
    static final Color ACCENT_YELLOW = new Color(250, 204, 21);
    static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    static final Color TEXT_MUTED = new Color(100, 116, 139);
    static final Color BORDER_DARK = new Color(40, 40, 60);
    static final Color BORDER_LIGHT = new Color(60, 70, 90);

    static JFrame frame;
    
    static JTabbedPane tabs;
    static List<DayPanel> dayPanels = new ArrayList<>();
    static JLabel clockLabel, statusLabel, dateLabel;
    static JCheckBox bellActiveCheck;
    static SoundSetting studentSound, teacherSound, breakSound;
    static JPanel soundpadGrid;
    static List<SoundpadPlayer> soundpadPlayers = new ArrayList<>();
    static JButton micBtn;
    static JSlider micVolume;
    static JLabel micStatusLabel;
    static AtomicBoolean micActive = new AtomicBoolean(false);
    static TargetDataLine micLine;
    static SourceDataLine speakerLine;
    static DefaultListModel<BackupEntry> backupModel;
    static Timer bellTimer, clockTimer;
    static ExecutorService audioPool = Executors.newFixedThreadPool(8);
    static List<LessonInfo> copiedDay = null;

    public static void main(String[] args) {
        initDirs();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(App::createUI);
    }

    private static void createUI() {
        frame = new JFrame(APP_NAME + " v" + VERSION);

        ImageIcon icon = new ImageIcon(App.class.getResource("/icon.png"));
        frame.setIconImage(icon.getImage());
        
        frame.setSize(1550, 950);
        frame.setMinimumSize(new Dimension(1300, 800));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_MAIN);
        frame.setLayout(new BorderLayout(0, 0));

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveConfig();
                if (confirm("Do you want to close the program?\n\nYour settings have been saved automatically.")) {
                    shutdown();
                    System.exit(0);
                }
            }
        });
        

        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveConfig()));

        frame.add(createHeader(), BorderLayout.NORTH);

        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_PANEL);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tabs.setOpaque(true);

        tabs.addTab("  \ud83d\udcc5 Schedule  ", createSchedulePanel());
        tabs.addTab("  \ud83d\udd0a Bell Sounds  ", createSoundPanel());
        tabs.addTab("  \ud83c\udfb9 Soundpad  ", createSoundpadPanel());
        tabs.addTab("  \ud83c\udfa4 Live Announcement  ", createAnnouncementPanel());
        tabs.addTab("  \ud83d\udcbe Backup  ", createBackupPanel());
        tabs.addTab("  \u2699\ufe0f System  ", createSystemPanel());

        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setBackgroundAt(i, BG_CARD);
        }

        frame.add(tabs, BorderLayout.CENTER);
        frame.add(createFooter(), BorderLayout.SOUTH);

        loadConfig();
        startTimers();
        frame.setVisible(true);
    }
    

    private static JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT_BLUE),
            new EmptyBorder(18, 30, 18, 30)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        left.setOpaque(false);
        
        JLabel logo = new JLabel("\ud83d\udd14");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        left.add(logo);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel title = new JLabel(APP_NAME);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT_PRIMARY);
        titlePanel.add(title);

        
        left.add(titlePanel);
        p.add(left, BorderLayout.WEST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 44));
        clockLabel.setForeground(ACCENT_GREEN);
        clockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(clockLabel);
        
        dateLabel = new JLabel("");
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(TEXT_SECONDARY);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(dateLabel);
        
        p.add(centerPanel, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        right.setOpaque(false);
        
        bellActiveCheck = new JCheckBox("BELL SYSTEM ACTIVE");
        bellActiveCheck.setSelected(true);
        bellActiveCheck.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bellActiveCheck.setForeground(ACCENT_GREEN);
        bellActiveCheck.setOpaque(false);
        bellActiveCheck.setFocusPainted(false);
        bellActiveCheck.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellActiveCheck.addActionListener(e -> {
            bellActiveCheck.setForeground(bellActiveCheck.isSelected() ? ACCENT_GREEN : ACCENT_RED);
            setStatus(bellActiveCheck.isSelected() ? "Bell system active" : "Bell system disabled");
        });
        right.add(bellActiveCheck);
        
        right.add(Box.createHorizontalStrut(20));
        
        JButton saveBtn = createButton("\ud83d\udcbe SAVE", ACCENT_BLUE, 140, 48);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.addActionListener(e -> { saveConfig(); msg("All settings saved!"); });
        right.add(saveBtn);
        
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private static JPanel createSchedulePanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setBackground(BG_CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1, true),
            new EmptyBorder(12, 18, 12, 18)
        ));

        toolbar.add(createButton("\ud83d\udccb Copy", ACCENT_BLUE, 120, 38));
        toolbar.add(createButton("\ud83d\udccc Paste", ACCENT_GREEN, 120, 38));
        toolbar.add(createSeparator());
        toolbar.add(createButton("\u2795 Add Lesson", ACCENT_PURPLE, 125, 38));
        toolbar.add(createButton("\u2796 Remove Lesson", ACCENT_RED, 130, 38));
        toolbar.add(createButton("\ud83d\uddd1\ufe0f Clear", ACCENT_ORANGE, 110, 38));
        toolbar.add(createSeparator());
        toolbar.add(createButton("\ud83d\udcd1 Copy to All", ACCENT_CYAN, 145, 38));

        Component[] btns = toolbar.getComponents();
        ((JButton)btns[0]).addActionListener(e -> copyDay());
        ((JButton)btns[1]).addActionListener(e -> pasteDay());
        ((JButton)btns[3]).addActionListener(e -> addLesson());
        ((JButton)btns[4]).addActionListener(e -> removeLesson());
        ((JButton)btns[5]).addActionListener(e -> clearDay());
        ((JButton)btns[7]).addActionListener(e -> copyToAll());

        p.add(toolbar, BorderLayout.NORTH);

        JTabbedPane dayTabs = new JTabbedPane(JTabbedPane.TOP);
        dayTabs.setBackground(BG_CARD);
        dayTabs.setForeground(TEXT_PRIMARY);
        dayTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dayTabs.setOpaque(true);

        for (int i = 0; i < 7; i++) {
            DayPanel dp = new DayPanel(DAYS[i], i);
            dayPanels.add(dp);
            dayTabs.addTab("  " + DAYS[i] + "  ", dp);
            if (i >= 5) dayTabs.setForegroundAt(i, ACCENT_ORANGE);
        }

        p.add(dayTabs, BorderLayout.CENTER);
        return p;
    }

    private static JPanel createSoundPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel header = new JLabel("\ud83d\udd0a Bell Sounds Configuration");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(TEXT_PRIMARY);
        headerPanel.add(header, BorderLayout.WEST);
        
        JButton stopAllBtn = createButton("\u23f9 Stop All", ACCENT_RED, 160, 40);
        stopAllBtn.addActionListener(e -> stopAllBellTests());
        headerPanel.add(stopAllBtn, BorderLayout.EAST);
        
        p.add(headerPanel, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG_MAIN);
        list.setOpaque(true);

        studentSound = new SoundSetting("Student Bell", "student", ACCENT_BLUE, "\ud83c\udf93");
        teacherSound = new SoundSetting("Teacher Bell", "teacher", ACCENT_GREEN, "\ud83d\udc68\u200d\ud83c\udfeb");
        breakSound = new SoundSetting("Break Bell", "break", ACCENT_ORANGE, "\u2615");

        list.add(studentSound);
        list.add(Box.createVerticalStrut(20));
        list.add(teacherSound);
        list.add(Box.createVerticalStrut(20));
        list.add(breakSound);
        list.add(Box.createVerticalGlue());

        JScrollPane scroll = createScrollPane(list);
        p.add(scroll, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoPanel.setBackground(new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), 30));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 1, true),
            new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel infoLabel = new JLabel("\ud83d\udca1 All audio formats supported (MP3, WAV, OGG, FLAC, M4A, AAC etc.) - Pause/Resume feature active");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLabel.setForeground(TEXT_PRIMARY);
        infoPanel.add(infoLabel);
        p.add(infoPanel, BorderLayout.SOUTH);

        return p;
    }

    private static JPanel createSoundpadPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 12));
        top.setBackground(BG_CARD);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1, true),
            new EmptyBorder(18, 25, 18, 25)
        ));

        JButton stopAll = createButton("\u23f9 STOP ALL SOUNDS", ACCENT_RED, 300, 60);
        stopAll.setFont(new Font("Segoe UI", Font.BOLD, 17));
        stopAll.addActionListener(e -> stopAllSounds());
        top.add(stopAll);

        JButton addSound = createButton("\u2795 ADD NEW SOUND", ACCENT_GREEN, 220, 60);
        addSound.setFont(new Font("Segoe UI", Font.BOLD, 15));
        addSound.addActionListener(e -> addSoundpadSound());
        top.add(addSound);

        p.add(top, BorderLayout.NORTH);

        soundpadGrid = new JPanel(new GridLayout(0, 3, 22, 22));
        soundpadGrid.setBackground(BG_MAIN);
        soundpadGrid.setOpaque(true);
        soundpadGrid.setBorder(new EmptyBorder(25, 15, 25, 15));

        JScrollPane scroll = createScrollPane(soundpadGrid);
        p.add(scroll, BorderLayout.CENTER);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.CENTER));
        info.setBackground(BG_CARD);
        info.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1, true),
            new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel infoLabel = new JLabel("\ud83d\udca1 Sounds resume exactly where they were stopped - Pause/Resume feature active");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLabel.setForeground(TEXT_SECONDARY);
        info.add(infoLabel);
        p.add(info, BorderLayout.SOUTH);

        return p;
    }

    static void addSoundpadSound() {
        FileDialog fd = new FileDialog(frame, "Select Sound File", FileDialog.LOAD);
        fd.setVisible(true);

        if (fd.getFile() != null) {
            File file = new File(fd.getDirectory(), fd.getFile());
            String name = JOptionPane.showInputDialog(frame, "Enter a name for this sound:", 
                file.getName().replaceFirst("[.][^.]+$", ""));
            
            if (name != null && !name.isEmpty()) {
                String id = "snd_" + System.currentTimeMillis();
                try {
                    File dest = new File(SOUNDS_DIR, id + ".wav");
                    convertToWav(file, dest);
                    
                    Color[] colors = {ACCENT_BLUE, ACCENT_GREEN, ACCENT_PURPLE, ACCENT_CYAN, ACCENT_PINK, ACCENT_ORANGE, ACCENT_YELLOW};
                    Color color = colors[new Random().nextInt(colors.length)];
                    
                    SoundpadPlayer player = new SoundpadPlayer(name, id, color, dest);
                    soundpadPlayers.add(player);
                    soundpadGrid.add(player);
                    soundpadGrid.revalidate();
                    soundpadGrid.repaint();
                    
                    saveConfig();
                    msg("'" + name + "' added successfully!");
                } catch (Exception e) {
                    error("Error adding sound: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private static JPanel createAnnouncementPanel() {
        JPanel p = new JPanel(new BorderLayout(25, 25));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel header = new JLabel("\ud83c\udfa4 Live Announcement System", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(TEXT_PRIMARY);
        p.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_MAIN);
        center.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        micBtn = new JButton("\ud83c\udfa4");
        micBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        micBtn.setPreferredSize(new Dimension(200, 200));
        micBtn.setBackground(BG_CARD);
        micBtn.setForeground(TEXT_PRIMARY);
        micBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 4, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        micBtn.setFocusPainted(false);
        micBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        micBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) startMic();
            }
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) stopMic();
            }
        });

        center.add(micBtn, gbc);
        
        gbc.gridy = 1;
        micStatusLabel = new JLabel("Hold the microphone button to make an announcement");
        micStatusLabel.setForeground(TEXT_SECONDARY);
        micStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        center.add(micStatusLabel, gbc);
        
        p.add(center, BorderLayout.CENTER);

        JPanel settings = new JPanel(new GridBagLayout());
        settings.setBackground(BG_CARD);
        settings.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1, true),
            BorderFactory.createTitledBorder(
                new EmptyBorder(15, 20, 15, 20),
                "  Microphone Settings  ",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 15),
                TEXT_PRIMARY
            )
        ));
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 20, 12, 20);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        JLabel volLbl = new JLabel("Volume Level:");
        volLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        volLbl.setForeground(TEXT_PRIMARY);
        settings.add(volLbl, g);

        g.gridx = 1; g.weightx = 1;
        micVolume = new JSlider(0, 200, 100);
        micVolume.setBackground(BG_CARD);
        micVolume.setForeground(TEXT_PRIMARY);
        micVolume.setMajorTickSpacing(50);
        micVolume.setPaintTicks(true);
        micVolume.setPaintLabels(true);
        settings.add(micVolume, g);

        g.gridx = 2; g.weightx = 0;
        JLabel volLabel = new JLabel("100%");
        volLabel.setForeground(ACCENT_BLUE);
        volLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        micVolume.addChangeListener(e -> volLabel.setText(micVolume.getValue() + "%"));
        settings.add(volLabel, g);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 3;
        JButton testBtn = createButton("\ud83d\udd0a Test for 5 Seconds", ACCENT_BLUE, 220, 45);
        testBtn.addActionListener(e -> testMic());
        settings.add(testBtn, g);

        p.add(settings, BorderLayout.SOUTH);

        return p;
    }

    private static JPanel createBackupPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("\ud83d\udcbe Backup Management");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        p.add(header, BorderLayout.NORTH);

        backupModel = new DefaultListModel<>();
        JList<BackupEntry> list = new JList<>(backupModel);
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_PRIMARY);
        list.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionBackground(ACCENT_BLUE);
        list.setCellRenderer(new BackupRenderer());
        list.setFixedCellHeight(75);

        JScrollPane scroll = createScrollPane(list);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1, true),
            BorderFactory.createTitledBorder(
                new EmptyBorder(10, 10, 10, 10),
                "  Available Backups  ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                TEXT_PRIMARY
            )
        ));
        p.add(scroll, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 15));
        btns.setBackground(BG_CARD);
        btns.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1, true),
            new EmptyBorder(12, 18, 12, 18)
        ));

        JButton create = createButton("\ud83d\udcbe Create Backup", ACCENT_GREEN, 150, 42);
        JButton restore = createButton("\ud83d\udd04 Restore", ACCENT_BLUE, 120, 42);
        JButton delete = createButton("\ud83d\uddd1\ufe0f Delete", ACCENT_RED, 100, 42);
        JButton export = createButton("\ud83d\udce4 Export", ACCENT_PURPLE, 120, 42);
        JButton importBtn = createButton("\ud83d\udce5 Import", ACCENT_ORANGE, 120, 42);
        JButton refresh = createButton("\ud83d\udd03 Refresh", new Color(100, 116, 139), 115, 42);

        create.addActionListener(e -> createBackup());
        restore.addActionListener(e -> restoreBackup(list.getSelectedValue()));
        delete.addActionListener(e -> deleteBackup(list.getSelectedValue()));
        export.addActionListener(e -> exportBackup(list.getSelectedValue()));
        importBtn.addActionListener(e -> importBackup());
        refresh.addActionListener(e -> loadBackups());

        btns.add(create);
        btns.add(restore);
        btns.add(delete);
        btns.add(Box.createHorizontalStrut(35));
        btns.add(export);
        btns.add(importBtn);
        btns.add(refresh);

        p.add(btns, BorderLayout.SOUTH);

        loadBackups();
        return p;
    }

    private static JPanel createSystemPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("\u2699\ufe0f System Information");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        p.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_MAIN);
        content.setOpaque(true);

        JPanel sysCard = createInfoCard("Application Information");

        addInfoRow(sysCard, "Java", System.getProperty("java.version"));
        addInfoRow(sysCard, "Operating System", System.getProperty("os.name"));
        addInfoRow(sysCard, "User", System.getProperty("user.name"));
        content.add(sysCard);
        content.add(Box.createVerticalStrut(20));

        JPanel storageCard = createInfoCard("Storage Status");
        addInfoRow(storageCard, "Data Folder", new File(DATA_DIR).getAbsolutePath());
        addInfoRow(storageCard, "Sound Files", countFiles(SOUNDS_DIR) + " files");
        addInfoRow(storageCard, "Backups", countFiles(BACKUPS_DIR) + " backups");
        content.add(storageCard);
        content.add(Box.createVerticalStrut(20));

        JPanel featuresCard = createInfoCard("Features");
        addInfoRow(featuresCard, "Auto Save", "Settings are saved automatically when program closes");
        addInfoRow(featuresCard, "Audio Formats", "All formats supported (FFmpeg)");
  
        content.add(featuresCard);
        content.add(Box.createVerticalStrut(20));

        JPanel actionsCard = createInfoCard("Quick Actions");
        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        actBtns.setBackground(BG_CARD);
        actBtns.setOpaque(true);

        JButton openFolder = createButton("\ud83d\udcc2 Open Folder", ACCENT_BLUE, 145, 42);
        JButton reset = createButton("\u26a0\ufe0f Factory Reset", ACCENT_RED, 160, 42);

        openFolder.addActionListener(e -> {
            try { Desktop.getDesktop().open(new File(DATA_DIR)); } 
            catch (Exception ex) { error("Could not open folder: " + ex.getMessage()); }
        });
        reset.addActionListener(e -> factoryReset());

        actBtns.add(openFolder);
        actBtns.add(reset);
        actionsCard.add(actBtns);
        content.add(actionsCard);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = createScrollPane(content);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private static JPanel createFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_DARK),
            new EmptyBorder(12, 25, 12, 25)
        ));

        statusLabel = new JLabel("\u25cf System ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(ACCENT_GREEN);
        p.add(statusLabel, BorderLayout.WEST);

        JLabel copy = new JLabel("https://github.com/X-croot");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copy.setForeground(TEXT_MUTED);
        p.add(copy, BorderLayout.EAST);

        return p;
    }

    static void convertToWav(File input, File output) throws Exception {
        String ffmpegPath = findFFmpeg();
        if (ffmpegPath != null) {
            ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath, "-y", "-i", input.getAbsolutePath(),
                "-acodec", "pcm_s16le", "-ar", "44100", "-ac", "2",
                output.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (reader.readLine() != null) {}
            int exitCode = process.waitFor();
            if (exitCode == 0 && output.exists() && output.length() > 0) return;
        }
        
        try {
            AudioInputStream sourceStream = AudioSystem.getAudioInputStream(input);
            AudioFormat sourceFormat = sourceStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            
            AudioInputStream convertedStream;
            if (AudioSystem.isConversionSupported(targetFormat, sourceFormat)) {
                convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
            } else {
                AudioFormat intermediateFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);
                
                if (AudioSystem.isConversionSupported(intermediateFormat, sourceFormat)) {
                    AudioInputStream intermediate = AudioSystem.getAudioInputStream(intermediateFormat, sourceStream);
                    if (AudioSystem.isConversionSupported(targetFormat, intermediateFormat)) {
                        convertedStream = AudioSystem.getAudioInputStream(targetFormat, intermediate);
                    } else {
                        convertedStream = intermediate;
                    }
                } else {
                    convertedStream = sourceStream;
                }
            }
            
            AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, output);
            convertedStream.close();
            sourceStream.close();
        } catch (UnsupportedAudioFileException e) {
            Files.copy(input.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String findFFmpeg() {
        String[] paths = {"ffmpeg", "/usr/bin/ffmpeg", "/usr/local/bin/ffmpeg",
            "C:\\ffmpeg\\bin\\ffmpeg.exe", System.getProperty("user.home") + "\\ffmpeg\\bin\\ffmpeg.exe"};
        for (String path : paths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(path, "-version");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                if (p.waitFor() == 0) return path;
            } catch (Exception ignored) {}
        }
        return null;
    }

    static File findSoundFile(String id) {
        File dir = new File(SOUNDS_DIR);
        if (!dir.exists()) return null;
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File f : files) {
            if (f.getName().startsWith(id + ".") || f.getName().equals(id)) return f;
        }
        return null;
    }

    static void stopAllSounds() {
        for (SoundpadPlayer sp : soundpadPlayers) sp.stopSound();
        setStatus("All sounds stopped");
    }

    static void stopAllBellTests() {
        if (studentSound != null) studentSound.stopTest();
        if (teacherSound != null) teacherSound.stopTest();
        if (breakSound != null) breakSound.stopTest();
        setStatus("All bell tests stopped");
    }

    static void startMic() {
        if (micActive.get()) return;
        try {
            AudioFormat fmt = new AudioFormat(44100, 16, 1, true, false);
            micLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, fmt));
            speakerLine = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));

            micLine.open(fmt);
            speakerLine.open(fmt);
            micLine.start();
            speakerLine.start();

            micActive.set(true);
            micBtn.setBackground(ACCENT_RED);
            micBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_RED, 5, true),
                new EmptyBorder(20, 20, 20, 20)
            ));
            micBtn.setText("\ud83d\udd34");
            micStatusLabel.setText("\ud83d\udd34 ANNOUNCING...");
            micStatusLabel.setForeground(ACCENT_RED);

            new Thread(() -> {
                byte[] buf = new byte[4096];
                float vol = micVolume.getValue() / 100f;
                while (micActive.get()) {
                    int n = micLine.read(buf, 0, buf.length);
                    if (n > 0) {
                        if (vol != 1f) {
                            for (int i = 0; i < n - 1; i += 2) {
                                short s = (short) ((buf[i + 1] << 8) | (buf[i] & 0xFF));
                                s = (short) Math.max(-32768, Math.min(32767, s * vol));
                                buf[i] = (byte) s;
                                buf[i + 1] = (byte) (s >> 8);
                            }
                        }
                        speakerLine.write(buf, 0, n);
                    }
                }
            }).start();

            setStatus("\ud83c\udfa4 Announcing...");

        } catch (Exception e) {
            error("Microphone error: " + e.getMessage());
            stopMic();
        }
    }

    static void stopMic() {
        micActive.set(false);
        if (micLine != null) { micLine.stop(); micLine.close(); }
        if (speakerLine != null) { speakerLine.stop(); speakerLine.close(); }
        micBtn.setBackground(BG_CARD);
        micBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 4, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        micBtn.setText("\ud83c\udfa4");
        micStatusLabel.setText("Hold the microphone button to make an announcement");
        micStatusLabel.setForeground(TEXT_SECONDARY);
        setStatus("System ready");
    }

    private static void testMic() {
        new Thread(() -> {
            startMic();
            try { Thread.sleep(5000); } catch (Exception ignored) {}
            stopMic();
        }).start();
    }

    private static void startTimers() {
        clockTimer = new Timer();
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    clockLabel.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    dateLabel.setText(new SimpleDateFormat("MMMM dd, yyyy, EEEE", Locale.ENGLISH).format(new Date()));
                });
            }
        }, 0, 1000);

        bellTimer = new Timer();
        bellTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!bellActiveCheck.isSelected()) return;

                Calendar c = Calendar.getInstance();
                int dow = c.get(Calendar.DAY_OF_WEEK);
                int idx = (dow == Calendar.SUNDAY) ? 6 : dow - 2;
                if (idx < 0 || idx >= dayPanels.size()) return;

                String now = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

                for (LessonRow r : dayPanels.get(idx).rows) {
                    if (r.studentField.getText().equals(now) && studentSound != null && studentSound.hasSound()) {
                        SwingUtilities.invokeLater(() -> studentSound.playBell());
                    }
                    if (r.teacherField.getText().equals(now) && teacherSound != null && teacherSound.hasSound()) {
                        SwingUtilities.invokeLater(() -> teacherSound.playBell());
                    }
                    if (r.breakField.getText().equals(now) && breakSound != null && breakSound.hasSound()) {
                        SwingUtilities.invokeLater(() -> breakSound.playBell());
                    }
                }
            }
        }, 0, 30000);
    }

    private static int currentDayIdx() {
        Component sel = tabs.getSelectedComponent();
        if (sel != null) {
            for (Component c : ((JPanel) sel).getComponents()) {
                if (c instanceof JTabbedPane) return ((JTabbedPane) c).getSelectedIndex();
            }
        }
        return -1;
    }

    private static void copyDay() {
        int i = currentDayIdx();
        if (i >= 0) {
            copiedDay = dayPanels.get(i).getData();
            msg(DAYS[i] + " copied! (" + copiedDay.size() + " lessons)");
        }
    }

    private static void pasteDay() {
        if (copiedDay == null) { error("Copy a day first!"); return; }
        int i = currentDayIdx();
        if (i >= 0) {
            dayPanels.get(i).setData(copiedDay);
            msg("Pasted to " + DAYS[i] + "!");
        }
    }

    private static void copyToAll() {
        int i = currentDayIdx();
        if (i < 0) return;
        if (confirm("Copy " + DAYS[i] + " to all days?")) {
            List<LessonInfo> data = dayPanels.get(i).getData();
            for (int j = 0; j < dayPanels.size(); j++) {
                if (j != i) dayPanels.get(j).setData(data);
            }
            msg("Copied to all days!");
        }
    }

    private static void addLesson() {
        int i = currentDayIdx();
        if (i >= 0) dayPanels.get(i).addRow();
    }

    private static void removeLesson() {
        int i = currentDayIdx();
        if (i >= 0) dayPanels.get(i).removeRow();
    }

    private static void clearDay() {
        int i = currentDayIdx();
        if (i >= 0 && confirm("Clear " + DAYS[i] + "?")) dayPanels.get(i).clearAll();
    }

    static void initDirs() {
        new File(DATA_DIR).mkdirs();
        new File(SOUNDS_DIR).mkdirs();
        new File(BACKUPS_DIR).mkdirs();
    }

    static void saveConfig() {
        try {
            JSONObject cfg = new JSONObject();
            cfg.put("bellActive", bellActiveCheck.isSelected());
            cfg.put("micVol", micVolume.getValue());

            JSONArray schedule = new JSONArray();
            for (int i = 0; i < dayPanels.size(); i++) {
                JSONObject d = new JSONObject();
                d.put("day", DAYS[i]);
                d.put("lessons", dayPanels.get(i).toJSON());
                schedule.put(d);
            }
            cfg.put("schedule", schedule);

            JSONObject sounds = new JSONObject();
            if (studentSound != null) sounds.put("student", studentSound.toJSON());
            if (teacherSound != null) sounds.put("teacher", teacherSound.toJSON());
            if (breakSound != null) sounds.put("break", breakSound.toJSON());
            cfg.put("sounds", sounds);

            JSONArray soundpad = new JSONArray();
            for (SoundpadPlayer sp : soundpadPlayers) soundpad.put(sp.toJSON());
            cfg.put("soundpad", soundpad);

            try (FileWriter w = new FileWriter(CONFIG_FILE)) {
                w.write(cfg.toString(2));
            }
            setStatus("Settings saved");
        } catch (Exception e) {
            error("Save error: " + e.getMessage());
        }
    }

    static void loadConfig() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
            }

            JSONObject cfg = new JSONObject(sb.toString());

            bellActiveCheck.setSelected(cfg.optBoolean("bellActive", true));
            bellActiveCheck.setForeground(bellActiveCheck.isSelected() ? ACCENT_GREEN : ACCENT_RED);
            micVolume.setValue(cfg.optInt("micVol", 100));

            if (cfg.has("schedule")) {
                JSONArray schedule = cfg.getJSONArray("schedule");
                for (int i = 0; i < Math.min(schedule.length(), dayPanels.size()); i++) {
                    JSONObject d = schedule.getJSONObject(i);
                    if (d.has("lessons")) dayPanels.get(i).fromJSON(d.getJSONArray("lessons"));
                }
            }

            if (cfg.has("sounds")) {
                JSONObject sounds = cfg.getJSONObject("sounds");
                if (sounds.has("student") && studentSound != null) studentSound.fromJSON(sounds.getJSONObject("student"));
                if (sounds.has("teacher") && teacherSound != null) teacherSound.fromJSON(sounds.getJSONObject("teacher"));
                if (sounds.has("break") && breakSound != null) breakSound.fromJSON(sounds.getJSONObject("break"));
            }

            if (cfg.has("soundpad")) {
                JSONArray soundpad = cfg.getJSONArray("soundpad");
                for (int i = 0; i < soundpad.length(); i++) {
                    JSONObject o = soundpad.getJSONObject(i);
                    String name = o.getString("name");
                    String id = o.getString("id");
                    Color color = new Color(o.getInt("color"));
                    File soundFile = findSoundFile(id);
                    if (soundFile != null && soundFile.exists()) {
                        SoundpadPlayer player = new SoundpadPlayer(name, id, color, soundFile);
                        player.setRange(o.optDouble("startSec", 0), o.optDouble("endSec", 0));
                        soundpadPlayers.add(player);
                        soundpadGrid.add(player);
                    }
                }
                soundpadGrid.revalidate();
            }

            setStatus("Settings loaded");
        } catch (Exception e) {
            error("Load error: " + e.getMessage());
        }
    }

    private static void loadBackups() {
        backupModel.clear();
        File dir = new File(BACKUPS_DIR);
        if (dir.exists()) {
            File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
            if (files != null) {
                Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                for (File f : files) backupModel.addElement(new BackupEntry(f.getName(), f.lastModified(), f.length()));
            }
        }
    }

    private static void createBackup() {
        saveConfig();
        String name = "backup_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json";
        try {
            Files.copy(new File(CONFIG_FILE).toPath(), new File(BACKUPS_DIR, name).toPath());
            loadBackups();
            msg("Backup created: " + name);
        } catch (Exception e) {
            error("Backup creation error: " + e.getMessage());
        }
    }

    private static void restoreBackup(BackupEntry entry) {
        if (entry == null) { error("Please select a backup!"); return; }
        if (confirm("Restore '" + entry.name + "'?")) {
            try {
                Files.copy(new File(BACKUPS_DIR, entry.name).toPath(), new File(CONFIG_FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
                loadConfig();
                msg("Backup restored!");
            } catch (Exception e) {
                error("Restore error: " + e.getMessage());
            }
        }
    }

    private static void deleteBackup(BackupEntry entry) {
        if (entry == null) { error("Please select a backup!"); return; }
        if (confirm("Delete '" + entry.name + "'?")) {
            new File(BACKUPS_DIR, entry.name).delete();
            loadBackups();
        }
    }

    private static void exportBackup(BackupEntry entry) {
        FileDialog fd = new FileDialog(frame, "Export Backup", FileDialog.SAVE);
        fd.setFile(entry != null ? entry.name : "backup.json");
        fd.setVisible(true);
        if (fd.getFile() != null) {
            try {
                File src = entry != null ? new File(BACKUPS_DIR, entry.name) : new File(CONFIG_FILE);
                Files.copy(src.toPath(), new File(fd.getDirectory(), fd.getFile()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                msg("Exported successfully!");
            } catch (Exception e) {
                error("Export error: " + e.getMessage());
            }
        }
    }

    private static void importBackup() {
        FileDialog fd = new FileDialog(frame, "Import Backup", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() != null) {
            try {
                String name = "import_" + System.currentTimeMillis() + ".json";
                Files.copy(new File(fd.getDirectory(), fd.getFile()).toPath(), new File(BACKUPS_DIR, name).toPath());
                loadBackups();
                msg("Imported: " + name);
            } catch (Exception e) {
                error("Import error: " + e.getMessage());
            }
        }
    }

    private static void factoryReset() {
        if (confirm("\u26a0\ufe0f ALL SETTINGS WILL BE DELETED!\n\nDo you want to continue?")) {
            if (confirm("This action cannot be undone! Are you sure?")) {
                deleteDir(new File(DATA_DIR));
                initDirs();
                msg("Factory reset complete. Program will restart.");
                System.exit(0);
            }
        }
    }

    private static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) deleteDir(f);
        }
        dir.delete();
    }

    private static void shutdown() {
        stopAllSounds();
        stopMic();
        if (clockTimer != null) clockTimer.cancel();
        if (bellTimer != null) bellTimer.cancel();
        audioPool.shutdownNow();
    }

    private static int countFiles(String dir) {
        File d = new File(dir);
        if (!d.exists()) return 0;
        File[] files = d.listFiles();
        return files != null ? files.length : 0;
    }

    static void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText("\u25cf " + msg);
            statusLabel.setForeground(msg.contains("error") || msg.contains("Error") ? ACCENT_RED : ACCENT_GREEN);
        }
    }

    static JButton createButton(String text, Color color, int w, int h) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(w, h));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(color); }
        });
        return b;
    }

    private static Component createSeparator() {
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(2, 30));
        sep.setBackground(BORDER_LIGHT);
        return sep;
    }

    static JScrollPane createScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_MAIN);
        sp.getViewport().setBackground(BG_MAIN);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    private static JPanel createInfoCard(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setOpaque(true);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1, true),
            BorderFactory.createTitledBorder(
                new EmptyBorder(12, 18, 12, 18),
                "  " + title + "  ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 15),
                TEXT_PRIMARY
            )
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        return p;
    }

    private static void addInfoRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        row.setBackground(BG_CARD);
        row.setOpaque(true);
        
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(160, 22));
        row.add(l);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        v.setForeground(TEXT_PRIMARY);
        row.add(v);
        
        panel.add(row);
    }

    static boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(frame, msg, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    static void msg(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    static void error(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}