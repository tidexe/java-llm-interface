package com.llmchat.ui.components;

import com.llmchat.model.ChatSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * 聊天列表组件 - 侧边栏显示所有聊天会话
 */
public class ChatListPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ITEM_BACKGROUND = new Color(255, 255, 255);
    private static final Color SELECTED_BACKGROUND = new Color(220, 230, 245);
    private static final Color HOVER_BACKGROUND = new Color(235, 235, 235);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color SUBTEXT_COLOR = new Color(130, 130, 130);
    
    private JPanel listContainer;
    private List<ChatSession> sessions;
    private String selectedSessionId;
    private ChatListListener listener;
    
    public interface ChatListListener {
        void onSessionSelected(String sessionId);
        void onSessionDeleted(String sessionId);
        void onNewChatClicked();
    }
    
    public ChatListPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(250, 0));
        
        initializeUI();
    }
    
    private void initializeUI() {
        // 标题栏
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("聊天记录");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // 新建聊天按钮
        ModernButton newChatButton = new ModernButton("+ 新对话");
        newChatButton.setPreferredSize(new Dimension(90, 32));
        newChatButton.addActionListener(e -> {
            if (listener != null) {
                listener.onNewChatClicked();
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(newChatButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // 聊天列表
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BACKGROUND_COLOR);
        listContainer.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 设置聊天列表监听器
     */
    public void setChatListListener(ChatListListener listener) {
        this.listener = listener;
    }
    
    /**
     * 更新聊天列表
     */
    public void updateChatList(List<ChatSession> sessions, String selectedSessionId) {
        this.sessions = sessions;
        this.selectedSessionId = selectedSessionId;
        refreshList();
    }
    
    /**
     * 刷新列表显示
     */
    private void refreshList() {
        listContainer.removeAll();
        
        if (sessions == null || sessions.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无聊天记录");
            emptyLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            emptyLabel.setForeground(SUBTEXT_COLOR);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBorder(new EmptyBorder(30, 0, 30, 0));
            listContainer.add(emptyLabel);
        } else {
            for (ChatSession session : sessions) {
                ChatItemPanel item = new ChatItemPanel(session, session.getId().equals(selectedSessionId));
                listContainer.add(item);
                listContainer.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        listContainer.revalidate();
        listContainer.repaint();
    }
    
    /**
     * 聊天列表项组件
     */
    private class ChatItemPanel extends JPanel {
        private final ChatSession session;
        private final boolean isSelected;
        private boolean isHovered = false;
        
        public ChatItemPanel(ChatSession session, boolean isSelected) {
            this.session = session;
            this.isSelected = isSelected;
            
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            
            // 鼠标事件
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (listener != null && !isSelected) {
                        listener.onSessionSelected(session.getId());
                    }
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
            
            // 标题
            JLabel titleLabel = new JLabel(session.getTitle());
            titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            titleLabel.setForeground(TEXT_COLOR);
            
            // 副标题（消息数量和时间）
            String subtitle = session.getMessageCount() + " 条消息";
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
            subtitleLabel.setForeground(SUBTEXT_COLOR);
            
            // 删除按钮
            JButton deleteButton = new JButton("×");
            deleteButton.setFont(new Font("Arial", Font.BOLD, 16));
            deleteButton.setForeground(SUBTEXT_COLOR);
            deleteButton.setBackground(new Color(0, 0, 0, 0));
            deleteButton.setBorder(null);
            deleteButton.setFocusPainted(false);
            deleteButton.setContentAreaFilled(false);
            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteButton.setPreferredSize(new Dimension(24, 24));
            deleteButton.addActionListener(e -> {
                if (listener != null) {
                    int result = JOptionPane.showConfirmDialog(
                        ChatListPanel.this,
                        "确定要删除这个对话吗？",
                        "删除确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        listener.onSessionDeleted(session.getId());
                    }
                }
            });
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 3));
            textPanel.setBackground(new Color(0, 0, 0, 0));
            textPanel.setOpaque(false);
            textPanel.add(titleLabel);
            textPanel.add(subtitleLabel);
            
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightPanel.setBackground(new Color(0, 0, 0, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(deleteButton);
            
            add(textPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color bgColor;
            if (isSelected) {
                bgColor = SELECTED_BACKGROUND;
            } else if (isHovered) {
                bgColor = HOVER_BACKGROUND;
            } else {
                bgColor = ITEM_BACKGROUND;
            }
            
            g2.setColor(bgColor);
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.fill(roundRect);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
