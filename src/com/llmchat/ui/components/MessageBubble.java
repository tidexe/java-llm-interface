package com.llmchat.ui.components;

import com.llmchat.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.plaf.TextUI;

/**
 * 消息气泡组件 - 现代化设计风格
 */
public class MessageBubble extends JPanel {
    private static final Color USER_COLOR = new Color(0, 122, 255);
    private static final Color USER_TEXT_COLOR = Color.WHITE;
    private static final Color ASSISTANT_COLOR = new Color(240, 240, 240);
    private static final Color ASSISTANT_TEXT_COLOR = new Color(30, 30, 30);
    private static final int MAX_WIDTH = 500;
    
    private final ChatMessage message;
    private final JTextPane textPane;
    private Style messageStyle;
    
    public MessageBubble(ChatMessage message) {
        this.message = message;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4, 8, 8, 12));
        
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        
        // 设置支持 emoji 的字体
        // Windows: Segoe UI Emoji, macOS: Apple Color Emoji, Linux: Noto Color Emoji
        Font emojiFont = createEmojiFont();
        
        // 设置文本和样式
        StyledDocument doc = textPane.getStyledDocument();
        messageStyle = textPane.addStyle("MessageStyle", null);
        StyleConstants.setFontFamily(messageStyle, emojiFont.getFamily());
        StyleConstants.setFontSize(messageStyle, 14);
        
        if (message.getRole() == ChatMessage.Role.USER) {
            StyleConstants.setForeground(messageStyle, USER_TEXT_COLOR);
        } else {
            StyleConstants.setForeground(messageStyle, ASSISTANT_TEXT_COLOR);
        }
        
        try {
            doc.insertString(0, message.getContent(), messageStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        // 直接添加文本面板，不使用滚动面板
        add(textPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建支持 emoji 的字体
     */
    private Font createEmojiFont() {
        // 尝试使用支持 emoji 的字体
        String[] fontNames = {
            "Segoe UI Emoji",      // Windows
            "Apple Color Emoji",   // macOS
            "Noto Color Emoji",    // Linux
            "Segoe UI Symbol",     // Windows fallback
            "Symbola",             // 通用
            "Microsoft YaHei",     // 中文 + 部分 emoji
            "Dialog"               // 系统默认
        };
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.PLAIN, 14);
            // 检查字体是否可用
            if (!font.getFamily().equals("Dialog") || fontName.equals("Dialog")) {
                return font;
            }
        }
        
        return new Font("Microsoft YaHei", Font.PLAIN, 14);
    }
    
    /**
     * 追加文本内容（调用方需确保在 EDT 线程中执行）
     */
    public void appendContent(String text) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            doc.insertString(doc.getLength(), text, messageStyle);
            
            // 强制重新计算布局
            revalidate();
            repaint();
            
            // 通知父容器更新布局链
            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int arc = 16;
        
        // 绘制圆角背景
        if (message.getRole() == ChatMessage.Role.USER) {
            g2.setColor(USER_COLOR);
        } else {
            g2.setColor(ASSISTANT_COLOR);
        }
        
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, arc, arc);
        g2.fill(roundRect);
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    @Override
    public Dimension getPreferredSize() {
        int maxWidth = MAX_WIDTH - 24; // 减去左右padding（EmptyBorder水平各12px）
        
        // 使用 View 层级精确计算换行后的文本尺寸
        int textWidth;
        int textHeight;
        
        try {
            // 设置宽度约束
            textPane.setSize(maxWidth, Integer.MAX_VALUE);
            // 【关键修复】setSize() 不会同步触发 View 层级更新（只标记 invalid），
            // 必须显式调用 doLayout() 强制 JTextPane 立即按新宽度计算文本换行
            textPane.doLayout();
            
            TextUI ui = textPane.getUI();
            View rootView = ui.getRootView(textPane);
            // Y_AXIS：按 maxWidth 约束计算换行后的实际高度
            textHeight = (int) rootView.getPreferredSpan(View.Y_AXIS);
            // X_AXIS：获取文本实际宽度
            textWidth = (int) Math.ceil(rootView.getPreferredSpan(View.X_AXIS));
        } catch (Exception e) {
            // 降级方案
            textPane.setSize(maxWidth, Integer.MAX_VALUE);
            Dimension textPrefSize = textPane.getPreferredSize();
            textWidth = textPrefSize.width;
            textHeight = textPrefSize.height;
        }
        
        // 计算实际宽度（取文本宽度和最大宽度的较小值）
        int actualWidth = Math.min(textWidth, maxWidth);
        // 保底高度：至少显示一行文本（防止 View 层级未初始化时高度为 0）
        FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
        int minHeight = fm != null ? fm.getHeight() : 16;
        int actualHeight = Math.max(textHeight, minHeight);
        
        // 加上padding：水平24px（左右各12px），垂直16px（上下各8px）
        return new Dimension(actualWidth + 24, actualHeight + 16);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
