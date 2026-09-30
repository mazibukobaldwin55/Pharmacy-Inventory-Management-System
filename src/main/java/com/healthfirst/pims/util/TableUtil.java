package com.healthfirst.pims.util;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;


public final class TableUtil {
    private TableUtil() { }
    public static final Color SELECTION = new Color(0xDDD6FE);
    public static void styleTable(JTable table){
        table.setFont(Theme.BODY);
        table.setForeground(Theme.TEXT);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xE5E7EB));
        table.setIntercellSpacing(new Dimension(0,1));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(Theme.TEXT);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0,36));
        header.setDefaultRenderer(new HeaderRenderer());
    }
    public static void padCells(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, false, row, col);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                setForeground(Theme.TEXT);
                setHorizontalAlignment(LEFT);
                if (selected) {
                    setBackground(t.getSelectionBackground());
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF9FAFB));
                }
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
    }
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col){
            String text = value == null ? "" : value.toString();
            RowSorter<?> sorter = table.getRowSorter();
            if (sorter != null && !sorter.getSortKeys().isEmpty()) {
                RowSorter.SortKey key = sorter.getSortKeys().getFirst();
                if (key.getColumn() == table.convertColumnIndexToModel(col)) {
                    text += key.getSortOrder() == SortOrder.ASCENDING ? "▲" : "▼";
                }
            }
            super.getTableCellRendererComponent(table, text,false,false, row, col);
            setBackground(Theme.PRIMARY);
            setForeground(Color.WHITE);
            setFont(Theme.BOLD);
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
            return this;
        }
    }
}
