package com.llmchat.ui.components;

import com.llmchat.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

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
    
    public MessageBubble(ChatMessage message) {
        this.message = message;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 8, 12));
        
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        
        // 设置支持 emoji 的字体
        // Windows: Segoe UI Emoji, macOS: Apple Color Emoji, Linux: Noto Color Emoji
        Font emojiFont = createEmojiFont();
        
        // 设置文本和样式
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("MessageStyle", null);
        StyleConstants.setFontFamily(style, emojiFont.getFamily());
        StyleConstants.setFontSize(style, 14);
        
        if (message.getRole() == ChatMessage.Role.USER) {
            StyleConstants.setForeground(style, USER_TEXT_COLOR);
        } else {
            StyleConstants.setForeground(style, ASSISTANT_TEXT_COLOR);
        }
        
        try {
            doc.insertString(0, message.getContent(), style);
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
     * 追加文本内容
     */
    public void appendContent(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = textPane.getStyledDocument();
                Style style = textPane.getStyle("MessageStyle");
                doc.insertString(doc.getLength(), text, style);
                // 触发重绘以更新显示
                textPane.revalidate();
                textPane.repaint();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
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
        // 先设置文本区域的宽度以计算换行
        FontMetrics fm = getFontMetrics(textPane.getFont());
        int charWidth = fm.charWidth('M');
        int maxWidth = MAX_WIDTH - 24; // 减去左右padding
        
        // 设置文本区域大小来计算换行后的高度
        textPane.setSize(maxWidth, Integer.MAX_VALUE);
        Dimension textPrefSize = textPane.getPreferredSize();
        
        // 计算实际宽度（取文本宽度和最大宽度的较小值）
        int actualWidth = Math.min(textPrefSize.width, maxWidth);
        int actualHeight = textPrefSize.height;
        
        // 加上padding
        return new Dimension(actualWidth + 24, actualHeight + 16);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
