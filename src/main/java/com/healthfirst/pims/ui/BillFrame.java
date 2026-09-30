package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.util.Money;
import com.healthfirst.pims.util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;

//The bill (receipt) window shown after a successful checkout.the real sale number, date, cashier, the prices charged and the total.
//The cashier can print it or save it as a text file.
public class BillFrame extends JDialog {
    private static final int WIDTH = 48;
    private static final int NAME_W  = 21;
    private static final int QTY_W   = 3;
    private static final int PRICE_W = 10;
    private static final int TOTAL_W = 11;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final Sale sale;
    private final JTextArea billArea = new JTextArea();
    public BillFrame(java.awt.Window owner, Sale sale) {
        super(owner, "HealthFirst Pharmacy - Bill #" + sale.getSaleId(), ModalityType.APPLICATION_MODAL);
        this.sale = sale;
        setSize(580,660);
        setLocationRelativeTo(owner);
        buildUI();
        billArea.setText(buildReceipt(sale));
        billArea.setCaretPosition(0);
    }
    //  Screen layout
    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);
        billArea.setEditable(false);
        billArea.setLineWrap(false);
        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN,14));
        billArea.setForeground(Theme.TEXT);
        billArea.setBackground(Color.WHITE);
        billArea.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JScrollPane scroll = new JScrollPane(billArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(15,15,5,15));
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        // Buttons
        JButton btnPrint = Theme.button("Print Bill", Theme.PRIMARY);
        JButton btnSave  = Theme.button("Save Bill", Theme.PRIMARY);
        JButton btnClose = Theme.button("Close",  Theme.MUTED);
        btnPrint.addActionListener(e -> printBill());
        btnSave.addActionListener(e -> saveBill());
        btnClose.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,10));
        buttons.setOpaque(false);
        buttons.add(btnPrint);buttons.add(btnSave);buttons.add(btnClose);
        add(buttons, BorderLayout.SOUTH);
    }
    //Receipt text
    static String buildReceipt(Sale sale) {
        String equals = "=".repeat(WIDTH);
        String dashes = "-".repeat(WIDTH);
        StringBuilder sb = new StringBuilder();
        sb.append(equals).append('\n');
        sb.append(center("HEALTHFIRST PHARMACY")).append('\n');
        sb.append(center("SALES RECEIPT")).append('\n');
        sb.append(equals).append('\n');
        sb.append(String.format("%-11s: %d%n", "Receipt No", sale.getSaleId()));
        sb.append(String.format("%-11s: %s%n", "Date", sale.getSaleDate().format(DATE_FORMAT)));
        sb.append(String.format("%-11s: %s%n", "Cashier", sale.getCashierName()));
        sb.append(dashes).append('\n');
        sb.append(row("Item", "Qty", "Price", "Total")).append('\n');
        sb.append(dashes).append('\n');
        for (CartItem item : sale.getItems()) {
            String name = item.getName();
            String qty = String.valueOf(item.getQuantity());
            String price = Money.format(item.getUnitPrice());
            String lineTotal = Money.format(item.getLineTotal());
            if (name.length() <= NAME_W) {
                sb.append(row(name, qty, price, lineTotal)).append('\n');
            } else {
                for (String part : wrap(name, WIDTH)) {
                    sb.append(part).append('\n');
                }
                sb.append(row("", qty, price, lineTotal)).append('\n');
            }
        }
        sb.append(dashes).append('\n');
        sb.append(String.format("%-" + (WIDTH - TOTAL_W - 1) + "s %" + TOTAL_W + "s%n", "Items sold", sale.getItemCount()));
        sb.append(String.format("%-" + (WIDTH - TOTAL_W - 1) + "s %" + TOTAL_W + "s%n", "TOTAL", Money.format(sale.getTotalAmount())));
        sb.append(equals).append('\n');
        sb.append(center("Thank you for your purchase!")).append('\n');
        sb.append(center("Get well soon.")).append('\n');
        sb.append(equals).append('\n');
        return sb.toString();
    }
    private static String row(String name, String qty, String price, String total) {
        return String.format("%-" + NAME_W + "s %" + QTY_W + "s %" + PRICE_W + "s %" + TOTAL_W + "s", name, qty, price, total);
    }
    private static String center(String text) {
        int pad = Math.max(0, (WIDTH - text.length()) / 2);
        return " ".repeat(pad) + text;
    }
    private static java.util.List<String> wrap(String text, int width) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String rest = text.trim();
        while (rest.length() > width) {
            int cut = rest.lastIndexOf(' ', width);
            if (cut <= 0) {
                cut = width;
            }
            lines.add(rest.substring(0, cut).trim());
            rest = rest.substring(cut).trim();
        }
        lines.add(rest);
        return lines;
    }
    //  Print and save
    private void printBill() {
        try {
            boolean printed = billArea.print();
            if (printed) {
                JOptionPane.showMessageDialog(this,"Bill sent to the printer.","Print Bill", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Could not print the bill.\n\n" + ex.getMessage(),"Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void saveBill() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Bill");
        chooser.setSelectedFile(new File("Bill_" + sale.getSaleId() + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getParentFile(), file.getName() + ".txt");
        }
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this,
                    file.getName() + " already exists. Replace it?",
                       "Save Bill", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            Files.writeString(file.toPath(), billArea.getText(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Bill saved to:\n" + file.getAbsolutePath(),
                    "Save Bill", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the bill.\n\n" + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
