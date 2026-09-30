package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.util.Money;
import com.healthfirst.pims.util.StatusColors;
import com.healthfirst.pims.util.TableUtil;
import com.healthfirst.pims.util.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//The cashier's "Stock Check" tab: look up the price and availability of a medicine
public class StockCheckPanel extends JPanel {
    private static final String[] COLUMNS = {"Medicine", "Company", "Type", "Price", "Qty", "Status", "Expiry"};
    private static final Color OK_BG = new Color(0xDCFCE7);
    private final MedicineDAO medicineDAO;
    private List<Medicine> catalog = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS,0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 3:  return BigDecimal.class;
                case 4:  return Integer.class;
                case 6:  return LocalDate.class;
                default: return String.class;
            }
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblCount = new JLabel(" ");
    private final JLabel lblHint = new JLabel("Select a medicine to see its details.");
    private final JLabel lblName = new JLabel(" ");
    private final JLabel lblMeta = new JLabel(" ");
    private final JLabel lblPrice = new JLabel(" ");
    private final JLabel lblStatus = new JLabel(" ");
    private final JLabel lblUnits = new JLabel(" ");
    private final JLabel lblExpiry  = new JLabel(" ");
    private final List<Component> detailParts = new ArrayList<>();
    public StockCheckPanel() {
        this(new MedicineDAO());
    }
    public StockCheckPanel(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
        setLayout(new BorderLayout(15,0));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildDetailCard(), BorderLayout.EAST);
        refresh();
    }
    //  Layout
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0,10));
        panel.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        left.setOpaque(false);
        JLabel searchLabel = new JLabel("Check stock for:");
        searchLabel.setFont(Theme.BOLD);
        searchLabel.setForeground(Theme.TEXT);
        Theme.styleField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(320, 40));
        txtSearch.setToolTipText("Type a name, company or type");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        left.add(searchLabel);
        left.add(txtSearch);
        toolbar.add(left, BorderLayout.WEST);
        lblCount.setFont(Theme.BODY);
        lblCount.setForeground(Theme.MUTED);
        toolbar.add(lblCount, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);
        TableUtil.styleTable(table);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        StockRenderer renderer = new StockRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        int[] widths = {220, 130, 85, 90, 55, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetails();
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(StatusColors.legendPanel(), BorderLayout.SOUTH);
        return panel;
    }
    private JPanel buildDetailCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(18,18,18,18)));
        card.setPreferredSize(new Dimension(350,0));
        lblHint.setFont(Theme.BODY);
        lblHint.setForeground(Theme.MUTED);
        left(lblHint);
        card.add(lblHint);
        lblName.setFont(Theme.TITLE.deriveFont(20f));
        lblName.setForeground(Theme.TEXT);
        lblMeta.setFont(Theme.BODY);
        lblMeta.setForeground(Theme.MUTED);
        lblPrice.setFont(Theme.TITLE.deriveFont(36f));
        lblPrice.setForeground(Theme.PRIMARY);
        lblUnits.setFont(Theme.BODY);
        lblUnits.setForeground(Theme.TEXT);
        lblExpiry.setFont(Theme.BOLD);
        lblExpiry.setForeground(Theme.TEXT);
        lblStatus.setFont(Theme.BOLD);
        lblStatus.setOpaque(true);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        pillRow.setOpaque(false);
        pillRow.add(lblStatus);
        pillRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        left(pillRow);
        addPart(card, lblName);
        addPart(card, lblMeta);
        addPart(card, Box.createVerticalStrut(22));
        addPart(card, caption("PRICE"));
        addPart(card, lblPrice);
        addPart(card, Box.createVerticalStrut(22));
        addPart(card, caption("AVAILABILITY"));
        addPart(card, pillRow);
        addPart(card, Box.createVerticalStrut(8));
        addPart(card, lblUnits);
        addPart(card, Box.createVerticalStrut(22));
        addPart(card, caption("EXPIRY DATE"));
        addPart(card, lblExpiry);
        card.add(Box.createVerticalGlue());
        showDetails();
        return card;
    }
    private void addPart(JPanel card, Component part) {
        if (part instanceof JComponent) {
            left((JComponent) part);
        }
        card.add(part);
        detailParts.add(part);
    }
    private JLabel caption(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL.deriveFont(java.awt.Font.BOLD));
        l.setForeground(Theme.MUTED);
        return l;
    }
    private void left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    public void refresh() {
        try {
            catalog = medicineDAO.getAll();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "A database error occurred:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        for (Medicine m : catalog) {
            tableModel.addRow(new Object[]{
                    m.getName(), m.getCompany(), m.getType(), m.getPrice(), m.getQuantity(), m.getStatusText(), m.getExpiryDate()});
        }
        applyFilter();
        showDetails();
    }
    private void applyFilter() {
        String text = txtSearch.getText().trim();
        sorter.setRowFilter(text.isEmpty()
                ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text),0, 1, 2));
        lblCount.setText("Showing " + table.getRowCount() + " of " + catalog.size());
    }
    private Medicine medicineAtViewRow(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow < catalog.size() ? catalog.get(modelRow) : null;
    }
    private void showDetails() {
        int viewRow = table.getSelectedRow();
        Medicine m = viewRow < 0 ? null : medicineAtViewRow(viewRow);
        lblHint.setVisible(m == null);
        for (Component part : detailParts) {
            part.setVisible(m != null);
        }
        if (m == null) {
            return;
        }
        lblName.setText(m.getName());
        lblMeta.setText(nz(m.getCompany()) + "  |  " + nz(m.getType()));
        lblPrice.setText(Money.format(m.getPrice()));
        lblStatus.setText(m.getStatusText());
        lblStatus.setBackground(pillBackground(m));
        lblStatus.setForeground(pillForeground(m));
        if (m.isExpired()) {
            lblUnits.setText("Do not sell. " + m.getQuantity() + " unit(s) on the shelf are expired.");
        } else if (m.isOutOfStock()) {
            lblUnits.setText("No units available.");
        } else {
            lblUnits.setText(m.getQuantity() + " unit(s) available");
        }
        lblExpiry.setText(m.getExpiryDate() == null ? "-" : m.getExpiryDate().toString());
    }
    private Color pillBackground(Medicine m) {
        if (m.isExpired())    return StatusColors.EXPIRED;
        if (m.isOutOfStock()) return StatusColors.OUT;
        if (m.isLowStock())   return StatusColors.LOW;
        return OK_BG;
    }
    private Color pillForeground(Medicine m) {
        if (m.isExpired())    return Theme.DANGER;
        if (m.isOutOfStock()) return Theme.TEXT;
        if (m.isLowStock())   return new Color(0x92400E);
        return new Color(0x166534);
    }
    private static String nz(String s) {
        return s == null ? "" : s;
    }
    //  Row colours
    private class StockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            Object shown = value instanceof BigDecimal ? Money.format((BigDecimal) value) : value;
            super.getTableCellRendererComponent(t, shown, selected,false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
            setForeground(Theme.TEXT);
            setHorizontalAlignment(col == 3 || col == 4 ? RIGHT : LEFT);
            if (selected) {
                setBackground(t.getSelectionBackground());
            } else {
                Medicine m = medicineAtViewRow(row);
                setBackground(m == null ? Color.WHITE : StatusColors.rowBackground(m));
            }
            return this;
        }
    }
}
