package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SupplierDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Supplier;
import com.healthfirst.pims.util.TableUtil;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicinePanel extends JPanel {
    private static final String[] COLUMNS = {"ID", "Name", "Company", "Type", "Price", "Stock", "Reorder", "Expiry", "Supplier"};
    private static final int COL_PRICE = 4;
    private static final int COL_STOCK = 5;
    private static final int COL_REORDER = 6;
    private static final String[] TYPES = {"Tablet", "Capsule", "Syrup", "Injection", "Cream", "Ointment", "Drops", "Inhaler", "Other"};
    // Row highlight colours
    private static final Color EXPIRED_BG  = new Color(0xFEE2E2);   // light red
    private static final Color EXPIRING_BG = new Color(0xFFEDD5);   // light orange
    private static final Color LOW_BG      = new Color(0xFEF9C3);   // light yellow
    private final MedicineDAO medicineDAO;
    private final SupplierDAO supplierDAO;
    private List<Medicine> medicines = new ArrayList<>();
    private Integer selectedId = null;
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 0: case COL_STOCK: case COL_REORDER: return Integer.class;
                case COL_PRICE: return BigDecimal.class;
                case 7: return LocalDate.class;
                default: return String.class;
            }
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[]{"All medicines", "Low stock", "Expiring within 1 month", "Expired"});
    private final JLabel lblCount = new JLabel(" ");
    private final JTextField txtName = new JTextField();
    private final JTextField txtCompany = new JTextField();
    private final JComboBox<String> cmbType = new JComboBox<>(TYPES);
    private final JTextField txtPrice = new JTextField();
    private final JTextField txtQuantity = new JTextField();
    private final JTextField txtReorder = new JTextField();
    private final JSpinner spnExpiry = new JSpinner(new SpinnerDateModel());
    private final JComboBox<Supplier> cmbSupplier = new JComboBox<>();
    private final JLabel lblStatus = new JLabel(" ");
    public MedicinePanel() {
        this(new MedicineDAO(), new SupplierDAO());
    }
    public MedicinePanel(MedicineDAO medicineDAO, SupplierDAO supplierDAO) {
        this.medicineDAO = medicineDAO;
        this.supplierDAO = supplierDAO;
        setLayout(new BorderLayout(15,0));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);
        refresh();
    }
    //  Layout
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        //toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        left.setOpaque(false);
        Theme.styleField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(240,38));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        styleCombo(cmbFilter);
        cmbFilter.addActionListener(e -> applyFilter());
        left.add(toolbarLabel("Search:"));
        left.add(txtSearch);
        left.add(toolbarLabel("Show:"));
        left.add(cmbFilter);
        toolbar.add(left, BorderLayout.WEST);
        lblCount.setFont(Theme.BODY);
        lblCount.setForeground(Theme.MUTED);
        toolbar.add(lblCount, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);
        configureTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        legend.setOpaque(false);
        legend.add(legendChip("Low stock", LOW_BG));
        legend.add(legendChip("Expires within 1 month", EXPIRING_BG));
        legend.add(legendChip("Expired", EXPIRED_BG));
        panel.add(legend, BorderLayout.SOUTH);
        return panel;
    }
    private void configureTable() {
        TableUtil.styleTable(table);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        RowRenderer renderer = new RowRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        int[] widths = {40, 185, 110, 80, 90, 65, 75, 110, 145};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });
    }
    private JPanel buildFormPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(14,11,14,11)));
        card.setPreferredSize(new Dimension(350, 0));
        // styles
        Theme.styleField(txtName);
        Theme.styleField(txtCompany);
        Theme.styleField(txtPrice);
        Theme.styleField(txtQuantity);
        Theme.styleField(txtReorder);
        styleCombo(cmbType);
        cmbType.setEditable(true);
        styleCombo(cmbSupplier);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spnExpiry, "yyyy-MM-dd");
        spnExpiry.setEditor(editor);
        editor.getTextField().setFont(Theme.BODY);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.NORTHWEST;
        JLabel title = new JLabel("Medicine Details");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 6;
        gc.insets = new Insets(0,4,12,4);
        card.add(title, gc);
        addField(card,"Name", txtName, 0,1,6);
        addField(card,"Company", txtCompany,0,2,6);
        addField(card,"Type", cmbType,0,3,3);
        addField(card,"Expiry (yyyy-MM-dd)", spnExpiry,3,3, 3);
        addField(card,"Price (R)", txtPrice,0,4,2);
        addField(card,"Stock", txtQuantity,2,4,2);
        addField(card,"Reorder At", txtReorder,4,4,2);
        addField(card,"Supplier", cmbSupplier,0,5,6);
        // buttons
        JButton btnAdd = Theme.button("Add", Theme.SUCCESS);
        JButton btnUpdate = Theme.button("Update", Theme.PRIMARY);
        JButton btnDelete = Theme.button("Delete", Theme.DANGER);
        JButton btnClear = Theme.button("Clear", Theme.MUTED);
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());
        gc.gridwidth = 3;
        gc.insets = new Insets(6,4,6,4);
        gc.gridy = 6; gc.gridx = 0; card.add(btnAdd, gc);
        gc.gridy = 6; gc.gridx = 3; card.add(btnUpdate, gc);
        gc.gridy = 7; gc.gridx = 0; card.add(btnDelete, gc);
        gc.gridy = 7; gc.gridx = 3; card.add(btnClear, gc);
        // status message
        lblStatus.setFont(Theme.SMALL);
        gc.gridx = 0; gc.gridy = 8; gc.gridwidth = 6;
        gc.insets = new Insets(8,4,0,4);
        card.add(lblStatus, gc);
        gc.gridy = 9; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        card.add(filler, gc);
        return card;
    }
    private void addField(JPanel card, String labelText, JComponent input, int x, int y, int span) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.SMALL.deriveFont(java.awt.Font.BOLD));
        label.setForeground(Theme.MUTED);
        input.setPreferredSize(new Dimension(input.getPreferredSize().width,38));
        JPanel cell = new JPanel(new BorderLayout(0,4));
        cell.setOpaque(false);
        cell.add(label, BorderLayout.NORTH);
        cell.add(input, BorderLayout.CENTER);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = span;
        gc.weightx = span;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0,4,12,4);
        card.add(cell, gc);
    }
    private JLabel toolbarLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JLabel legendChip(String text, Color background) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        l.setOpaque(true);
        l.setBackground(background);
        l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(5,12,5,12)));
        return l;
    }
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(Theme.BODY);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width,38));
    }
    public void refresh() {
        reloadSuppliers();
        loadMedicines();
        clearForm();
    }
    private void reloadSuppliers() {
        try {
            List<Supplier> suppliers = supplierDAO.getAll();
            cmbSupplier.removeAllItems();
            cmbSupplier.addItem(new Supplier(0," None "));
            for (Supplier s : suppliers) {
                cmbSupplier.addItem(s);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void loadMedicines() {
        try {
            medicines = medicineDAO.getAll();
        } catch (SQLException ex) {
            showDbError(ex);
            return;
        }
        tableModel.setRowCount(0);
        for (Medicine m : medicines) {
            tableModel.addRow(new Object[]{
                    m.getId(), m.getName(),
                    m.getCompany(), m.getType(),
                    m.getPrice(), m.getQuantity(),
                    m.getReorderLevel(), m.getExpiryDate(),
                    m.getSupplierName() == null ? "-" : m.getSupplierName()
            });
        }
        applyFilter();
    }
    //  Search and status
    private void applyFilter() {
        final String text = txtSearch.getText().trim().toLowerCase();
        final int mode = cmbFilter.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int index = entry.getIdentifier();
                if (index >= medicines.size()) {
                    return true;
                }
                Medicine m = medicines.get(index);
                boolean statusOk = mode == 0 || (mode == 1 && m.isLowStock()) || (mode == 2 && m.isExpiringSoon()) || (mode == 3 && m.isExpired());
                if (!statusOk) {
                    return false;
                }
                if (text.isEmpty()) {
                    return true;
                }
                return contains(m.getName(), text) || contains(m.getCompany(), text) || contains(m.getType(), text) || contains(m.getSupplierName(), text);
            }
        });
        lblCount.setText("Showing " + table.getRowCount() + " of " + medicines.size());
    }
    private static boolean contains(String value, String lowerCaseText) {
        return value != null && value.toLowerCase().contains(lowerCaseText);
    }
    private void onRowSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        Medicine m = medicines.get(table.convertRowIndexToModel(viewRow));
        selectedId = m.getId();
        txtName.setText(m.getName());
        txtCompany.setText(m.getCompany() == null ? "" : m.getCompany());
        cmbType.setSelectedItem(m.getType());
        txtPrice.setText(m.getPrice().toPlainString());
        txtQuantity.setText(String.valueOf(m.getQuantity()));
        txtReorder.setText(String.valueOf(m.getReorderLevel()));
        setExpiry(m.getExpiryDate());
        selectSupplier(m.getSupplierId());
        lblStatus.setText(" ");
    }
    private void selectSupplier(Integer supplierId) {
        cmbSupplier.setSelectedIndex(0);
        if (supplierId == null) {
            return;
        }
        for (int i = 0; i < cmbSupplier.getItemCount(); i++) {
            if (cmbSupplier.getItemAt(i).getId() == supplierId) {
                cmbSupplier.setSelectedIndex(i);
                return;
            }
        }
    }
    private void setExpiry(LocalDate date) {
        LocalDate d = date == null ? LocalDate.now().plusYears(1) : date;
        spnExpiry.setValue(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
    //  Buttons
    private void onAdd() {
        Medicine m = readForm();
        if (m == null) {
            return;
        }
        try {
            medicineDAO.add(m);
            loadMedicines();
            clearForm();
            showStatus("Added: " + m.getName(),true);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void onUpdate() {
        if (selectedId == null) {
            warn("Select a medicine in the table only then can you edit its details.");
            return;
        }
        Medicine m = readForm();
        if (m == null) {
            return;
        }
        m.setId(selectedId);
        try {
            medicineDAO.update(m);
            loadMedicines();
            clearForm();
            showStatus("Updated: " + m.getName(),true);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void onDelete() {
        if (selectedId == null) {
            warn("Select a medicine in the table first.");
            return;
        }
        String name = txtName.getText().trim();
        int choice = JOptionPane.showConfirmDialog(this, "Permanently delete \"" + name + "\"?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            medicineDAO.delete(selectedId);
            loadMedicines();
            clearForm();
            showStatus("Deleted: " + name, true);
        } catch (SQLIntegrityConstraintViolationException ex) {
            warn("\"" + name + "\" cannot be deleted as it appears in past sales.\n" + "Set its stock to 0 instead, so the sales history stays intact.");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void clearForm() {
        table.clearSelection();
        selectedId = null;
        txtName.setText("");
        txtCompany.setText("");
        cmbType.setSelectedIndex(0);
        txtPrice.setText("");
        txtQuantity.setText("");
        txtReorder.setText("10");
        setExpiry(null);
        if (cmbSupplier.getItemCount() > 0) {
            cmbSupplier.setSelectedIndex(0);
        }
        lblStatus.setText(" ");
    }
    //  Reading and validating the form
    private Medicine readForm() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            warn("Please enter the medicine name.");
            txtName.requestFocusInWindow();
            return null;
        }
        Object typeItem = cmbType.getSelectedItem();
        String type = typeItem == null ? "" : typeItem.toString().trim();
        if (type.isEmpty()) {
            warn("Please choose or type a medicine type.");
            return null;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(txtPrice.getText().trim().replace(',','.'));
        } catch (NumberFormatException ex) {
            warn("Price must be a number, for example 45.50");
            txtPrice.requestFocusInWindow();
            return null;
        }
        if (price.signum() < 0) {
            warn("Price cannot be negative.");
            return null;
        }
        price = price.setScale(2, RoundingMode.HALF_UP);
        int quantity;
        int reorder;
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
            reorder = Integer.parseInt(txtReorder.getText().trim());
        } catch (NumberFormatException ex) {
            warn("Stock and Reorder At must be whole numbers, for example 50.");
            return null;
        }
        if (quantity < 0 || reorder < 0) {
            warn("Stock and Reorder At cannot be negative.");
            return null;
        }try {
            spnExpiry.commitEdit();
        } catch (ParseException ex) {
            warn("Expiry date must look like 2027-06-30 (yyyy-MM-dd).");
            return null;
        }
        LocalDate expiry = ((Date) spnExpiry.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Supplier supplier = (Supplier) cmbSupplier.getSelectedItem();
        Medicine m = new Medicine();
        m.setName(name);m.setCompany(txtCompany.getText().trim());
        m.setType(type);m.setPrice(price);
        m.setQuantity(quantity);m.setReorderLevel(reorder);
        m.setExpiryDate(expiry);
        m.setSupplierId(supplier == null || supplier.getId() == 0 ? null : supplier.getId());
        return m;
    }
    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Check your input", JOptionPane.WARNING_MESSAGE);
    }
    private void showStatus(String message, boolean success) {
        lblStatus.setForeground(success ? Theme.SUCCESS : Theme.DANGER);
        lblStatus.setText(message);
    }
    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "A database error occurred:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
    //  Row colours: red = expired, orange = expiring, yellow = low stock
    private class RowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            Object shown = value;
            if (col == COL_PRICE && value instanceof BigDecimal) {
                shown = String.format(Locale.US,"R %,.2f", value);
            }
            super.getTableCellRendererComponent(t, shown, selected, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
            setForeground(Theme.TEXT);
            setHorizontalAlignment(
                    col == COL_PRICE || col == COL_STOCK || col == COL_REORDER ? RIGHT : LEFT);
            if (selected) {
                setBackground(t.getSelectionBackground());
            } else {
                Color bg = Color.WHITE;
                int modelRow = t.convertRowIndexToModel(row);
                if (modelRow < medicines.size()) {
                    Medicine m = medicines.get(modelRow);
                    if (m.isExpired()) bg = EXPIRED_BG;
                    else if (m.isExpiringSoon()) bg = EXPIRING_BG;
                    else if (m.isLowStock()) bg = LOW_BG;
                }
                setBackground(bg);
            }
            return this;
        }
    }
}
