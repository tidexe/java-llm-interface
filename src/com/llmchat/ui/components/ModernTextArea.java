package com.llmchat.ui.components;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * 现代化输入文本区域组件
 */
public class ModernTextArea extends JScrollPane {
    private final JTextArea textArea;
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color FOCUS_COLOR = new Color(0, 122, 255);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    public ModernTextArea(int rows, int columns) {
        textArea = new JTextArea(rows, columns);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        textArea.setBackground(BACKGROUND_COLOR);
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        setViewportView(textArea);
        setBorder(new LineBorder(BORDER_COLOR, 1, true));
        setBackground(BACKGROUND_COLOR);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 焦点监听器
        textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(new LineBorder(FOCUS_COLOR, 2, true));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                setBorder(new LineBorder(BORDER_COLOR, 1, true));
            }
        });
    }
    
    public String getText() {
        return textArea.getText();
    }
    
    public void setText(String text) {
        textArea.setText(text);
    }
    
    public void append(String text) {
        textArea.append(text);
    }
    
    public void clear() {
        textArea.setText("");
    }
    
    public JTextArea getTextArea() {
        return textArea;
    }
}
