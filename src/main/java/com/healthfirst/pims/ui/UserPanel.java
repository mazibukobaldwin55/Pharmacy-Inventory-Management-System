package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.FormUtil;
import com.healthfirst.pims.util.TableUtil;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//The administrator creates, updates and deletes Cashier accounts here.
public class UserPanel extends JPanel {
    private static final String[] COLUMNS = {"ID", "Username", "Full Name", "Role"};
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._]{3,50}$");
    private final UserDAO userDAO;
    private List<User> users = new ArrayList<>();
    private Integer selectedId = null;
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
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
    private final JTextField txtFullName = new JTextField();
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JPasswordField txtConfirm = new JPasswordField();
    private final JLabel lblStatus = new JLabel(" ");
    public UserPanel() {
        this(new UserDAO());
    }
    public UserPanel(UserDAO userDAO) {
        this.userDAO = userDAO;
        setLayout(new BorderLayout(15,0));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);
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
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(Theme.BOLD);
        searchLabel.setForeground(Theme.TEXT);
        Theme.styleField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(260,38));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter();}
            @Override public void removeUpdate(DocumentEvent e) { applyFilter();}
            @Override public void changedUpdate(DocumentEvent e) { applyFilter();}
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
        int[] widths = {50, 200, 280, 120};
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
        Theme.styleField(txtFullName);
        Theme.styleField(txtUsername);
        Theme.styleField(txtPassword);
        Theme.styleField(txtConfirm);
        FormUtil.addTitle(card, "Cashier Account");
        FormUtil.addField(card, "Full Name", txtFullName,0,1,6);
        FormUtil.addField(card, "Username", txtUsername,0,2,6);
        FormUtil.addField(card, "Password", txtPassword,0,3,6);
        FormUtil.addField(card, "Confirm Password", txtConfirm,0,4,6);
        FormUtil.addNote(card,"Password: at least " + MIN_PASSWORD_LENGTH + " characters.<br>" + "When updating, leave both password boxes<br>"
                        + "empty to keep the current password.<br>" + "The username can't be changed.", 5);

        JButton btnAdd    = Theme.button("Add",    Theme.SUCCESS);
        JButton btnUpdate = Theme.button("Update", Theme.PRIMARY);
        JButton btnDelete = Theme.button("Delete", Theme.DANGER);
        JButton btnClear  = Theme.button("Clear",  Theme.MUTED);
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());
        FormUtil.addButtons(card, 6, btnAdd, btnUpdate, btnDelete, btnClear, lblStatus);
        return card;
    }
    public void refresh() {
        loadUsers();
        clearForm();
    }
    private void loadUsers() {
        try {
            users = userDAO.getCashiers();
        } catch (SQLException ex) {
            showDbError(ex);
            return;
        }
        tableModel.setRowCount(0);
        for (User u : users) {
            tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(), u.getRole()});
        }
        applyFilter();
    }
    private void applyFilter() {
        String text = txtSearch.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        lblCount.setText("Showing " + table.getRowCount() + " of " + users.size());
    }
    //  Selecting a row fills the form
    private void onRowSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        User u = users.get(table.convertRowIndexToModel(viewRow));
        selectedId = u.getUserId();
        txtFullName.setText(u.getFullName());
        txtUsername.setText(u.getUsername());
        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(0xF3F4F6));
        txtPassword.setText("");
        txtConfirm.setText("");
        lblStatus.setText(" ");
    }
    //  Buttons
    private void onAdd() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (selectedId != null) {
            warn("A cashier is selected. Click Clear first to create a new account.");
            return;
        }
        if (fullName.isEmpty()) {
            warn("Please enter the cashier's full name.");
            txtFullName.requestFocusInWindow();
            return;
        }
        if (!USERNAME.matcher(username).matches()) {
            warn("Username must be 3-50 characters: letters, numbers, dots or underscores only.");
            txtUsername.requestFocusInWindow();
            return;
        }
        if (!passwordOk(password, confirm)) {
            return;
        }
        try {
            userDAO.addCashier(username, fullName, password);
            loadUsers();
            clearForm();
            showStatus("Created cashier account: " + username, true);
        } catch (SQLIntegrityConstraintViolationException ex) {
            warn("The username \"" + username + "\" is already taken. Please choose another one.");
            txtUsername.requestFocusInWindow();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void onUpdate() {
        if (selectedId == null) {
            warn("Select a cashier in the table first.");
            return;
        }
        String fullName = txtFullName.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());
        if (fullName.isEmpty()) {
            warn("Please enter the cashier's full name.");
            return;
        }
        boolean resetting = !password.isEmpty() || !confirm.isEmpty();
        if (resetting && !passwordOk(password, confirm)) {
            return;
        }
        try {
            userDAO.updateCashier(selectedId, fullName, resetting ? password : null);
            String username = txtUsername.getText();
            loadUsers();
            clearForm();
            showStatus("Updated: " + username + (resetting ? " (password reset)" : ""),true);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    private void onDelete() {
        if (selectedId == null) {
            warn("Select a cashier in the table first.");
            return;
        }
        String username = txtUsername.getText().trim();
        int choice = JOptionPane.showConfirmDialog(this,
                "Permanently delete the cashier account \"" + username + "\"?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            userDAO.deleteCashier(selectedId);
            loadUsers();
            clearForm();
            showStatus("Deleted: " + username,true);
        } catch (SQLIntegrityConstraintViolationException ex) {
            warn("\"" + username + "\" has processed sales, so the account can't be deleted " + "without erasing the sales history.\nTo block their access, reset their password instead.");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void clearForm() {
        table.clearSelection();
        selectedId = null;
        txtFullName.setText("");
        txtUsername.setText("");
        txtUsername.setEditable(true);
        txtUsername.setBackground(Color.WHITE);
        txtPassword.setText("");
        txtConfirm.setText("");
        lblStatus.setText(" ");
    }
    //  Validation
    private boolean passwordOk(String password, String confirm) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            warn("The password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            txtPassword.requestFocusInWindow();
            return false;
        }
        if (!password.equals(confirm)) {
            warn("The two passwords don't match.");
            txtConfirm.requestFocusInWindow();
            return false;
        }
        return true;
    }
    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Check your input", JOptionPane.WARNING_MESSAGE);
    }
    private void showStatus(String message, boolean success) {
        lblStatus.setForeground(success ? Theme.SUCCESS : Theme.DANGER);
        lblStatus.setText(message);
    }
    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this,"A database error occurred:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
