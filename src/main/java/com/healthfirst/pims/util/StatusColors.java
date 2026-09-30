package com.healthfirst.pims.util;

import com.healthfirst.pims.model.Medicine;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;

public final class StatusColors {

    private StatusColors() { }

    public static final Color EXPIRED  = new Color(0xFEE2E2);   // light red
    public static final Color OUT      = new Color(0xE5E7EB);   // grey
    public static final Color EXPIRING = new Color(0xFFEDD5);   // light orange
    public static final Color LOW      = new Color(0xFEF9C3);   // light yellow

    public static Color rowBackground(Medicine m) {
        if (m.isExpired())      return EXPIRED;
        if (m.isOutOfStock())   return OUT;
        if (m.isExpiringSoon()) return EXPIRING;
        if (m.isLowStock())     return LOW;
        return Color.WHITE;
    }
    public static JPanel legendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        legend.setOpaque(false);
        legend.add(chip("Low stock", LOW));
        legend.add(chip("Expires within 1 month", EXPIRING));
        legend.add(chip("Out of stock", OUT));
        legend.add(chip("Expired", EXPIRED));
        return legend;
    }
    private static JLabel chip(String text, Color background) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        l.setOpaque(true);
        l.setBackground(background);
        l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(5,12,5,12)));
        return l;
    }
}