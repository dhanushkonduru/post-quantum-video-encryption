package com.pqc.videoencryption;

import com.pqc.videoencryption.storage.UserRepository;
import com.pqc.videoencryption.ui.LoginFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;

public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Could not set system look and feel", e);
        }
        
        try {
            UserRepository.initialize();
        } catch (IOException e) {
            logger.error("Failed to initialize user repository", e);
            JOptionPane.showMessageDialog(null, 
                "Failed to initialize application: " + e.getMessage(), 
                "Initialization Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

