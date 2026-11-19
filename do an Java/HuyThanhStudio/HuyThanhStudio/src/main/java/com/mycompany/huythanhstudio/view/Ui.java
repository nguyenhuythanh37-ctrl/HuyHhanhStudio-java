package com.mycompany.huythanhstudio.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

final class Ui {
    static final Color DARK = new Color(37,43,53);
    static final Color DARK2 = new Color(47,55,67);
    static final Color GOLD = new Color(174,139,91);
    static final Color BG = new Color(245,246,248);
    static final Color MUTED = new Color(120,127,138);
    static final Color BORDER = new Color(228,231,235);
    static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font BOLD = new Font("Segoe UI", Font.BOLD, 13);

    static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }
    static JPanel card() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(16,16,16,16)));
        return p;
    }
    static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(BOLD); b.setForeground(Color.WHITE); b.setBackground(GOLD);
        b.setFocusPainted(false); b.setBorder(new EmptyBorder(9,15,9,15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    static JTable table(Object[][] data, String[] columns) {
        JTable t = new JTable(data, columns) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        t.setRowHeight(34); t.setFont(FONT); t.setForeground(DARK);
        t.setGridColor(BORDER); t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(240,233,222));
        t.setSelectionForeground(DARK);
        t.getTableHeader().setFont(BOLD);
        t.getTableHeader().setBackground(new Color(247,248,250));
        t.getTableHeader().setForeground(DARK);
        t.getTableHeader().setPreferredSize(new Dimension(0,36));
        return t;
    }
    static JScrollPane scroll(JTable table) {
        JScrollPane s = new JScrollPane(table);
        s.setBorder(new LineBorder(BORDER,1));
        return s;
    }
    static JPanel page(String title, String desc) {
        JPanel p = new JPanel(new BorderLayout(0,18));
        p.setBackground(BG); p.setBorder(new EmptyBorder(25,28,25,28));
        JPanel h = new JPanel(); h.setOpaque(false);
        h.setLayout(new BoxLayout(h,BoxLayout.Y_AXIS));
        h.add(label(title,26,true,DARK));
        h.add(Box.createVerticalStrut(5));
        h.add(label(desc,13,false,MUTED));
        p.add(h,BorderLayout.NORTH);
        return p;
    }
    private Ui() {}
}
