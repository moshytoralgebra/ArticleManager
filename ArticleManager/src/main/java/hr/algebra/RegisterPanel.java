package hr.algebra;

import hr.algebra.dal.UserRepository;
import hr.algebra.dal.sql.SqlUserRepository;
import hr.algebra.model.Role;
import hr.algebra.model.User;
import hr.algebra.utilities.MessageUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterPanel extends javax.swing.JPanel {

    private final javax.swing.JDialog parentDialog;
    private final UserRepository userRepository = new SqlUserRepository();

    public RegisterPanel(javax.swing.JDialog dialog) {
        this.parentDialog = dialog;
        initComponents();
    }

    private void initComponents() {
        tfUsername        = new javax.swing.JTextField(22);
        pfPassword        = new javax.swing.JPasswordField(22);
        pfConfirmPassword = new javax.swing.JPasswordField(22);
        btnRegister       = new javax.swing.JButton("Register");

        btnRegister.addActionListener(this::btnRegisterActionPerformed);

        javax.swing.JLabel lblUsername        = new javax.swing.JLabel("Username:");
        javax.swing.JLabel lblPassword        = new javax.swing.JLabel("Password:");
        javax.swing.JLabel lblConfirmPassword = new javax.swing.JLabel("Confirm password:");

        setLayout(new java.awt.GridBagLayout());
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 36, 24, 36));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 8, 6, 8);

        // row 0 — username
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        add(lblUsername, gbc);
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(tfUsername, gbc);

        // row 1 — password
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        add(lblPassword, gbc);
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(pfPassword, gbc);

        // row 2 — confirm password
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        add(lblConfirmPassword, gbc);
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(pfConfirmPassword, gbc);

        // row 3 — register button centered under both columns
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.insets = new java.awt.Insets(14, 8, 6, 8);
        add(btnRegister, gbc);
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        String username        = tfUsername.getText().trim();
        String password        = new String(pfPassword.getPassword());
        String confirmPassword = new String(pfConfirmPassword.getPassword());

        if (username.isEmpty()) {
            MessageUtils.showErrorMessage("Register", "Username is required.");
            return;
        }
        if (password.isEmpty()) {
            MessageUtils.showErrorMessage("Register", "Password is required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            MessageUtils.showErrorMessage("Register", "Passwords do not match.");
            return;
        }

        try {
            if (userRepository.userExists(username)) {
                MessageUtils.showErrorMessage("Register", "That username is already taken.");
                return;
            }
            userRepository.createUser(new User(username, password, Role.USER));
            MessageUtils.showInformationMessage("Register", "Account created. You may now log in.");
            parentDialog.dispose();
        } catch (Exception ex) {
            Logger.getLogger(RegisterPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Registration failed. Please try again.");
        }
    }

    // Variables declaration
    private javax.swing.JTextField     tfUsername;
    private javax.swing.JPasswordField pfPassword;
    private javax.swing.JPasswordField pfConfirmPassword;
    private javax.swing.JButton        btnRegister;
    // End of variables declaration
}
