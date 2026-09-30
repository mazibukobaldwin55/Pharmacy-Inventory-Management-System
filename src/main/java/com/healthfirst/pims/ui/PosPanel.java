package com.healthfirst.pims.ui;
//The cashier's Point of Sale screen.
// Left: search the medicines and add them to the cart.
// Right: the current sale, with quantity controls, Clear Cart and Checkout.
import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.dao.SaleException;
import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.Money;
import com.healthfirst.pims.util.StatusColors;
import com.healthfirst.pims.util.TableUtil;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PosPanel extends JPanel {
    private static final String[] PRODUCT_COLUMNS = {"Medicine", "Company", "Type", "Price", "In Stock"};
    private static final String[] CART_COLUMNS = {"Item", "Price", "Qty", "Total"};

    private final User cashier;
    private final MedicineDAO medicineDAO;
    private final SaleDAO saleDAO;

    private List<Medicine> catalog = new ArrayList<>();
    private final List<CartItem> cart = new ArrayList<>();

    // ---- Product table ----
    private final DefaultTableModel productModel = new DefaultTableModel(PRODUCT_COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 3:  return BigDecimal.class;
                case 4:  return Integer.class;
                default: return String.class;
            }
        }
    };
    private final JTable productTable = new JTable(productModel);
    private final TableRowSorter<DefaultTableModel> productSorter = new TableRowSorter<>(productModel);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblCount = new JLabel(" ");
    private final JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1,1,999,1));
    private final JLabel lblMessage = new JLabel(" ");
    // ---- Cart table ----
    private final DefaultTableModel cartModel = new DefaultTableModel(CART_COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int col) {return false;}
    };
    private final JTable cartTable = new JTable(cartModel);
    private final JLabel lblItems = new JLabel("0 items");
    private final JLabel lblTotal = new JLabel(Money.format(BigDecimal.ZERO));

    public PosPanel(User cashier) {
        this(cashier, new MedicineDAO(), new SaleDAO());
    }
    public PosPanel(User cashier, MedicineDAO medicineDAO, SaleDAO saleDAO) {
        this.cashier = cashier;
        this.medicineDAO = medicineDAO;
        this.saleDAO = saleDAO;

        setLayout(new BorderLayout(15,0));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        add(buildProductPanel(), BorderLayout.CENTER);
        add(buildCartPanel(), BorderLayout.EAST);

        refresh();
    }
    //  Layout: left side (products)
    private JPanel buildProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,10));
        panel.setOpaque(false);
        //search bar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        left.setOpaque(false);
        JLabel searchLabel = new JLabel("Find medicine:");
        searchLabel.setFont(Theme.BOLD);
        searchLabel.setForeground(Theme.TEXT);
        Theme.styleField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(300,40));
        txtSearch.setToolTipText("Type a name, company or type. Press Enter to add the first match.");
        txtSearch.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){ applyFilter();}
            @Override public void removeUpdate(DocumentEvent e){ applyFilter();}
            @Override public void changedUpdate(DocumentEvent e){ applyFilter();}
        });
        txtSearch.addActionListener(e -> addFirstMatch());
        left.add(searchLabel);
        left.add(txtSearch);
        toolbar.add(left, BorderLayout.WEST);
        lblCount.setFont(Theme.BODY);
        lblCount.setForeground(Theme.MUTED);
        toolbar.add(lblCount, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);
        //product table
        TableUtil.styleTable(productTable);
        productTable.setRowSorter(productSorter);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ProductRenderer renderer = new ProductRenderer();
        for (int i = 0; i < productTable.getColumnCount(); i++){
            productTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        int[] widths = {230, 140, 90, 90, 80};
        for (int i = 0; i < widths.length; i++) {
            productTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    addSelectedToCart();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel addBar = new JPanel(new BorderLayout(15,0));
        addBar.setOpaque(false);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        controls.setOpaque(false);
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(Theme.BOLD);
        qtyLabel.setForeground(Theme.TEXT);
        spnQty.setFont(Theme.BODY);
        spnQty.setPreferredSize(new Dimension(80,40));
        JButton btnAdd = Theme.button("Add to Cart", Theme.SUCCESS_BOLD);
        btnAdd.setPreferredSize(new Dimension(140,40));
        btnAdd.addActionListener(e -> addSelectedToCart());
        controls.add(qtyLabel);controls.add(spnQty);controls.add(btnAdd);
        addBar.add(controls, BorderLayout.WEST);

        lblMessage.setFont(Theme.BOLD);
        addBar.add(lblMessage, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(addBar, BorderLayout.NORTH);
        south.add(StatusColors.legendPanel(), BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }
    //right side (cart)
    private JPanel buildCartPanel() {
        JPanel card = new JPanel(new BorderLayout(0,10));
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(12,12,12,12)));
        card.setPreferredSize(new Dimension(450,0));

        JLabel title = new JLabel("Current Sale");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        card.add(title, BorderLayout.NORTH);
        TableUtil.styleTable(cartTable);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        CartRenderer renderer = new CartRenderer();
        for (int i = 0; i < cartTable.getColumnCount(); i++) {
            cartTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        int[] widths = {190, 80, 45, 90};
        for (int i = 0; i < widths.length; i++) {
            cartTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        JButton btnPlus = Theme.button("+ 1", Color.BLACK);
        JButton btnMinus = Theme.button("- 1", Color.BLACK);
        JButton btnRemove = Theme.button("Remove", Theme.DANGER);
        btnPlus.addActionListener(e -> changeQuantity(1));
        btnMinus.addActionListener(e -> changeQuantity(-1));
        btnRemove.addActionListener(e -> removeSelected());
        JPanel lineButtons = new JPanel(new GridLayout(1,3,8,0));
        lineButtons.setOpaque(false);
        lineButtons.add(btnPlus);lineButtons.add(btnMinus);lineButtons.add(btnRemove);
        JPanel center = new JPanel(new BorderLayout(0,8));
        center.setOpaque(false);
        center.add(scroll, BorderLayout.CENTER);
        center.add(lineButtons, BorderLayout.SOUTH);
        card.add(center, BorderLayout.CENTER);

        lblItems.setFont(Theme.BODY);
        lblItems.setForeground(Theme.MUTED);
        JLabel totalCaption = new JLabel("TOTAL");
        totalCaption.setFont(Theme.BOLD);
        totalCaption.setForeground(Theme.MUTED);
        lblTotal.setFont(Theme.TITLE.deriveFont(30f));
        lblTotal.setForeground(Theme.PRIMARY);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel totals = new JPanel(new BorderLayout());
        totals.setOpaque(false);
        JPanel totalLeft = new JPanel(new GridLayout(2,1));
        totalLeft.setOpaque(false);
        totalLeft.add(totalCaption);
        totalLeft.add(lblItems);
        totals.add(totalLeft, BorderLayout.WEST);
        totals.add(lblTotal, BorderLayout.CENTER);

        JButton btnClear = Theme.button("Clear Cart", Theme.MUTED);
        JButton btnCheckout = Theme.button("Checkout", Theme.SUCCESS_BOLD);
        btnCheckout.setFont(Theme.HEADING);
        btnClear.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> checkout());

        JPanel actionButtons = new JPanel(new GridLayout(1,2,8,0));
        actionButtons.setOpaque(false);
        actionButtons.add(btnClear);
        actionButtons.add(btnCheckout);
        actionButtons.setPreferredSize(new Dimension(0,50));

        JPanel south = new JPanel(new BorderLayout(0,12));
        south.setOpaque(false);
        south.add(totals, BorderLayout.NORTH);
        south.add(actionButtons, BorderLayout.SOUTH);
        card.add(south, BorderLayout.SOUTH);

        return card;
    }
    //Loading and searching the catalog
    public void refresh() {
        try {
            catalog = medicineDAO.getAll();
        } catch (SQLException ex) {
            showDbError(ex);
            return;
        }
        productModel.setRowCount(0);
        for (Medicine m : catalog) {
            productModel.addRow(new Object[]{
                    m.getName(), m.getCompany(), m.getType(), m.getPrice(), m.getQuantity()});
        }
        applyFilter();
    }
    private void applyFilter() {
        String text = txtSearch.getText().trim();
        productSorter.setRowFilter(text.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0, 1, 2));
        lblCount.setText("Showing " + productTable.getRowCount() + " of " + catalog.size());
    }
    private Medicine medicineAtViewRow(int viewRow) {
        int modelRow = productTable.convertRowIndexToModel(viewRow);
        return modelRow < catalog.size() ? catalog.get(modelRow) : null;
    }
    //Adding to the cart
    private void addSelectedToCart() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) {
            message("Select a medicine from the list first.", false);
            return;
        }
        Medicine m = medicineAtViewRow(viewRow);
        if (m != null) {
            addToCart(m, readQuantity());
        }
    }
    private void addFirstMatch() {
        if (txtSearch.getText().trim().isEmpty()) {
            return;
        }
        if (productTable.getRowCount() == 0) {
            message("No medicine matches \"" + txtSearch.getText().trim() + "\".",false);
            return;
        }
        Medicine m = medicineAtViewRow(0);
        if (m != null && addToCart(m, readQuantity())) {
            txtSearch.setText("");
        }
        txtSearch.requestFocusInWindow();
    }
    private int readQuantity() {
        try {
            spnQty.commitEdit();
        } catch (ParseException ex) {
            spnQty.setValue(1);
        }
        return (Integer) spnQty.getValue();
    }
    private boolean addToCart(Medicine m, int qty) {
        if (m.isExpired()) {
            message(m.getName() + " has expired and can't be sold.",false);
            return false;
        }
        if (m.isOutOfStock()) {
            message(m.getName() + " is out of stock.",false);
            return false;
        }

        CartItem existing = findInCart(m.getId());
        int inCart = existing == null ? 0 : existing.getQuantity();
        int canAdd = m.getQuantity() - inCart;
        if (canAdd <= 0) {
            message("All " + m.getQuantity() + " units of " + m.getName() + " are already in the cart.",false);
            return false;
        }
        if (qty > canAdd) {
            message("Only " + canAdd + " more of " + m.getName() + " available" + (inCart > 0 ? " (" + inCart + " already in the cart)." : "."),false);
            return false;
        }
        int row;
        if (existing != null) {
            existing.setQuantity(inCart + qty);
            row = cart.indexOf(existing);
        } else {
            cart.add(new CartItem(m.getId(), m.getName(), m.getPrice(), qty, m.getQuantity()));
            row = cart.size() - 1;
        }
        refreshCart(row);
        spnQty.setValue(1);
        message("Added " + qty + " x " + m.getName(),true);
        return true;
    }
    private CartItem findInCart(int medicineId) {
        for (CartItem item : cart) {
            if (item.getMedicineId() == medicineId) {
                return item;
            }
        }
        return null;
    }
    private void changeQuantity(int delta) {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            message("Select an item in the cart first.",false);
            return;
        }
        CartItem item = cart.get(row);
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            removeSelected();
            return;
        }
        if (newQty > item.getStockLimit()) {
            message("Only " + item.getStockLimit() + " units of " + item.getName() + " in stock.",false);
            return;
        }
        item.setQuantity(newQty);
        refreshCart(row);
        message(" ",true);
    }
    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            message("Select an item in the cart first.",false);
            return;
        }
        CartItem removed = cart.remove(row);
        refreshCart(Math.min(row, cart.size() - 1));
        message("Removed " + removed.getName(),true);
    }
    private void clearCart() {
        if (cart.isEmpty()) {
            message("The cart is already empty.",false);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove all " + cart.size() + " item(s) from the cart?",
                    "Clear Cart", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            cart.clear();
            refreshCart(-1);
            message("Cart cleared.",true);
            txtSearch.requestFocusInWindow();
        }
    }
    private void refreshCart(int selectRow) {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        int units = 0;
        for (CartItem item : cart) {
            cartModel.addRow(new Object[]{
                    item.getName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal()});
            total = total.add(item.getLineTotal());
            units += item.getQuantity();
        }
        lblTotal.setText(Money.format(total));
        lblItems.setText(units + (units == 1 ? " item" : " items"));

        if (selectRow >= 0 && selectRow < cart.size()) {
            cartTable.setRowSelectionInterval(selectRow, selectRow);
        }
    }
    //Checkout
    private void checkout() {
        if (cart.isEmpty()) {
            message("The cart is empty. Add medicines first.",false);
            return;
        }
        try {
            Sale sale = saleDAO.checkout(cashier, cart);
            cart.clear();
            refreshCart(-1);
            refresh();
            message("Sale Number " + sale.getSaleId() + " completed.",true);
            onSaleCompleted(sale);
            txtSearch.requestFocusInWindow();
        } catch (SaleException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage() + "\n\nNothing was charged. " + "Adjust the cart and try again.", "Cannot complete sale", JOptionPane.WARNING_MESSAGE);
            refresh();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }
    //Called after a sale has been saved - opens the printable Bill window.
    private void onSaleCompleted(Sale sale) {
        new BillFrame(javax.swing.SwingUtilities.getWindowAncestor(this), sale).setVisible(true);
    }
    private void message(String text, boolean success) {
        lblMessage.setForeground(success ? Theme.SUCCESS : Theme.DANGER);
        lblMessage.setText(text);
    }
    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "A database error occurred:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private class ProductRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            Object shown = value instanceof BigDecimal ? Money.format((BigDecimal) value) : value;
            super.getTableCellRendererComponent(t, shown, selected, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
            setHorizontalAlignment(col >= 3 ? RIGHT : LEFT);
            Medicine m = medicineAtViewRow(row);
            setForeground(m != null && !m.isSellable() ? Theme.MUTED : Theme.TEXT);
            if (selected) {
                setBackground(t.getSelectionBackground());
            } else {
                setBackground(m == null ? Color.WHITE : StatusColors.rowBackground(m));
            }
            return this;
        }
    }
    private static class CartRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            Object shown = value instanceof BigDecimal ? Money.format((BigDecimal) value) : value;
            super.getTableCellRendererComponent(t, shown, selected, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
            setForeground(Theme.TEXT);
            setHorizontalAlignment(col >= 1 ? RIGHT : LEFT);
            if (selected) {
                setBackground(t.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF9FAFB));
            }
            return this;
        }
    }
}