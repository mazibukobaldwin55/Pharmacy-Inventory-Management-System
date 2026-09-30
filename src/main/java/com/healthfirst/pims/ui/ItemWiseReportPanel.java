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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class ItemWiseReportPanel extends JPanel {
    private static final String[] COLUMNS = {
            "Medicine",
            "Quantity Sold",
            "Sales"
    };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS,0) {
                @Override
                public boolean isCellEditable(
                        int row, int column) {
                    return false;
                }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel lblItems = new JLabel("Medicines: 0");
    private final JLabel lblTotal = new JLabel("Total Sales: R0.00");
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
    public ItemWiseReportPanel() {
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
        JLabel title = new JLabel("Item-Wise Sales Report");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);
        lblItems.setFont(Theme.BODY);
        lblItems.setForeground(Theme.MUTED);
        lblTotal.setFont(Theme.BODY.deriveFont(Font.BOLD));
        lblTotal.setForeground(Theme.PRIMARY);
        JButton btnRefresh = Theme.button("Refresh", Theme.PRIMARY);
        btnRefresh.addActionListener(e -> loadReport());
        right.add(lblItems);
        right.add(lblTotal);
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
                "SELECT m.name AS medicine, " +
                        "       SUM(si.quantity_sold) AS quantity_sold, " +
                        "       SUM(si.quantity_sold * si.price_at_sale) AS sales " +
                        "FROM sale_items si " +
                        "INNER JOIN medicines m " +
                        "        ON si.medicine_id = m.medicine_id " +
                        "INNER JOIN sales s " +
                        "        ON si.sale_id = s.sale_id " +
                        "GROUP BY m.medicine_id, m.name " +
                        "ORDER BY sales DESC";
        int medicineCount = 0;
        BigDecimal totalSales = BigDecimal.ZERO;
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                String medicine = rs.getString("medicine");
                int quantity = rs.getInt("quantity_sold");
                BigDecimal sales = rs.getBigDecimal("sales");
                if (sales == null) {
                    sales = BigDecimal.ZERO;
                }
                tableModel.addRow(
                        new Object[]{
                                medicine,
                                quantity,
                                currency.format(sales)
                        }
                );
                medicineCount++;
                totalSales = totalSales.add(sales);
            }
            lblItems.setText("Medicines: " + medicineCount);
            lblTotal.setText("Total Sales: " + currency.format(totalSales));
        } catch (SQLException ex) {
            lblItems.setText("Medicines: 0");
            lblTotal.setText("Total Sales: R0.00");
            JOptionPane.showMessageDialog(
                    this,"Could not load the item-wise sales report.\n\n" + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void refresh() {
        loadReport();
    }
}