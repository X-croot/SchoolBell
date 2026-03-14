package com.xcr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class LessonRow extends JPanel {
    JTextField studentField, teacherField, breakField;
    int num;

    LessonRow(int n) {
        num = n;
        setLayout(new GridLayout(1, 4, 18, 0));
        setBackground(n % 2 == 0 ? App.BG_CARD : App.BG_CARD_LIGHT);
        setOpaque(true);
        setBorder(new EmptyBorder(12, 18, 12, 18));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel lbl = new JLabel(String.valueOf(n), SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(App.ACCENT_BLUE);
        lbl.setBorder(BorderFactory.createLineBorder(App.ACCENT_BLUE.darker(), 1, true));
        add(lbl);

        studentField = createTimeField();
        teacherField = createTimeField();
        breakField = createTimeField();

        add(studentField);
        add(teacherField);
        add(breakField);
    }

    JTextField createTimeField() {
        JTextField f = new JTextField("08:00");
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setFont(new Font("JetBrains Mono", Font.BOLD, 15));
        f.setBackground(App.BG_INPUT);
        f.setForeground(App.TEXT_PRIMARY);
        f.setCaretColor(App.ACCENT_CYAN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(App.BORDER_LIGHT, 2, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBackground(App.BG_INPUT_FOCUS);
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(App.ACCENT_BLUE, 2, true),
                    new EmptyBorder(10, 14, 10, 14)
                ));
            }
            public void focusLost(FocusEvent e) {
                f.setBackground(App.BG_INPUT);
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(App.BORDER_LIGHT, 2, true),
                    new EmptyBorder(10, 14, 10, 14)
                ));
            }
        });

        // Boyutları büyütülmüş saat kutucukları
        f.setPreferredSize(new Dimension(100, 40));  // Yeni genişlik 100px, yükseklik 40px
        return f;
    }

    LessonInfo getData() { 
        return new LessonInfo(studentField.getText(), teacherField.getText(), breakField.getText()); 
    }
    
    void setData(LessonInfo d) { 
        studentField.setText(d.student); 
        teacherField.setText(d.teacher); 
        breakField.setText(d.breakTime); 
    }
}