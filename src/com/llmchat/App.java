package com.llmchat;

import com.llmchat.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * 大语言模型聊天应用程序主入口
 */
public class App {
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 在EDT中启动UI
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
