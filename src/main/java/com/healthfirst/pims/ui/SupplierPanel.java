package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.SupplierDAO;
import com.healthfirst.pims.model.Supplier;
import com.healthfirst.pims.util.FormUtil;
import com.healthfirst.pims.util.TableUtil;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SupplierPanel extends JPanel {
    private static final String[] COLUMNS = {"ID", "Name", "Contact Person", "Phone", "Email", "Address"};
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9 +()\\-]{7,20}$");
    private final SupplierDAO supplierDAO;
    private List<Supplier> suppliers = new ArrayList<>();
    private Integer selectedId = null;
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS,0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblCount = new JLabel(" ");
    private final JTextField txtName = new JTextField();
    private final JTextField txtContact = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextArea txtAddress = new JTextArea(1,20);
    private final JLabel lblStatus = new JLabel(" ");
    public SupplierPanel() {
        this(new SupplierDAO());
    }
    public SupplierPanel(SupplierDAO supplierDAO) {
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
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        left.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(Theme.BOLD);
        searchLabel.setForeground(Theme.TEXT);
        Theme.styleField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(260,38));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e){ applyFilter();}
            @Override public void removeUpdate(DocumentEvent e){ applyFilter();}
            @Override public void changedUpdate(DocumentEvent e){ applyFilter();}
        });
        left.add(searchLabel);
        left.add(txtSearch);
        toolbar.add(left, BorderLayout.WEST);
        lblCount.setFont(Theme.BODY);
        lblCount.setForeground(Theme.MUTED);
        toolbar.add(lblCount, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);
        TableUtil.styleTable(table);
        TableUtil.padCells(table);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {40, 190, 140, 125, 200, 190};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    private JPanel buildFormPanel() {
        JPanel card = FormUtil.formCard(350);
        Theme.styleField(txtName);
        Theme.styleField(txtContact);
        Theme.styleField(txtPhone);
        Theme.styleField(txtEmail);
        txtAddress.setFont(Theme.BODY);
        txtAddress.setForeground(Theme.TEXT);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        JScrollPane addressScroll = new JScrollPane(txtAddress);
        addressScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER,1, true));
        FormUtil.addTitle(card,"Supplier Details");
        FormUtil.addField(card,"Supplier Name", txtName,0,1,6);
        FormUtil.addField(card,"Contact Person", txtContact,0,2,6);
        FormUtil.addField(card,"Phone", txtPhone,0,3,6);
        FormUtil.addField(card,"Email", txtEmail,0,4,6);
        FormUtil.addField(card,"Address", addressScroll,0,5,6,60);
        JButton btnAdd = Theme.button("Add", Theme.SUCCESS);
        JButton btnUpdate = Theme.button("Update", Theme.PRIMARY);
        JButton btnDelete = Theme.button("Delete", Theme.DANGER);
        JButton btnClear  = Theme.button("Clear", Theme.MUTED);
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());
        FormUtil.addButtons(card, 6, btnAdd, btnUpdate, btnDelete, btnClear, lblStatus);
        return card;
    }
    public void refresh() {
        loadSuppliers();
        clearForm();
    }
    private void loadSuppliers() {
        try {
            suppliers = supplierDAO.getAll();
        } catch (SQLException ex) {
            showDbError(ex);
            return;
        }
        tableModel.setRowCount(0);
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                    s.getId(), s.getName(), nz(s.getContactPerson()), nz(s.getPhone()), nz(s.getEmail()), nz(s.getAddress())
            });
        }
        applyFilter();
    }
    private void applyFilter() {
        String text = txtSearch.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        lblCount.setText("Showing " + table.getRowCount() + " of " + suppliers.size());
    }
    private static String nz(String s) {
        return s == null ? "" : s;
    }
    //  Selecting a row fills the form
    private void onRowSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        Supplier s = suppliers.get(table.convertRowIndexToModel(viewRow));
        selectedId = s.getId();
        txtName.setText(s.getName());
        txtContact.setText(nz(s.getContactPerson()));
        txtPhone.setText(nz(s.getPhone()));
        txtEmail.setText(nz(s.getEmail()));
        txtAddress.setText(nz(s.getAddress()));
        txtAddress.setCaretPosition(0);
        lblStatus.setText(" ");
    }
    //  Buttons
    private void onAdd() {
        Supplier s = readForm();
        if (s == null) {
            return;
        }
        try {
            supplierDAO.add(s);
            loadSuppliers();
            clearForm();
            showStatus("Added: " + s.getName(), true);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void onUpdate() {
        if (selectedId == null) {
            warn("Select a supplier in the table first, then edit its details.");
            return;
        }
        Supplier s = readForm();
        if (s == null) {
            return;
        }
        s.setId(selectedId);
        try {
            supplierDAO.update(s);
            loadSuppliers();
            clearForm();
            showStatus("Updated: " + s.getName(), true);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void onDelete() {
        if (selectedId == null) {
            warn("Select a supplier in the table first.");
            return;
        }
        String name = txtName.getText().trim();
        int inUse;
        try {
            inUse = supplierDAO.countMedicines(selectedId);
        } catch (SQLException ex) {
            showDbError(ex);
            return;
        }
        String message = "Delete supplier \"" + name + "\"?";
        if (inUse > 0) {
            message += "\n\n" + inUse + " medicine(s) use this supplier. They will be kept," + "\nbut will show no supplier until you assign a new one.";
        }
        int choice = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            supplierDAO.delete(selectedId);
            loadSuppliers();
            clearForm();
            showStatus("Deleted: " + name,true);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void clearForm() {
        table.clearSelection();
        selectedId = null;
        txtName.setText("");
        txtContact.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        lblStatus.setText(" ");
    }
    //  Validation
    private Supplier readForm() {
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        if (name.isEmpty()) {
            warn("Please enter the supplier name.");
            txtName.requestFocusInWindow();
            return null;
        }
        if (name.length() > 100 || contact.length() > 100 || email.length() > 100) {
            warn("Name, contact person and email can each be at most 100 characters.");
            return null;
        }
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            warn("Phone number looks wrong. Use digits, spaces, + ( ) or -, for example 011 555 0101.");
            txtPhone.requestFocusInWindow();
            return null;
        }
        if (!email.isEmpty() && !EMAIL.matcher(email).matches()) {
            warn("Email address looks wrong. It should look like name@company.co.za");
            txtEmail.requestFocusInWindow();
            return null;
        }
        return new Supplier(0, name, contact, phone, email, address);
    }
    //  Messages
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
}
