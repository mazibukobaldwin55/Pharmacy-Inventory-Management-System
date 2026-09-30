package com.healthfirst.pims.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


public final class FormUtil{
    private FormUtil() { }
    public static JPanel formCard(int width) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(10,11,10,11)));
        card.setPreferredSize(new Dimension(width, 0));
        return card;
    }
    public static void addTitle(JPanel card, String text){
        JLabel title = new JLabel(text);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 6;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0,4,8,4);
        card.add(title, gc);
    }
    public static void addField(JPanel card, String labelText, JComponent input, int x, int y, int span){
        addField(card, labelText, input, x, y, span,38);
    }
    public static void addField(JPanel card, String labelText, JComponent input, int x, int y, int span, int height){
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.SMALL.deriveFont(Font.BOLD));
        label.setForeground(Theme.MUTED);
        input.setPreferredSize(new Dimension(input.getPreferredSize().width, height));
        input.setMinimumSize(new Dimension(0, height));
        JPanel cell = new JPanel(new BorderLayout(0,3));
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
        gc.insets = new Insets(0,4,8,4);
        card.add(cell, gc);
    }
    public static void addNote(JPanel card, String text, int y) {
        JLabel note = new JLabel("<html>" + text + "</html>");
        note.setFont(Theme.SMALL);
        note.setForeground(Theme.MUTED);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = y;
        gc.gridwidth = 6;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0,4,8,4);
        card.add(note, gc);
    }
    public static void addButtons(JPanel card, int y, JButton add, JButton update, JButton delete, JButton clear, JLabel status) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.gridwidth = 3;
        gc.insets = new Insets(4,4,4,4);
        gc.gridy = y; gc.gridx = 0; card.add(add, gc);
        gc.gridy = y; gc.gridx = 3; card.add(update, gc);
        gc.gridy = y + 1; gc.gridx = 0; card.add(delete, gc);
        gc.gridy = y + 1; gc.gridx = 3; card.add(clear, gc);
        status.setFont(Theme.SMALL);
        gc.gridy = y + 2; gc.gridx = 0; gc.gridwidth = 6;
        gc.insets = new Insets(6,4,0,4);
        card.add(status, gc);
        gc.gridy = y + 3;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        card.add(filler, gc);
    }
    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(Theme.BODY);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width,38));
    }
}
