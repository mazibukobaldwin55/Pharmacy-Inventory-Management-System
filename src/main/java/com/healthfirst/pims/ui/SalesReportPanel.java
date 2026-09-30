package com.healthfirst.pims.ui;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.util.TableUtil;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class SalesReportPanel extends JPanel {

    private static final String[] COLUMNS = {
            "Sale ID", "Date", "Cashier", "Total"
    };
    private final DefaultTableModel tableModel =
            new DefaultTableModel(COLUMNS,0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);
    private final JLabel lblTransactions = new JLabel("Transactions: 0");
    private final JLabel lblTotal = new JLabel("Total Sales: R0.00");
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en","ZA"));
    public SalesReportPanel(){
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
        JLabel title = new JLabel("Sales Report");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);
        lblTransactions.setFont(Theme.BODY);
        lblTransactions.setForeground(Theme.MUTED);
        lblTotal.setFont(Theme.BODY.deriveFont(Font.BOLD));
        lblTotal.setForeground(Theme.PRIMARY);
        JButton btnRefresh = Theme.button("Refresh", Theme.PRIMARY);
        btnRefresh.addActionListener(e -> loadReport());
        right.add(lblTransactions);
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
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    private void loadReport() {
        tableModel.setRowCount(0);
        String sql =
                "SELECT s.sale_id, " +
                        "       s.sale_date, " +
                        "       COALESCE(u.full_name, u.username, 'Unknown') AS cashier, " +
                        "       s.total_amount " +
                        "FROM sales s " +
                        "LEFT JOIN users u ON s.user_id = u.user_id " +
                        "ORDER BY s.sale_date DESC";
        int transactionCount = 0;
        BigDecimal totalSales = BigDecimal.ZERO;
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                int saleId = rs.getInt("sale_id");
                String saleDate = rs.getTimestamp("sale_date").toLocalDateTime().toString().replace("T", " ");
                String cashier = rs.getString("cashier");
                BigDecimal total = rs.getBigDecimal("total_amount");
                if (total == null) {
                    total = BigDecimal.ZERO;
                }
                tableModel.addRow(new Object[]{
                        saleId,
                        saleDate,
                        cashier,
                        currency.format(total)
                });
                transactionCount++;
                totalSales = totalSales.add(total);
            }
            lblTransactions.setText("Transactions: " + transactionCount);
            lblTotal.setText("Total Sales: " + currency.format(totalSales));
        } catch (SQLException ex) {
            lblTransactions.setText("Transactions: 0");
            lblTotal.setText("Total Sales: R0.00");
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Could not load the sales report.\n\n" + ex.getMessage(), "Database Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }
    public void refresh() {
        loadReport();
    }
}
