package com.llmchat.ui.components;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * 现代化按钮组件
 */
public class ModernButton extends JButton {
    private static final Color PRIMARY_COLOR = new Color(0, 122, 255);
    private static final Color HOVER_COLOR = new Color(0, 102, 204);
    private static final Color PRESSED_COLOR = new Color(0, 82, 184);
    private static final int CORNER_RADIUS = 18; // 统一圆角半径
    
    private boolean isHovered = false;
    private boolean isPressed = false;
    
    // Loading state
    private boolean loading = false;
    private Timer loadingTimer;
    private int currentDot = 0;
    private static final int DOT_COUNT = 3;
    private static final int DOT_SIZE = 6;
    private static final int DOT_SPACING = 10;
    
    public ModernButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(80, 36));
        
        // Loading timer
        loadingTimer = new Timer(200, e -> {
            currentDot = (currentDot + 1) % DOT_COUNT;
            repaint();
        });
        
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
    
    /**
     * 设置加载状态
     */
    public void setLoading(boolean loading) {
        if (this.loading == loading) return;
        this.loading = loading;
        if (loading) {
            currentDot = 0;
            loadingTimer.start();
            setEnabled(false);
        } else {
            loadingTimer.stop();
            setEnabled(true);
            currentDot = 0;
        }
        repaint();
    }
    
    /**
     * 检查是否正在加载
     */
    public boolean isLoading() {
        return loading;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int arc = CORNER_RADIUS;
        
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
        
        if (loading) {
            // 绘制加载动画点
            drawLoadingDots(g2, width, height);
        } else {
            // 正常绘制文字
            g2.dispose();
            super.paintComponent(g);
        }
        
        g2.dispose();
    }
    
    private void drawLoadingDots(Graphics2D g2, int width, int height) {
        int totalWidth = DOT_COUNT * DOT_SIZE + (DOT_COUNT - 1) * DOT_SPACING;
        int startX = (width - totalWidth) / 2;
        int startY = (height - DOT_SIZE) / 2;
        
        for (int i = 0; i < DOT_COUNT; i++) {
            int x = startX + i * (DOT_SIZE + DOT_SPACING);
            
            float alpha;
            if (i == currentDot) {
                alpha = 1.0f;
            } else if (i == (currentDot - 1 + DOT_COUNT) % DOT_COUNT) {
                alpha = 0.6f;
            } else {
                alpha = 0.3f;
            }
            
            g2.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
            Ellipse2D dot = new Ellipse2D.Float(x, startY, DOT_SIZE, DOT_SIZE);
            g2.fill(dot);
        }
    }
}
