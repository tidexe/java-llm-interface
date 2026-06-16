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
 * HTTP客户端 - 支持流式响应和请求取消
 */
public class HttpClient {
    
    private volatile boolean cancelled = false;
    private volatile Thread requestThread;
    private volatile HttpURLConnection currentConnection;
    
    /**
     * 流式响应回调接口
     */
    public interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(String error);
        void onCancelled();
    }
    
    /**
     * 取消当前正在进行的请求
     */
    public void cancel() {
        cancelled = true;
        // 关闭连接以中断流
        HttpURLConnection conn = currentConnection;
        if (conn != null) {
            conn.disconnect();
        }
        // 中断请求线程
        Thread thread = requestThread;
        if (thread != null) {
            thread.interrupt();
        }
    }
    
    /**
     * 检查当前请求是否已被取消
     */
    public boolean isCancelled() {
        return cancelled;
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
        // 重置取消标志
        cancelled = false;
        
        requestThread = new Thread(() -> {
            HttpURLConnection conn = null;
            InputStream is = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(config.getApiUrl());
                conn = (HttpURLConnection) url.openConnection();
                currentConnection = conn;
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                
                // 发送请求
                String requestBody = buildRequestBody(config, messages);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
                
                // 检查是否在发送后已被取消
                if (cancelled || Thread.currentThread().isInterrupted()) {
                    callback.onCancelled();
                    return;
                }
                
                // 读取流式响应
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    String errorMsg = readErrorStream(conn);
                    callback.onError("请求失败: " + responseCode + " - " + errorMsg);
                    return;
                }
                
                is = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // 检查取消标志
                    if (cancelled || Thread.currentThread().isInterrupted()) {
                        callback.onCancelled();
                        return;
                    }
                    
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if (data.equals("[DONE]")) {
                            break;
                        }
                        String content = parseStreamContent(data);
                        if (content != null && !content.isEmpty()) {
                            callback.onChunk(content);
                        }
                    }
                }
                
                // 最终检查是否在完成前被取消
                if (cancelled || Thread.currentThread().isInterrupted()) {
                    callback.onCancelled();
                    return;
                }
                
                callback.onComplete();
                
            } catch (java.io.InterruptedIOException | java.net.SocketException e) {
                // 连接被主动中断（cancel() 调用 disconnect 导致）
                if (cancelled) {
                    callback.onCancelled();
                } else {
                    callback.onError("请求超时: " + e.getMessage());
                }
            } catch (Exception e) {
                if (cancelled || Thread.currentThread().isInterrupted()) {
                    callback.onCancelled();
                } else {
                    callback.onError("请求异常: " + e.getMessage());
                }
            } finally {
                // 确保所有资源被释放
                closeQuietly(reader);
                closeQuietly(is);
                if (conn != null) {
                    conn.disconnect();
                }
                currentConnection = null;
                requestThread = null;
            }
        });
        requestThread.start();
    }
    
    /**
     * 安全关闭 Closeable 资源
     */
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
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
