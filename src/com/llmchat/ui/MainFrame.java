package com.llmchat.ui;

import com.llmchat.config.ConfigManager;
import com.llmchat.ui.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 主窗口 - 应用程序主界面
 */
public class MainFrame extends JFrame {
    private static final String APP_TITLE = "LLM Chat";
    private static final Color HEADER_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    
    private final ConfigManager configManager;
    private ChatPanel chatPanel;
    
    public MainFrame() {
        this.configManager = new ConfigManager();
        
        initializeUI();
        setupWindowListener();
    }
    
    private void initializeUI() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(null);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 头部工具栏
        JPanel headerPanel = createHeaderPanel();
        
        // 聊天面板
        chatPanel = new ChatPanel(configManager);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
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
        
        // 新对话按钮
        JButton newChatButton = new JButton("新对话");
        newChatButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        newChatButton.setPreferredSize(new Dimension(80, 32));
        newChatButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newChatButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要开始新对话吗？当前对话将被清空。",
                "新对话",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                chatPanel.clearChat();
            }
        });
        
        // 设置按钮
        JButton settingsButton = new JButton("设置");
        settingsButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        settingsButton.setPreferredSize(new Dimension(80, 32));
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(this, configManager);
            dialog.setVisible(true);
        });
        
        buttonPanel.add(newChatButton);
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
                    dispose();
                    System.exit(0);
                }
            }
        });
    }
}
