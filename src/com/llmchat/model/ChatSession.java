package com.llmchat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 聊天会话模型
 */
public class ChatSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private long createdAt;
    private long updatedAt;
    private List<ChatMessage> messages;
    
    public ChatSession() {
        this.id = UUID.randomUUID().toString();
        this.title = "新对话";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.messages = new ArrayList<>();
    }
    
    public ChatSession(String title) {
        this();
        this.title = title;
    }
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        this.updatedAt = System.currentTimeMillis();
    }
    
    public void clearMessages() {
        this.messages.clear();
        this.updatedAt = System.currentTimeMillis();
    }
    
    public int getMessageCount() {
        return messages.size();
    }
    
    /**
     * 获取第一个用户消息作为标题
     */
    public String getFirstMessagePreview() {
        for (ChatMessage msg : messages) {
            if (msg.getRole() == ChatMessage.Role.USER) {
                String content = msg.getContent();
                if (content.length() > 20) {
                    return content.substring(0, 20) + "...";
                }
                return content;
            }
        }
        return "新对话";
    }
}
