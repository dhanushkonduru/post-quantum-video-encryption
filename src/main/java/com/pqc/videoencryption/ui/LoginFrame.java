package com.pqc.videoencryption.ui;

import com.pqc.videoencryption.storage.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginFrame() {
        super("Post-Quantum Video Encryption - Login");
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel titlePanel = createTitlePanel();
        JPanel loginPanel = createLoginPanel();
        
        add(titlePanel, BorderLayout.NORTH);
        add(loginPanel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(49, 120, 206));
        panel.setPreferredSize(new Dimension(600, 60));
        panel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Post-Quantum Video Encryption System", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(title, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton resetButton = new JButton("Reset");
        
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> openRegister());
        resetButton.addActionListener(e -> resetFields());
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(resetButton);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required", "Error", 
                JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required", "Error", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        try {
            if (UserRepository.authenticate(username, password)) {
                setVisible(false);
                UserScreenFrame userScreen = new UserScreenFrame(username, password);
                userScreen.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", 
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                resetFields();
            }
        } catch (IOException e) {
            logger.error("Error during authentication", e);
            JOptionPane.showMessageDialog(this, 
                "Error accessing user database: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openRegister() {
        RegisterFrame registerFrame = new RegisterFrame(this);
        registerFrame.setVisible(true);
        setVisible(false);
    }
    
    private void resetFields() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }
}

