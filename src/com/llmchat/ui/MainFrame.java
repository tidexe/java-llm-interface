package com.llmchat.ui;

import com.llmchat.config.ChatHistoryManager;
import com.llmchat.config.ConfigManager;
import com.llmchat.model.ChatSession;
import com.llmchat.ui.components.ChatListPanel;
import com.llmchat.ui.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 主窗口 - 应用程序主界面
 */
public class MainFrame extends JFrame {
    private static final String APP_TITLE = "LLM Chat";
    private static final Color HEADER_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    
    private final ConfigManager configManager;
    private final ChatHistoryManager historyManager;
    
    private ChatPanel chatPanel;
    private ChatListPanel chatListPanel;
    
    public MainFrame() {
        this.configManager = new ConfigManager();
        this.historyManager = new ChatHistoryManager();
        
        initializeUI();
        setupWindowListener();
    }
    
    private void initializeUI() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 头部工具栏
        JPanel headerPanel = createHeaderPanel();
        
        // 内容面板（侧边栏 + 聊天面板）
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // 侧边栏
        chatListPanel = new ChatListPanel();
        chatListPanel.setChatListListener(new ChatListPanel.ChatListListener() {
            @Override
            public void onSessionSelected(String sessionId) {
                chatPanel.loadSession(sessionId);
                refreshChatList();
            }
            
            @Override
            public void onSessionDeleted(String sessionId) {
                historyManager.deleteSession(sessionId);
                String firstSessionId = historyManager.getFirstSessionId();
                chatPanel.loadSession(firstSessionId);
                refreshChatList();
            }
            
            @Override
            public void onNewChatClicked() {
                ChatSession newSession = historyManager.createNewSession();
                chatPanel.loadSession(newSession.getId());
                refreshChatList();
            }
        });
        
        // 聊天面板
        chatPanel = new ChatPanel(configManager, historyManager);
        chatPanel.setChatPanelListener(() -> refreshChatList());
        
        // 分隔线
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatListPanel, chatPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setBackground(new Color(220, 220, 220));
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
        
        // 初始化聊天列表
        refreshChatList();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_COLOR);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // 左侧标题
        JLabel titleLabel = new JLabel(APP_TITLE);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        // 右侧按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(HEADER_COLOR);
        
        // 设置按钮
        JButton settingsButton = new JButton("设置");
        settingsButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        settingsButton.setPreferredSize(new Dimension(80, 32));
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(this, configManager);
            dialog.setVisible(true);
        });
        
        buttonPanel.add(settingsButton);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        // 添加分隔线
        JPanel separatorPanel = new JPanel(new BorderLayout());
        separatorPanel.setBackground(HEADER_COLOR);
        separatorPanel.add(panel, BorderLayout.CENTER);
        
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(220, 220, 220));
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(HEADER_COLOR);
        wrapperPanel.add(separatorPanel, BorderLayout.CENTER);
        wrapperPanel.add(separator, BorderLayout.SOUTH);
        
        return wrapperPanel;
    }
    
    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "确定要退出应用程序吗？",
                    "退出",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    historyManager.saveHistory();
                    dispose();
                    System.exit(0);
                }
            }
        });
    }
    
    /**
     * 刷新聊天列表
     */
    private void refreshChatList() {
        List<ChatSession> sessions = historyManager.getAllSessions();
        String currentSessionId = chatPanel.getCurrentSessionId();
        chatListPanel.updateChatList(sessions, currentSessionId);
    }
}
