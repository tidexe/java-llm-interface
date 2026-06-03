package com.llmchat.ui.components;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 现代化按钮组件
 */
public class ModernButton extends JButton {
    private static final Color PRIMARY_COLOR = new Color(0, 122, 255);
    private static final Color HOVER_COLOR = new Color(0, 102, 204);
    private static final Color PRESSED_COLOR = new Color(0, 82, 184);
    
    private boolean isHovered = false;
    private boolean isPressed = false;
    
    public ModernButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(80, 36));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int arc = 8;
        
        // 选择颜色
        Color bgColor;
        if (isPressed) {
            bgColor = PRESSED_COLOR;
        } else if (isHovered) {
            bgColor = HOVER_COLOR;
        } else {
            bgColor = PRIMARY_COLOR;
        }
        
        // 绘制圆角背景
        g2.setColor(bgColor);
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, arc, arc);
        g2.fill(roundRect);
        
        g2.dispose();
        super.paintComponent(g);
    }
}
