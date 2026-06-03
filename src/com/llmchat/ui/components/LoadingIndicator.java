package com.llmchat.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * 加载指示器组件 - 动画效果
 */
public class LoadingIndicator extends JPanel {
    private static final Color DOT_COLOR = new Color(0, 122, 255);
    private static final int DOT_COUNT = 3;
    private static final int DOT_SIZE = 8;
    private static final int DOT_SPACING = 16;
    
    private int currentDot = 0;
    private Timer timer;
    private boolean running = false;
    
    public LoadingIndicator() {
        setOpaque(false);
        int width = DOT_COUNT * DOT_SIZE + (DOT_COUNT - 1) * DOT_SPACING;
        setPreferredSize(new Dimension(width + 20, DOT_SIZE + 10));
        
        timer = new Timer(200, e -> {
            currentDot = (currentDot + 1) % DOT_COUNT;
            repaint();
        });
    }
    
    /**
     * 开始动画
     */
    public void start() {
        if (!running) {
            running = true;
            currentDot = 0;
            timer.start();
        }
    }
    
    /**
     * 停止动画
     */
    public void stop() {
        running = false;
        timer.stop();
        currentDot = 0;
        repaint();
    }
    
    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!running) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int startX = (getWidth() - (DOT_COUNT * DOT_SIZE + (DOT_COUNT - 1) * DOT_SPACING)) / 2;
        int startY = (getHeight() - DOT_SIZE) / 2;
        
        for (int i = 0; i < DOT_COUNT; i++) {
            int x = startX + i * (DOT_SIZE + DOT_SPACING);
            
            // 计算透明度
            float alpha;
            if (i == currentDot) {
                alpha = 1.0f;
            } else if (i == (currentDot - 1 + DOT_COUNT) % DOT_COUNT) {
                alpha = 0.6f;
            } else {
                alpha = 0.3f;
            }
            
            g2.setColor(new Color(DOT_COLOR.getRed(), DOT_COLOR.getGreen(), 
                                  DOT_COLOR.getBlue(), (int)(alpha * 255)));
            
            Ellipse2D dot = new Ellipse2D.Float(x, startY, DOT_SIZE, DOT_SIZE);
            g2.fill(dot);
        }
        
        g2.dispose();
    }
}
