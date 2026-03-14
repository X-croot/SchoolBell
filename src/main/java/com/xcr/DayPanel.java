package com.xcr;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;

public class DayPanel extends JPanel {
    List<LessonRow> rows = new ArrayList<>();
    JPanel container;

    DayPanel(String name, int idx) {
        setLayout(new BorderLayout());
        setBackground(App.BG_MAIN);
        setOpaque(true);

        JPanel header = new JPanel(new GridLayout(1, 4, 18, 0));
        header.setBackground(App.BG_CARD);
        header.setOpaque(true);
        header.setBorder(new EmptyBorder(16, 18, 16, 18));
        header.add(createHeaderLabel("Lesson"));
        header.add(createHeaderLabel("\ud83c\udf93 Student"));
        header.add(createHeaderLabel("\ud83d\udc68\u200d\ud83c\udfeb Teacher"));
        header.add(createHeaderLabel("\u2615 Break"));
        add(header, BorderLayout.NORTH);

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(App.BG_MAIN);
        container.setOpaque(true);

        JScrollPane scroll = App.createScrollPane(container);
        add(scroll, BorderLayout.CENTER);

        for (int i = 0; i < 8; i++) addRow();
    }

    JLabel createHeaderLabel(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(App.TEXT_PRIMARY);
        return l;
    }

    void addRow() { LessonRow r = new LessonRow(rows.size() + 1); rows.add(r); container.add(r); container.revalidate(); }
    void removeRow() { if (!rows.isEmpty()) { container.remove(rows.remove(rows.size() - 1)); container.revalidate(); container.repaint(); } }
    void clearAll() { rows.clear(); container.removeAll(); container.revalidate(); container.repaint(); }

    List<LessonInfo> getData() { List<LessonInfo> list = new ArrayList<>(); for (LessonRow r : rows) list.add(r.getData().copy()); return list; }
    void setData(List<LessonInfo> data) { clearAll(); for (LessonInfo d : data) { LessonRow r = new LessonRow(rows.size() + 1); r.setData(d); rows.add(r); container.add(r); } container.revalidate(); }

    JSONArray toJSON() { JSONArray a = new JSONArray(); for (LessonRow r : rows) { JSONObject o = new JSONObject(); o.put("student", r.studentField.getText()); o.put("teacher", r.teacherField.getText()); o.put("break", r.breakField.getText()); a.put(o); } return a; }
    void fromJSON(JSONArray a) { List<LessonInfo> data = new ArrayList<>(); for (int i = 0; i < a.length(); i++) { JSONObject o = a.getJSONObject(i); data.add(new LessonInfo(o.optString("student", "08:00"), o.optString("teacher", "08:05"), o.optString("break", "08:45"))); } setData(data); }
}