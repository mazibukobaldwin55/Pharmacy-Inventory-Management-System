package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.Theme;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;

 //Main window for cashiers. Cashiers can only sell and check stock
public class CashierDashboard extends JFrame {
    private final User currentUser;
    private final PosPanel posPanel;
    private final StockCheckPanel stockCheckPanel = new StockCheckPanel();
    public CashierDashboard(User user) {
        super("HealthFirst PIMS - Cashier");
        this.currentUser = user;
        this.posPanel = new PosPanel(user);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300,700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(new HeaderPanel("HealthFirst PIMS  -  Cashier", user, this::logout), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BOLD);
        tabs.addTab("Point of Sale", posPanel);
        tabs.addTab("Stock Check",   stockCheckPanel);
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == posPanel) {
                posPanel.refresh();
            } else if (selected == stockCheckPanel) {
                stockCheckPanel.refresh();
            }
        });
        add(tabs, BorderLayout.CENTER);
    }
    public User getCurrentUser() {
        return currentUser;
    }
    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}

