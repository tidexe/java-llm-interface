package com.llmchat.http;

import com.llmchat.model.ChatMessage;
import com.llmchat.model.Config;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * HTTP客户端 - 支持流式响应
 */
public class HttpClient {
    
    /**
     * 流式响应回调接口
     */
    public interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(String error);
    }
    
    /**
     * 构建请求JSON体
     */
    private String buildRequestBody(Config config, List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\":\"").append(escapeJson(config.getModelName())).append("\",");
        sb.append("\"stream\":true,");
        sb.append("\"messages\":[");
        
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"role\":\"").append(msg.getRoleString()).append("\",");
            sb.append("\"content\":\"").append(escapeJson(msg.getContent())).append("\"");
            sb.append("}");
        }
        
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 发送流式请求
     */
    public void sendStreamRequest(Config config, List<ChatMessage> messages, StreamCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(config.getApiUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);
                
                // 发送请求
                String requestBody = buildRequestBody(config, messages);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
                
                // 读取流式响应
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    String errorMsg = readErrorStream(conn);
                    callback.onError("请求失败: " + responseCode + " - " + errorMsg);
                    return;
                }
                
                try (InputStream is = conn.getInputStream();
                     BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (data.equals("[DONE]")) {
                                break;
                            }
                            // 调试输出
                            System.out.println("收到数据: " + data);
                            String content = parseStreamContent(data);
                            System.out.println("解析结果: " + content);
                            if (content != null && !content.isEmpty()) {
                                callback.onChunk(content);
                            }
                        }
                    }
                }
                
                callback.onComplete();
                
            } catch (Exception e) {
                callback.onError("请求异常: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
    
    /**
     * 解析流式响应内容
     */
    private String parseStreamContent(String jsonData) {
        try {
            // 查找 "content" 字段
            int contentIndex = jsonData.indexOf("\"content\"");
            if (contentIndex == -1) return null;
            
            // 找到冒号
            int colonIndex = jsonData.indexOf(":", contentIndex);
            if (colonIndex == -1) return null;
            
            // 找到内容开始的引号
            int quoteStart = jsonData.indexOf("\"", colonIndex);
            if (quoteStart == -1) return null;
            
            int quoteEnd = quoteStart + 1;
            while (quoteEnd < jsonData.length()) {
                char c = jsonData.charAt(quoteEnd);
                if (c == '\\' && quoteEnd + 1 < jsonData.length()) {
                    quoteEnd += 2;
                } else if (c == '"') {
                    break;
                } else {
                    quoteEnd++;
                }
            }
            
            String content = jsonData.substring(quoteStart + 1, quoteEnd);
            return unescapeJson(content);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * JSON字符串反转义
     */
    private String unescapeJson(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    default: sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 读取错误流
     */
    private String readErrorStream(HttpURLConnection conn) {
        try (InputStream is = conn.getErrorStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
