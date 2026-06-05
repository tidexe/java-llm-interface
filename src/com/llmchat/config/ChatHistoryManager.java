package com.llmchat.config;

import com.llmchat.model.ChatMessage;
import com.llmchat.model.ChatSession;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天记录管理器 - 负责聊天会话的持久化存储
 */
public class ChatHistoryManager {
    private static final String HISTORY_DIR = System.getProperty("user.home") + "/.llmchat/history";
    private static final String INDEX_FILE = HISTORY_DIR + "/index.dat";
    
    private Map<String, ChatSession> sessions;
    private List<String> sessionOrder; // 保持会话顺序
    
    public ChatHistoryManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.sessionOrder = Collections.synchronizedList(new ArrayList<>());
        loadHistory();
    }
    
    /**
     * 加载历史记录
     */
    private void loadHistory() {
        Path indexFile = Paths.get(INDEX_FILE);
        if (Files.exists(indexFile)) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(INDEX_FILE))) {
                @SuppressWarnings("unchecked")
                List<String> loadedOrder = (List<String>) ois.readObject();
                sessionOrder.addAll(loadedOrder);
                
                for (String sessionId : loadedOrder) {
                    Path sessionFile = Paths.get(HISTORY_DIR + "/" + sessionId + ".dat");
                    if (Files.exists(sessionFile)) {
                        try (ObjectInputStream sessionOis = new ObjectInputStream(
                                new FileInputStream(sessionFile.toString()))) {
                            ChatSession session = (ChatSession) sessionOis.readObject();
                            sessions.put(sessionId, session);
                        } catch (Exception e) {
                            // 单个会话加载失败，跳过
                            sessionOrder.remove(sessionId);
                        }
                    }
                }
            } catch (Exception e) {
                // 索引文件损坏，创建新会话
                sessions.clear();
                sessionOrder.clear();
            }
        }
        
        // 如果没有会话，创建一个默认会话
        if (sessions.isEmpty()) {
            createNewSession();
        }
    }
    
    /**
     * 保存历史记录
     */
    public void saveHistory() {
        try {
            Path dirPath = Paths.get(HISTORY_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // 保存索引
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(INDEX_FILE))) {
                oos.writeObject(new ArrayList<>(sessionOrder));
            }
            
            // 保存每个会话
            for (ChatSession session : sessions.values()) {
                Path sessionFile = Paths.get(HISTORY_DIR + "/" + session.getId() + ".dat");
                try (ObjectOutputStream sessionOos = new ObjectOutputStream(
                        new FileOutputStream(sessionFile.toString()))) {
                    sessionOos.writeObject(session);
                } catch (Exception e) {
                    // 单个会话保存失败，记录错误后继续
                    System.err.println("保存会话失败 [" + session.getId() + "]: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("保存历史记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建新会话
     */
    public ChatSession createNewSession() {
        ChatSession session = new ChatSession();
        sessions.put(session.getId(), session);
        sessionOrder.add(0, session.getId()); // 新会话放在最前面
        saveHistory();
        return session;
    }
    
    /**
     * 获取指定会话
     */
    public ChatSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * 获取所有会话（按时间顺序）
     */
    public List<ChatSession> getAllSessions() {
        List<ChatSession> result = new ArrayList<>();
        for (String id : sessionOrder) {
            ChatSession session = sessions.get(id);
            if (session != null) {
                result.add(session);
            }
        }
        return result;
    }
    
    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
        sessionOrder.remove(sessionId);
        
        // 删除文件
        try {
            Path sessionFile = Paths.get(HISTORY_DIR + "/" + sessionId + ".dat");
            Files.deleteIfExists(sessionFile);
        } catch (Exception e) {
            // 忽略
        }
        
        // 如果没有会话了，创建一个
        if (sessions.isEmpty()) {
            createNewSession();
        }
        
        saveHistory();
    }
    
    /**
     * 更新会话标题
     */
    public void updateSessionTitle(String sessionId, String title) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.setTitle(title);
            saveHistory();
        }
    }
    
    /**
     * 添加消息到会话
     */
    public void addMessageToSession(String sessionId, ChatMessage message) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.addMessage(message);
            
            // 如果是第一条用户消息，更新标题
            if (session.getMessageCount() == 1 && message.getRole() == ChatMessage.Role.USER) {
                session.setTitle(session.getFirstMessagePreview());
            }
            
            saveHistory();
        }
    }
    
    /**
     * 清空会话消息
     */
    public void clearSessionMessages(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.clearMessages();
            session.setTitle("新对话");
            saveHistory();
        }
    }
    
    /**
     * 获取第一个会话ID
     */
    public String getFirstSessionId() {
        if (sessionOrder.isEmpty()) {
            return createNewSession().getId();
        }
        return sessionOrder.get(0);
    }
}
