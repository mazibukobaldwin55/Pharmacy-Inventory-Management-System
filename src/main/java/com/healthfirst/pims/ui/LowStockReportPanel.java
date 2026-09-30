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

public class LowStockReportPanel extends JPanel {
    private static final String[] COLUMNS = {
            "Medicine",
            "Current Stock",
            "Reorder Level",
            "Status"
    };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS,0) {
                @Override
                public boolean isCellEditable(
                        int row, int column){
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);
    private final JLabel lblItems = new JLabel("Low Stock Items: 0");
    public LowStockReportPanel() {
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
        JLabel title = new JLabel("Low Stock Report");
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
        String sql =
                "SELECT name, " +
                        "       quantity_in_stock, " +
                        "       reorder_level " +
                        "FROM medicines " +
                        "WHERE quantity_in_stock <= reorder_level " +
                        "ORDER BY quantity_in_stock ASC, name ASC";
        int itemCount = 0;
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                String medicine = rs.getString("name");
                int currentStock = rs.getInt("quantity_in_stock");
                int reorderLevel = rs.getInt("reorder_level");
                String status;
                if (currentStock == 0) {
                    status = "OUT OF STOCK";
                } else {
                    status = "LOW STOCK";
                }
                tableModel.addRow(
                        new Object[]{
                                medicine,
                                currentStock,
                                reorderLevel,
                                status
                        }
                );
                itemCount++;
            }
            lblItems.setText("Low Stock Items: " + itemCount);
        } catch (SQLException ex) {
            lblItems.setText("Low Stock Items: 0");
            JOptionPane.showMessageDialog(this,"Could not load the low stock report.\n\n" + ex.getMessage(),"Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    public void refresh() {
        loadReport();
    }
}