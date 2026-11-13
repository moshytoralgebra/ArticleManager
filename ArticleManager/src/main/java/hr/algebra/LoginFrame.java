package hr.algebra;

import hr.algebra.model.User;

public class LoginFrame extends javax.swing.JFrame {

    public LoginFrame() {
        initComponents();
        configurePanel();
    }

    private void initComponents() {
        setTitle("Article Manager — Login");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new java.awt.BorderLayout());
    }

    private void configurePanel() {
        getContentPane().add(new LoginPanel(this), java.awt.BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public void onLoginSuccess(User user) {
        dispose();
        new ArticleManager(user).setVisible(true);
    }
}
