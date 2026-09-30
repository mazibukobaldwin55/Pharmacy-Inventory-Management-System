package com.healthfirst.pims.util;
//One place for the system's look and feel
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class Theme {
    private Theme() { }
    // ---- Colours ----
    public static final Color PRIMARY = new Color(0x4C1D95);
    public static final Color PRIMARY_DARK = new Color(0x4C1D95);
    public static final Color PRIMARY_LIGHT = new Color(0x8B5CF6);
    public static final Color BACKGROUND = new Color(0xF3F4F6);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xD1D5DB);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color SUCCESS_BOLD = new Color(0x15803D);
    public static final Color WARNING = new Color(0xD97706);
    // ---- Fonts ----
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD,26);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD,16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN,14);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD,14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN,12);
    public static JButton button(String text, Color background) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = background;
                if (!isEnabled()) c = BORDER;
                else if (getModel().isPressed()) c = background.darker();
                else if (getModel().isRollover()) c = background.brighter();
                g2.setColor(c);
                g2.fillRoundRect(0,0, getWidth(), getHeight(),10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(BOLD);
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setBorder(BorderFactory.createEmptyBorder(10,22,10,22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    // Rounded outline and inner padding for text fields and password fields.
    public static void styleField(JTextField field) {
        field.setFont(BODY);
        field.setForeground(TEXT);
        Border outline = BorderFactory.createLineBorder(BORDER,1,true);
        Border padding = BorderFactory.createEmptyBorder(8,10,8,10);
        field.setBorder(BorderFactory.createCompoundBorder(outline, padding));
    }
}
