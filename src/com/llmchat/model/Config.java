package com.llmchat.model;

import java.io.Serializable;

/**
 * 应用配置模型
 */
public class Config implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String apiUrl;
    private String apiKey;
    private String modelName;
    
    public Config() {
        // 默认配置
        this.apiUrl = "https://api.openai.com/v1/chat/completions";
        this.apiKey = "";
        this.modelName = "gpt-3.5-turbo";
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public boolean isValid() {
        return apiUrl != null && !apiUrl.isEmpty() 
            && apiKey != null && !apiKey.isEmpty()
            && modelName != null && !modelName.isEmpty();
    }
}
