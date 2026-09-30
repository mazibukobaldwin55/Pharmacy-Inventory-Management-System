package com.healthfirst.pims.ui;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.util.TableUtil;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ExpiryReportPanel extends JPanel {
    private static final String[] COLUMNS = {
            "Medicine",
            "Expiry Date",
            "Days Remaining",
            "Status"
    };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS,0) {
                @Override
                public boolean isCellEditable(int row, int column) {return false;}
            };
    private final JTable table = new JTable(tableModel);
    private final JLabel lblItems = new JLabel("Expiring Items: 0");
    public ExpiryReportPanel() {
        setLayout(new BorderLayout(15,15));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        loadReport();
    }
    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("Expiry Report");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);
        lblItems.setFont(Theme.BODY.deriveFont(Font.BOLD));
        lblItems.setForeground(Theme.MUTED);
        JButton btnRefresh = Theme.button("Refresh", Theme.PRIMARY);
        btnRefresh.addActionListener(e -> loadReport());
        right.add(lblItems);
        right.add(btnRefresh);
        panel.add(title, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CARD);
        panel.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        TableUtil.styleTable(table);
        TableUtil.padCells(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    private void loadReport() {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        LocalDate oneMonthLater = today.plusMonths(1);
        String sql =
                "SELECT name, expiry_date " +
                        "FROM medicines " +
                        "WHERE expiry_date >= ? " +
                        "AND expiry_date <= ? " +
                        "ORDER BY expiry_date ASC";
        int itemCount = 0;
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setDate(1, Date.valueOf(today));
            ps.setDate(2, Date.valueOf(oneMonthLater));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String medicine = rs.getString("name");
                    Date expiryDate = rs.getDate("expiry_date");
                    LocalDate expiry = expiryDate.toLocalDate();
                    long daysRemaining = ChronoUnit.DAYS.between(today, expiry);
                    String status;
                    if (daysRemaining == 0) {
                        status = "EXPIRES TODAY";
                    } else if (daysRemaining <= 7) {
                        status = "EXPIRING SOON";
                    } else {
                        status = "EXPIRING";
                    }
                    tableModel.addRow(
                            new Object[]{
                                    medicine,
                                    expiryDate,
                                    daysRemaining,
                                    status
                            }
                    );
                    itemCount++;
                }
            }
            lblItems.setText("Expiring Items: " + itemCount);
        } catch (SQLException ex) {
            lblItems.setText("Expiring Items: 0");
            JOptionPane.showMessageDialog(this,"Could not load the expiry report.\n\n" + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    public void refresh() {
        loadReport();
    }
}
