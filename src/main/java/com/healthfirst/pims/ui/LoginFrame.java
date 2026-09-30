package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.SQLException;
//Login screen. On success it checks the user's role and opens either the AdminDashboard or the CashierDashboard.
public class LoginFrame extends JFrame {
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");
    private final JButton btnLogin = Theme.button("Sign In", Theme.PRIMARY);
    private final UserDAO userDAO = new UserDAO();
    public LoginFrame() {
        super("HealthFirst PIMS - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820,480);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new BrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);
        // Pressing Enter anywhere in the window clicks "Sign In"
        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(30,50,30,50));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        JLabel title = new JLabel("We're Glad You're Here!");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        gc.insets = new Insets(0,0,4,0);
        panel.add(title, gc);

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.MUTED);
        gc.insets = new Insets(0,0,24,0);
        panel.add(subtitle, gc);
        gc.insets = new Insets(0,0,6,0);
        panel.add(fieldLabel("Username"), gc);
        Theme.styleField(txtUsername);
        gc.insets = new Insets(0,0,16,0);
        panel.add(txtUsername, gc);
        gc.insets = new Insets(0,0,6,0);
        panel.add(fieldLabel("Password"), gc);
        Theme.styleField(txtPassword);
        gc.insets = new Insets(0,0,8,0);
        panel.add(txtPassword, gc);
        lblError.setFont(Theme.SMALL);
        lblError.setForeground(Theme.DANGER);
        gc.insets = new Insets(0,0,14,0);
        panel.add(lblError, gc);
        btnLogin.addActionListener(e -> doLogin());
        btnLogin.setPreferredSize(new Dimension(100,42));
        gc.insets = new Insets(0,0,0,0);
        panel.add(btnLogin, gc);
        return panel;
    }
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }
    //  Login logic
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }
        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                showError("Invalid username or password.");
                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
                return;
            }
            openDashboard(user);
        } catch (SQLException ex) {
            showError("Cannot connect to the database.");
            JOptionPane.showMessageDialog(this,
                    "Could not reach the database. Make sure MySQL is running.\n\nDetails: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //Role-based redirect: Admin -> AdminDashboard, Cashier -> CashierDashboard.
    private void openDashboard(User user) {
        dispose();
        JFrame dashboard = user.isAdmin()
                ? new AdminDashboard(user)
                : new CashierDashboard(user);
        dashboard.setVisible(true);
    }
    private void showError(String message) {
        lblError.setText(message);
    }
    private static class BrandPanel extends JPanel {
        BrandPanel() {
            setPreferredSize(new Dimension(340,480));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            g2.setPaint(new GradientPaint(0,0, Theme.PRIMARY_DARK, w, h, Theme.PRIMARY));
            g2.fillRect(0,0, w, h);

            int cx = w / 2; int cy = 170;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(cx - 14,cy - 44,28,88,10,10);
            g2.fillRoundRect(cx - 44,cy - 14,88,28,10,10);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.TITLE.deriveFont(28f));
            drawCentered(g2, "HealthFirst", w,280);
            g2.setFont(Theme.BODY.deriveFont(16f));
            g2.setColor(new Color(255,255,255,200));
            drawCentered(g2, "Pharmacy Inventory", w,312);
            drawCentered(g2, "Management System", w,335);
            g2.dispose();
        }
        private void drawCentered(Graphics2D g2, String text, int panelWidth, int y) {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (panelWidth - fm.stringWidth(text)) / 2, y);
        }
    }
}