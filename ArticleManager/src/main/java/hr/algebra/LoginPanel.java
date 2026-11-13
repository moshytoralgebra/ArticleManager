package hr.algebra;

import hr.algebra.dal.UserRepository;
import hr.algebra.dal.sql.SqlUserRepository;
import hr.algebra.model.User;
import hr.algebra.utilities.MessageUtils;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginPanel extends javax.swing.JPanel {

    private final LoginFrame loginFrame;
    private final UserRepository userRepository = new SqlUserRepository();

    public LoginPanel(LoginFrame frame) {
        this.loginFrame = frame;
        initComponents();
    }

    private void initComponents() {
        tfUsername  = new javax.swing.JTextField(22);
        pfPassword  = new javax.swing.JPasswordField(22);
        btnLogin    = new javax.swing.JButton("Login");
        btnRegister = new javax.swing.JButton("Register");

        btnLogin.addActionListener(this::btnLoginActionPerformed);
        btnRegister.addActionListener(this::btnRegisterActionPerformed);

        javax.swing.JLabel lblTitle    = new javax.swing.JLabel("Article Manager", javax.swing.SwingConstants.CENTER);
        javax.swing.JLabel lblUsername = new javax.swing.JLabel("Username:");
        javax.swing.JLabel lblPassword = new javax.swing.JLabel("Password:");

        lblTitle.setFont(lblTitle.getFont().deriveFont(java.awt.Font.BOLD, 20f));

        javax.swing.JPanel btnPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 0));
        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);

        setLayout(new java.awt.GridBagLayout());
        setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 50, 30, 50));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 8, 6, 8);

        // row 0 — title spans both columns
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(lblTitle, gbc);

        // row 1 — visual separator
        gbc.gridy = 1;
        add(new javax.swing.JSeparator(), gbc);

        // row 2 — username
        gbc.gridwidth = 1; gbc.gridy = 2; gbc.gridx = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        add(lblUsername, gbc);
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(tfUsername, gbc);

        // row 3 — password
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        add(lblPassword, gbc);
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(pfPassword, gbc);

        // row 4 — buttons centered under both columns
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.insets = new java.awt.Insets(14, 8, 6, 8);
        add(btnPanel, gbc);
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            MessageUtils.showErrorMessage("Login", "Please enter username and password.");
            return;
        }

        try {
            Optional<User> result = userRepository.selectUser(username, password);
            if (result.isPresent()) {
                loginFrame.onLoginSuccess(result.get());
            } else {
                MessageUtils.showErrorMessage("Login failed", "Invalid username or password.");
            }
        } catch (Exception ex) {
            Logger.getLogger(LoginPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Could not connect to the database.");
        }
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(loginFrame, "Register New Account", true);
        dialog.setContentPane(new RegisterPanel(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(loginFrame);
        dialog.setVisible(true);
    }

    // Variables declaration
    private javax.swing.JTextField    tfUsername;
    private javax.swing.JPasswordField pfPassword;
    private javax.swing.JButton       btnLogin;
    private javax.swing.JButton       btnRegister;
    // End of variables declaration
}
