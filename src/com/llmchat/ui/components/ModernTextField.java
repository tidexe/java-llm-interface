package com.llmchat.ui.components;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * 现代化文本输入框组件
 */
public class ModernTextField extends JTextField {
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color FOCUS_COLOR = new Color(0, 122, 255);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    public ModernTextField(int columns) {
        super(columns);
        setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(FOCUS_COLOR, 2, true),
                    BorderFactory.createEmptyBorder(5, 9, 5, 9)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        });
    }
    
    public ModernTextField() {
        this(20);
    }
}
