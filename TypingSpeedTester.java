import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Typing Speed Tester - Professional Java Application
 * Measures typing speed in WPM, accuracy, and provides real-time feedback
 * Demonstrates: String comparison, timing, event handling, GUI design
 */
public class TypingSpeedTester extends JFrame {

    private JTextArea sampleTextArea, inputTextArea;
    private JLabel timerLabel, wpmLabel, accuracyLabel, statusLabel;
    private JButton startButton, resetButton;
    private Timer timer;
    private LocalTime startTime;
    private boolean testStarted = false;
    private boolean testCompleted = false;

    // Sample texts for testing
    private static final String[] SAMPLE_TEXTS = {
        "The quick brown fox jumps over the lazy dog. This pangram contains every letter of the alphabet at least once. Typing speed tests often use pangrams because they provide a good mix of common and uncommon letters.",
        "In the world of programming, efficiency and accuracy are equally important. A skilled developer not only writes code that works but also code that is maintainable and performant. Practice makes perfect in any field.",
        "Java is a versatile programming language that supports object-oriented programming, multithreading, and cross-platform development. Its rich API and strong community make it ideal for enterprise applications.",
        "The art of typing quickly and accurately requires practice and proper technique. Touch typing, where fingers rest on the home row keys, allows for maximum speed and reduces strain on hands and wrists.",
        "Success in software development comes from continuous learning and adaptation. New technologies emerge regularly, and staying current with industry trends is essential for career growth and innovation."
    };

    private String currentSampleText = "";
    private int correctChars = 0;
    private int totalChars = 0;

    public TypingSpeedTester() {
        initializeComponents();
        setupLayout();
        setupListeners();
        loadNewText();

        setTitle("Typing Speed Tester - Professional Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        // Sample text area (read-only)
        sampleTextArea = new JTextArea(8, 60);
        sampleTextArea.setEditable(false);
        sampleTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sampleTextArea.setLineWrap(true);
        sampleTextArea.setWrapStyleWord(true);
        sampleTextArea.setBackground(new Color(240, 240, 240));
        sampleTextArea.setBorder(BorderFactory.createTitledBorder("Sample Text"));

        // Input text area
        inputTextArea = new JTextArea(8, 60);
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(BorderFactory.createTitledBorder("Your Typing"));
        inputTextArea.setEnabled(false); // Disabled until test starts

        // Labels
        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(new Color(52, 152, 219));

        wpmLabel = new JLabel("WPM: 0");
        wpmLabel.setFont(new Font("Arial", Font.BOLD, 16));
        wpmLabel.setForeground(new Color(46, 204, 113));

        accuracyLabel = new JLabel("Accuracy: 0%");
        accuracyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        accuracyLabel.setForeground(new Color(230, 126, 34));

        statusLabel = new JLabel("Click 'Start Test' to begin");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(new Color(149, 165, 166));

        // Buttons
        startButton = new JButton("Start Test");
        startButton.setBackground(new Color(46, 204, 113));
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));

        resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(231, 76, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setEnabled(false);

        // Timer for updating display
        timer = new Timer(100, e -> updateTimer());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu testMenu = new JMenu("Test");
        JMenuItem newTextItem = new JMenuItem("New Text");
        newTextItem.addActionListener(e -> loadNewText());
        testMenu.add(newTextItem);
        menuBar.add(testMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Text areas panel
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textPanel.add(new JScrollPane(sampleTextArea));
        textPanel.add(new JScrollPane(inputTextArea));

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statsPanel.add(timerLabel);
        statsPanel.add(wpmLabel);
        statsPanel.add(accuracyLabel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(resetButton);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.add(statusLabel);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statsPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        add(textPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        startButton.addActionListener(e -> startTest());
        resetButton.addActionListener(e -> resetTest());

        // Real-time input monitoring
        inputTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkInput(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkInput(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkInput(); }
        });

        // Keyboard shortcuts
        inputTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    resetTest();
                }
            }
        });
    }

    private void loadNewText() {
        Random random = new Random();
        currentSampleText = SAMPLE_TEXTS[random.nextInt(SAMPLE_TEXTS.length)];
        sampleTextArea.setText(currentSampleText);
        resetTest();
    }

    private void startTest() {
        if (!testStarted) {
            testStarted = true;
            testCompleted = false;
            startTime = LocalTime.now();
            inputTextArea.setEnabled(true);
            inputTextArea.requestFocus();
            inputTextArea.setText("");
            startButton.setEnabled(false);
            resetButton.setEnabled(true);
            statusLabel.setText("Test in progress... Start typing!");
            timer.start();
        }
    }

    private void resetTest() {
        testStarted = false;
        testCompleted = false;
        timer.stop();
        inputTextArea.setText("");
        inputTextArea.setEnabled(false);
        startButton.setEnabled(true);
        resetButton.setEnabled(false);
        timerLabel.setText("Time: 00:00");
        wpmLabel.setText("WPM: 0");
        accuracyLabel.setText("Accuracy: 0%");
        statusLabel.setText("Click 'Start Test' to begin");
        correctChars = 0;
        totalChars = 0;
    }

    private void updateTimer() {
        if (testStarted && !testCompleted) {
            Duration elapsed = Duration.between(startTime, LocalTime.now());
            long seconds = elapsed.getSeconds();
            long minutes = seconds / 60;
            seconds = seconds % 60;
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
        }
    }

    private void checkInput() {
        if (!testStarted || testCompleted) return;

        String userInput = inputTextArea.getText();
        totalChars = userInput.length();

        // Calculate correct characters
        correctChars = 0;
        int minLength = Math.min(userInput.length(), currentSampleText.length());

        for (int i = 0; i < minLength; i++) {
            if (userInput.charAt(i) == currentSampleText.charAt(i)) {
                correctChars++;
            }
        }

        // Update accuracy
        double accuracy = totalChars > 0 ? (double) correctChars / totalChars * 100 : 0;
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", accuracy));

        // Check if test is complete
        if (userInput.equals(currentSampleText)) {
            completeTest();
        }

        // Update WPM in real-time
        updateWPM();
    }

    private void updateWPM() {
        if (!testStarted || startTime == null) return;

        Duration elapsed = Duration.between(startTime, LocalTime.now());
        double minutes = elapsed.toMillis() / 60000.0;

        if (minutes > 0) {
            // WPM = (characters typed / 5) / time in minutes
            // Standard: 1 word = 5 characters
            double wpm = (correctChars / 5.0) / minutes;
            wpmLabel.setText(String.format("WPM: %.1f", wpm));
        }
    }

    private void completeTest() {
        testCompleted = true;
        timer.stop();
        inputTextArea.setEnabled(false);
        startButton.setEnabled(true);
        resetButton.setEnabled(true);

        Duration elapsed = Duration.between(startTime, LocalTime.now());
        double minutes = elapsed.toMillis() / 60000.0;
        double wpm = (correctChars / 5.0) / minutes;
        double accuracy = (double) correctChars / totalChars * 100;

        statusLabel.setText("Test completed!");
        wpmLabel.setText(String.format("WPM: %.1f", wpm));
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", accuracy));

        // Show results dialog
        String message = String.format(
            "Test Results:\n\n" +
            "Words Per Minute: %.1f\n" +
            "Accuracy: %.1f%%\n" +
            "Time Taken: %d seconds\n" +
            "Correct Characters: %d/%d\n\n" +
            "Great job!",
            wpm, accuracy, elapsed.getSeconds(), correctChars, totalChars
        );

        JOptionPane.showMessageDialog(this, message, "Test Complete!",
                                    JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String aboutText =
            "Typing Speed Tester v1.0\n\n" +
            "A professional typing speed measurement application\n" +
            "Features: Real-time WPM calculation, accuracy tracking,\n" +
            "multiple sample texts, and comprehensive statistics.\n\n" +
            "Built with Java Swing - Demonstrates string comparison\n" +
            "and timing concepts.\n\n" +
            "Keyboard shortcuts:\n" +
            "F5 - Reset test";

        JOptionPane.showMessageDialog(this, aboutText, "About Typing Speed Tester",
                                    JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                // Use default look and feel
            }
            new TypingSpeedTester();
        });
    }
}