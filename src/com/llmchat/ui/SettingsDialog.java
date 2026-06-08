package com.llmchat.ui;

import com.llmchat.config.ConfigManager;
import com.llmchat.model.Config;
import com.llmchat.ui.components.ModernButton;
import com.llmchat.ui.components.ModernTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 设置对话框 - API配置界面
 */
public class SettingsDialog extends JDialog {
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(60, 60, 60);
    
    private final ConfigManager configManager;
    
    private ModernTextField apiUrlField;
    private JPasswordField apiKeyField;
    private ModernTextField modelNameField;
    private JComboBox<String> modelComboBox;
    private boolean useCustomModel = false;
    
    public SettingsDialog(Frame parent, ConfigManager configManager) {
        super(parent, "设置", true);
        this.configManager = configManager;
        
        initializeUI();
        loadCurrentConfig();
        
        setSize(600, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // 标题
        JLabel titleLabel = new JLabel("API 配置");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        // 表单面板
        JPanel formPanel = createFormPanel();
        
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 组装
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // API URL
        panel.add(createFormField("API URL:", apiUrlField = new ModernTextField(30)));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // API Key
        JPanel apiKeyPanel = new JPanel(new BorderLayout(5, 0));
        apiKeyPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel apiKeyLabel = new JLabel("API Key:");
        apiKeyLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        apiKeyLabel.setForeground(LABEL_COLOR);
        apiKeyLabel.setPreferredSize(new Dimension(80, 25));
        
        apiKeyField = new JPasswordField(30);
        apiKeyField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        apiKeyField.setBackground(Color.WHITE);
        apiKeyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        apiKeyPanel.add(apiKeyLabel, BorderLayout.WEST);
        apiKeyPanel.add(apiKeyField, BorderLayout.CENTER);
        panel.add(apiKeyPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // 模型选择
        panel.add(createModelSelectionPanel());
        
        return panel;
    }
    
    private JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        label.setForeground(LABEL_COLOR);
        label.setPreferredSize(new Dimension(80, 25));
        
        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createModelSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel("模型:");
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        label.setForeground(LABEL_COLOR);
        label.setPreferredSize(new Dimension(80, 25));
        
        // 预设模型列表
        String[] models = {
            "gpt-3.5-turbo",
            "gpt-4",
            "gpt-4-turbo",
            "gpt-4o",
            "gpt-4o-mini",
            "自定义..."
        };
        
        modelComboBox = new JComboBox<>(models);
        modelComboBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        modelComboBox.setBackground(Color.WHITE);
        
        modelNameField = new ModernTextField(20);
        modelNameField.setVisible(false);
        
        modelComboBox.addActionListener(e -> {
            String selected = (String) modelComboBox.getSelectedItem();
            if ("自定义...".equals(selected)) {
                useCustomModel = true;
                modelNameField.setVisible(true);
                modelNameField.setText("");
            } else {
                useCustomModel = false;
                modelNameField.setVisible(false);
            }
            panel.revalidate();
            panel.repaint();
        });
        
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setBackground(BACKGROUND_COLOR);
        fieldPanel.add(modelComboBox, BorderLayout.CENTER);
        fieldPanel.add(modelNameField, BorderLayout.EAST);
        
        panel.add(label, BorderLayout.WEST);
        panel.add(fieldPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        ModernButton saveButton = new ModernButton("保存");
        saveButton.addActionListener(e -> saveConfig());
        
        ModernButton cancelButton = new ModernButton("取消");
        cancelButton.addActionListener(e -> dispose());
        
        panel.add(cancelButton);
        panel.add(saveButton);
        
        return panel;
    }
    
    private void loadCurrentConfig() {
        Config config = configManager.getConfig();
        apiUrlField.setText(config.getApiUrl());
        apiKeyField.setText(config.getApiKey());
        
        String modelName = config.getModelName();
        String[] models = {
            "gpt-3.5-turbo",
            "gpt-4",
            "gpt-4-turbo",
            "gpt-4o",
            "gpt-4o-mini"
        };
        
        boolean found = false;
        for (String model : models) {
            if (model.equals(modelName)) {
                modelComboBox.setSelectedItem(modelName);
                found = true;
                break;
            }
        }
        
        if (!found && !modelName.isEmpty()) {
            modelComboBox.setSelectedItem("自定义...");
            modelNameField.setText(modelName);
        }
    }
    
    private void saveConfig() {
        String apiUrl = apiUrlField.getText().trim();
        String apiKey = new String(apiKeyField.getPassword()).trim();
        String modelName;
        
        if (useCustomModel) {
            modelName = modelNameField.getText().trim();
        } else {
            modelName = (String) modelComboBox.getSelectedItem();
        }
        
        // 验证
        if (apiUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入API URL", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入API Key", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (modelName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入模型名称", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 保存配置
        configManager.updateConfig(apiUrl, apiKey, modelName);
        
        JOptionPane.showMessageDialog(this, "配置已保存", "成功", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
