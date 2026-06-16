package com.llmchat.ui;

import com.llmchat.config.ChatHistoryManager;
import com.llmchat.config.ConfigManager;
import com.llmchat.model.ChatMessage;
import com.llmchat.model.ChatSession;
import com.llmchat.model.Config;
import com.llmchat.http.HttpClient;
import com.llmchat.ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天面板 - 核心聊天界面
 */
public class ChatPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    
    private final ConfigManager configManager;
    private final ChatHistoryManager historyManager;
    private final HttpClient httpClient;
    
    private String currentSessionId;
    private List<ChatMessage> messageHistory;
    
    private JPanel messagesPanel;
    private JScrollPane messagesScrollPane;
    private ModernTextArea inputArea;
    private ModernButton sendButton;
    private MessageBubble currentResponseBubble;
    private JPanel currentBubbleWrapper;
    private StringBuilder currentResponse;
    
    private ChatPanelListener listener;
    
    public interface ChatPanelListener {
        void onSessionUpdated();
    }
    
    public ChatPanel(ConfigManager configManager, ChatHistoryManager historyManager) {
        this.configManager = configManager;
        this.historyManager = historyManager;
        this.httpClient = new HttpClient();
        this.messageHistory = new ArrayList<>();
        this.currentResponse = new StringBuilder();
        
        initializeUI();
        
        // 加载第一个会话
        loadSession(historyManager.getFirstSessionId());
    }
    
    public void setChatPanelListener(ChatPanelListener listener) {
        this.listener = listener;
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 消息显示区域
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BACKGROUND_COLOR);
        messagesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        messagesScrollPane = new JScrollPane(messagesPanel);
        messagesScrollPane.setBackground(BACKGROUND_COLOR);
        messagesScrollPane.setBorder(null);
        messagesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // 输入区域
        JPanel inputPanel = createInputPanel();
        
        add(messagesScrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // 输入文本区域
        inputArea = new ModernTextArea(3, 40);
        inputArea.getTextArea().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendMessage();
                }
            }
        });
        
        // 右侧面板（发送按钮）
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(BACKGROUND_COLOR);
        
        sendButton = new ModernButton("发送");
        sendButton.addActionListener(e -> {
            if (sendButton.isLoading()) {
                // 正在加载时，点击按钮取消请求
                cancelCurrentRequest();
            } else {
                sendMessage();
            }
        });
        
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(sendButton);
        rightPanel.add(Box.createVerticalGlue());
        
        panel.add(inputArea, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 加载指定会话
     */
    public void loadSession(String sessionId) {
        if (sessionId == null) return;
        
        // 如果正在加载，先取消当前请求
        if (sendButton.isLoading()) {
            httpClient.cancel();
            // 等待取消完成后再加载新会话（简化处理：直接重置按钮状态）
            resetSendButton();
            currentResponseBubble = null;
            currentBubbleWrapper = null;
        }
        
        this.currentSessionId = sessionId;
        ChatSession session = historyManager.getSession(sessionId);
        
        if (session != null) {
            this.messageHistory = new ArrayList<>(session.getMessages());
            displayMessages();
        } else {
            this.messageHistory = new ArrayList<>();
            displayWelcomeMessage();
        }
    }
    
    /**
     * 显示消息列表
     */
    private void displayMessages() {
        messagesPanel.removeAll();
        
        if (messageHistory.isEmpty()) {
            displayWelcomeMessage();
        } else {
            for (ChatMessage msg : messageHistory) {
                addMessageBubble(msg, msg.getRole() == ChatMessage.Role.USER);
            }
        }
        
        messagesPanel.revalidate();
        messagesPanel.repaint();
        scrollToBottom();
    }
    
    private void displayWelcomeMessage() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(BACKGROUND_COLOR);
        welcomePanel.setBorder(new EmptyBorder(50, 0, 0, 0));
        
        JLabel welcomeLabel = new JLabel("欢迎使用 LLM Chat");
        welcomeLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(100, 100, 100));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel hintLabel = new JLabel("请先配置API设置，然后开始对话");
        hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        hintLabel.setForeground(new Color(150, 150, 150));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setBackground(BACKGROUND_COLOR);
        textPanel.add(welcomeLabel);
        textPanel.add(hintLabel);
        
        welcomePanel.add(textPanel, BorderLayout.CENTER);
        
        messagesPanel.add(welcomePanel);
        messagesPanel.add(Box.createVerticalGlue());
    }
    
    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty() || sendButton.isLoading()) {
            return;
        }
        
        Config config = configManager.getConfig();
        if (!config.isValid()) {
            JOptionPane.showMessageDialog(this, 
                "请先配置API设置", 
                "配置错误", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 清除欢迎消息
        if (messageHistory.isEmpty()) {
            messagesPanel.removeAll();
            messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        }
        
        // 添加用户消息
        ChatMessage userMessage = new ChatMessage(ChatMessage.Role.USER, text);
        messageHistory.add(userMessage);
        historyManager.addMessageToSession(currentSessionId, userMessage);
        addMessageBubble(userMessage, true);
        
        // 通知会话更新
        if (listener != null) {
            listener.onSessionUpdated();
        }
        
        // 清空输入框
        inputArea.clear();
        
        // 显示加载状态（按钮变为“停止”）
        sendButton.setText("停止");
        sendButton.setLoading(true);
        
        // 准备接收响应
        currentResponse = new StringBuilder();
        ChatMessage assistantMessage = new ChatMessage(ChatMessage.Role.ASSISTANT, "");
        currentResponseBubble = new MessageBubble(assistantMessage);
        
        // 添加响应气泡
        currentBubbleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        currentBubbleWrapper.setBackground(BACKGROUND_COLOR);
        currentBubbleWrapper.add(currentResponseBubble);
        messagesPanel.add(currentBubbleWrapper);
        messagesPanel.revalidate();
        messagesPanel.repaint();
        scrollToBottom();
        
        // 发送请求
        List<ChatMessage> requestMessages = new ArrayList<>(messageHistory);
        httpClient.sendStreamRequest(config, requestMessages, new HttpClient.StreamCallback() {
            @Override
            public void onChunk(String chunk) {
                SwingUtilities.invokeLater(() -> {
                    currentResponse.append(chunk);
                    currentResponseBubble.appendContent(chunk);
                    messagesPanel.revalidate();
                    messagesPanel.repaint();
                    scrollToBottom();
                });
            }
            
            @Override
            public void onComplete() {
                SwingUtilities.invokeLater(() -> {
                    resetSendButton();
                    
                    // 保存完整的响应消息
                    ChatMessage completeMessage = new ChatMessage(
                        ChatMessage.Role.ASSISTANT, 
                        currentResponse.toString()
                    );
                    messageHistory.add(completeMessage);
                    historyManager.addMessageToSession(currentSessionId, completeMessage);
                    currentResponseBubble = null;
                    currentBubbleWrapper = null;
                    
                    // 通知会话更新
                    if (listener != null) {
                        listener.onSessionUpdated();
                    }
                });
            }
            
            @Override
            public void onCancelled() {
                SwingUtilities.invokeLater(() -> {
                    resetSendButton();
                    
                    String partialContent = currentResponse.toString();
                    if (!partialContent.isEmpty()) {
                        // 保留已接收的部分响应
                        ChatMessage partialMessage = new ChatMessage(
                            ChatMessage.Role.ASSISTANT,
                            partialContent
                        );
                        messageHistory.add(partialMessage);
                        historyManager.addMessageToSession(currentSessionId, partialMessage);
                    } else {
                        // 没有收到任何内容，移除空的气泡
                        if (currentBubbleWrapper != null) {
                            messagesPanel.remove(currentBubbleWrapper);
                            messagesPanel.revalidate();
                            messagesPanel.repaint();
                        }
                        // 移除用户消息
                        if (!messageHistory.isEmpty()) {
                            ChatMessage lastMsg = messageHistory.get(messageHistory.size() - 1);
                            if (lastMsg.getRole() == ChatMessage.Role.USER) {
                                messageHistory.remove(messageHistory.size() - 1);
                            }
                        }
                    }
                    
                    currentResponseBubble = null;
                    currentBubbleWrapper = null;
                    
                    if (listener != null) {
                        listener.onSessionUpdated();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    resetSendButton();
                    
                    // 移除空的响应气泡
                    if (currentBubbleWrapper != null) {
                        messagesPanel.remove(currentBubbleWrapper);
                        messagesPanel.revalidate();
                        messagesPanel.repaint();
                    }
                    
                    // 显示错误消息
                    JOptionPane.showMessageDialog(ChatPanel.this, 
                        error, 
                        "请求错误", 
                        JOptionPane.ERROR_MESSAGE);
                    
                    // 从历史中移除用户消息
                    if (!messageHistory.isEmpty()) {
                        messageHistory.remove(messageHistory.size() - 1);
                    }
                    
                    currentResponseBubble = null;
                    currentBubbleWrapper = null;
                });
            }
        });
    }
    
    /**
     * 取消当前正在进行的请求
     */
    private void cancelCurrentRequest() {
        httpClient.cancel();
    }
    
    /**
     * 重置发送按钮状态
     */
    private void resetSendButton() {
        sendButton.setText("发送");
        sendButton.setLoading(false);
    }
    
    private void addMessageBubble(ChatMessage message, boolean isUser) {
        JPanel bubbleWrapper = new JPanel(new FlowLayout(
            isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 5
        ));
        bubbleWrapper.setBackground(BACKGROUND_COLOR);
        
        MessageBubble bubble = new MessageBubble(message);
        bubbleWrapper.add(bubble);
        
        messagesPanel.add(bubbleWrapper);
        messagesPanel.revalidate();
        messagesPanel.repaint();
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = messagesScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }
    
    /**
     * 获取当前会话ID
     */
    public String getCurrentSessionId() {
        return currentSessionId;
    }
    
    /**
     * 刷新当前会话显示
     */
    public void refreshCurrentSession() {
        loadSession(currentSessionId);
    }
}
