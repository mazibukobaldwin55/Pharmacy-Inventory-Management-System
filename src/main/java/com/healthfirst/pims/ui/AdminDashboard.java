package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.Theme;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;

//Main window for administrators here Manage Medicines, Suppliers, Users, plus a Reports tab that holds the four report screens.
public class AdminDashboard extends JFrame {
    private final User currentUser;
    private final MedicinePanel medicinePanel = new MedicinePanel();
    private final SupplierPanel supplierPanel = new SupplierPanel();
    private final UserPanel userPanel = new UserPanel();
    private final SalesReportPanel salesReportPanel = new SalesReportPanel();
    private final ItemWiseReportPanel itemWiseReportPanel = new ItemWiseReportPanel();
    private final LowStockReportPanel lowStockReportPanel = new LowStockReportPanel();
    private final ExpiryReportPanel expiryReportPanel = new ExpiryReportPanel();
    public AdminDashboard(User user) {
        super("HealthFirst PIMS - Administrator");
        this.currentUser = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300,700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(new HeaderPanel("HealthFirst PIMS  -  Admin", user, this::logout), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BOLD);
        tabs.addTab("Manage Medicines", medicinePanel);
        tabs.addTab("Manage Suppliers", supplierPanel);
        tabs.addTab("Manage Users", userPanel);
        JTabbedPane reportsTabs = new JTabbedPane();
        reportsTabs.addTab("Sales", salesReportPanel);
        reportsTabs.addTab("Item-Wise", itemWiseReportPanel);
        reportsTabs.addTab("Low Stock", lowStockReportPanel);
        reportsTabs.addTab("Expiry", expiryReportPanel);
        tabs.addTab("Reports", reportsTabs);
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == medicinePanel) {
                medicinePanel.refresh();
            } else if (selected == supplierPanel) {
                supplierPanel.refresh();
            } else if (selected == userPanel) {
                userPanel.refresh();
            } else if (selected == reportsTabs) {
                refreshReport(reportsTabs.getSelectedComponent());
            }
        });
        reportsTabs.addChangeListener(e -> refreshReport(reportsTabs.getSelectedComponent()));
        add(tabs, BorderLayout.CENTER);
    }
    public User getCurrentUser() {
        return currentUser;
    }

    private void refreshReport(Component report) {
        if (report == salesReportPanel) {
            salesReportPanel.refresh();
        } else if (report == itemWiseReportPanel) {
            itemWiseReportPanel.refresh();
        } else if (report == lowStockReportPanel) {
            lowStockReportPanel.refresh();
        } else if (report == expiryReportPanel) {
            expiryReportPanel.refresh();
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}

