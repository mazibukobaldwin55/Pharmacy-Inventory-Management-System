package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

public class HeaderPanel extends JPanel {
    public HeaderPanel(String title, User user, Runnable onLogout) {
        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.TITLE.deriveFont(20f));
        lblTitle.setForeground(Color.WHITE);
        add(lblTitle, BorderLayout.WEST);
        JLabel lblUser = new JLabel("Signed in as " + user.getFullName() + "  |  " + user.getRole());
        lblUser.setFont(Theme.BODY);
        lblUser.setForeground(Color.WHITE);
        JButton btnLogout = Theme.button("Logout", Theme.DANGER);
        btnLogout.addActionListener(e -> onLogout.run());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,15,0));
        right.setOpaque(false);right.add(lblUser);right.add(btnLogout);
        add(right, BorderLayout.EAST);
    }
}