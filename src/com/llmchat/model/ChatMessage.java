package com.llmchat.model;

import java.io.Serializable;

/**
 * 聊天消息模型
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Role {
        USER, ASSISTANT, SYSTEM
    }
    
    private Role role;
    private String content;
    private long timestamp;
    
    public ChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Role getRole() {
        return role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getRoleString() {
        return role.name().toLowerCase();
    }
}
