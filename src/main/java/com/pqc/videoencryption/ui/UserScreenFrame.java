package com.pqc.videoencryption.ui;

import com.pqc.videoencryption.crypto.VideoEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserScreenFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(UserScreenFrame.class);
    
    private final String username;
    private final String password;
    private JTextArea logArea;
    private Path selectedVideoPath;
    
    public UserScreenFrame(String username, String password) {
        super("Post-Quantum Video Encryption - User Screen");
        this.username = username;
        this.password = password;
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(900, 600);
        
        JPanel titlePanel = createTitlePanel();
        JPanel buttonPanel = createButtonPanel();
        JPanel logPanel = createLogPanel();
        
        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(49, 120, 206));
        panel.setPreferredSize(new Dimension(900, 50));
        panel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Welcome, " + username, JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(title, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton selectButton = new JButton("Select Video File");
        JButton encryptButton = new JButton("Encrypt Video (PQC)");
        JButton decryptButton = new JButton("Decrypt Video (PQC)");
        
        selectButton.addActionListener(e -> selectVideoFile());
        encryptButton.addActionListener(e -> encryptVideo());
        decryptButton.addActionListener(e -> decryptVideo());
        
        panel.add(selectButton);
        panel.add(encryptButton);
        panel.add(decryptButton);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        logArea = new JTextArea(10, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void selectVideoFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        Path defaultDir = Paths.get("testVideos");
        if (Files.exists(defaultDir)) {
            fileChooser.setCurrentDirectory(defaultDir.toFile());
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedVideoPath = fileChooser.getSelectedFile().toPath();
            log("Selected video: " + selectedVideoPath.getFileName());
        }
    }
    
    private void encryptVideo() {
        if (selectedVideoPath == null || !Files.exists(selectedVideoPath)) {
            JOptionPane.showMessageDialog(this, 
                "Please select a video file first", 
                "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Path encryptedDir = Paths.get("encryptedVideos");
            if (!Files.exists(encryptedDir)) {
                Files.createDirectories(encryptedDir);
            }
            
            Path outputFile = encryptedDir.resolve(
                selectedVideoPath.getFileName().toString() + ".encrypted"
            );
            
            log("Encrypting video...");
            VideoEncryptionService.encryptVideo(
                selectedVideoPath, 
                outputFile, 
                username, 
                password
            );
            
            log("Encryption complete: " + outputFile.getFileName());
            JOptionPane.showMessageDialog(this, 
                "Video encrypted successfully!\nSaved to: " + outputFile, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            log("ERROR: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Encryption failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void decryptVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        Path encryptedDir = Paths.get("encryptedVideos");
        if (Files.exists(encryptedDir)) {
            fileChooser.setCurrentDirectory(encryptedDir.toFile());
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        Path encryptedFile = fileChooser.getSelectedFile().toPath();
        Path decryptedDir = Paths.get("decryptedVideos");
        
        try {
            if (!Files.exists(decryptedDir)) {
                Files.createDirectories(decryptedDir);
            }
            
            String originalName = encryptedFile.getFileName().toString()
                .replace(".encrypted", "");
            Path outputFile = decryptedDir.resolve(originalName);
            
            log("Decrypting video...");
            VideoEncryptionService.decryptVideo(
                encryptedFile, 
                outputFile, 
                username, 
                password
            );
            
            log("Decryption complete: " + outputFile.getFileName());
            JOptionPane.showMessageDialog(this, 
                "Video decrypted successfully!\nSaved to: " + outputFile, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            log("ERROR: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Decryption failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now() + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}

