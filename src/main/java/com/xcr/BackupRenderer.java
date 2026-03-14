package com.xcr;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;

public class BackupRenderer extends DefaultListCellRenderer {
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public Component getListCellRendererComponent(JList<?> list, Object value, int idx, boolean sel, boolean focus) {
        super.getListCellRendererComponent(list, value, idx, sel, focus);
        setBackground(sel ? App.ACCENT_BLUE : App.BG_CARD);
        setForeground(App.TEXT_PRIMARY);
        setOpaque(true);
        if (value instanceof BackupEntry) {
            BackupEntry e = (BackupEntry) value;
            String date = sdf.format(new Date(e.time));
            String size = e.size < 1024 ? e.size + " B" : e.size < 1024*1024 ? String.format("%.1f KB", e.size/1024.0) : String.format("%.1f MB", e.size/(1024.0*1024.0));
            setText("<html><b style='color:#f8fafc'>" + e.name + "</b><br><span style='color:#94a3b8'>" + date + " \u2022 " + size + "</span></html>");
        }
        setBorder(new EmptyBorder(14, 18, 14, 18));
        return this;
    }
}