package com.pqc.videoencryption.ui;

import com.pqc.videoencryption.storage.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(RegisterFrame.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField contactField;
    private JTextField emailField;
    private JTextField addressField;
    private LoginFrame parentFrame;
    
    public RegisterFrame(LoginFrame parent) {
        super("Post-Quantum Video Encryption - Register");
        this.parentFrame = parent;
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel titlePanel = createTitlePanel();
        JPanel registerPanel = createRegisterPanel();
        
        add(titlePanel, BorderLayout.NORTH);
        add(registerPanel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(49, 120, 206));
        panel.setPreferredSize(new Dimension(500, 50));
        panel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("New User Registration", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(title, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        contactField = new JTextField(20);
        emailField = new JTextField(20);
        addressField = new JTextField(20);
        
        addField(panel, gbc, "Username:", usernameField, 0);
        addField(panel, gbc, "Password:", passwordField, 1);
        addField(panel, gbc, "Contact:", contactField, 2);
        addField(panel, gbc, "Email:", emailField, 3);
        addField(panel, gbc, "Address:", addressField, 4);
        
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> handleRegister());
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);
        
        return panel;
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, String label, 
                         JComponent field, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
    
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        
        if (username.isEmpty()) {
            showError("Username is required");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Password is required");
            passwordField.requestFocus();
            return;
        }
        
        if (contact.isEmpty()) {
            showError("Contact number is required");
            contactField.requestFocus();
            return;
        }
        
        if (email.isEmpty()) {
            showError("Email is required");
            emailField.requestFocus();
            return;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format");
            emailField.requestFocus();
            return;
        }
        
        if (address.isEmpty()) {
            showError("Address is required");
            addressField.requestFocus();
            return;
        }
        
        try {
            UserRepository.createUser(username, password, contact, email, address);
            JOptionPane.showMessageDialog(this, 
                "Registration successful! You can now login.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            logger.error("Error during registration", e);
            showError("Error saving user data: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

