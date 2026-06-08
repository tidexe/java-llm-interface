package com.llmchat.config;

import com.llmchat.model.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 配置管理器 - 负责配置的持久化存储
 */
public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.dir") + "/.llmchat";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.dat";
    
    private Config config;
    
    public ConfigManager() {
        loadConfig();
    }
    
    /**
     * 加载配置
     */
    private void loadConfig() {
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(CONFIG_FILE))) {
                config = (Config) ois.readObject();
            } catch (Exception e) {
                config = new Config();
            }
        } else {
            config = new Config();
        }
    }
    
    /**
     * 保存配置
     */
    public void saveConfig() {
        try {
            Path dirPath = Paths.get(CONFIG_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(CONFIG_FILE))) {
                oos.writeObject(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取配置
     */
    public Config getConfig() {
        return config;
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(String apiUrl, String apiKey, String modelName) {
        config.setApiUrl(apiUrl);
        config.setApiKey(apiKey);
        config.setModelName(modelName);
        saveConfig();
    }
    
    /**
     * 简单加密API Key (Base64编码)
     */
    public static String encryptApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(apiKey.getBytes());
    }
    
    /**
     * 解密API Key
     */
    public static String decryptApiKey(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(encrypted));
        } catch (Exception e) {
            return "";
        }
    }
}
