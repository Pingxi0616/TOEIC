package main;

import javax.swing.*;
import java.awt.Color;
import ui.ToeicApp;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 全域 ScrollBar 細化 + 配色
        UIManager.put("ScrollBar.width",          8);
        UIManager.put("ScrollBar.thumb",          new Color(0xC8BAA0));
        UIManager.put("ScrollBar.thumbDarkShadow",new Color(0xC8BAA0));
        UIManager.put("ScrollBar.thumbHighlight", new Color(0xE8DCC8));
        UIManager.put("ScrollBar.thumbShadow",    new Color(0xC8BAA0));
        UIManager.put("ScrollBar.track",          new Color(0xF5EFE0));
        UIManager.put("ScrollBar.trackHighlight", new Color(0xF5EFE0));
        UIManager.put("ScrollBar.background",     new Color(0xF5EFE0));

        // ComboBox 選中顏色（防止系統藍色）
        UIManager.put("ComboBox.selectionBackground", new Color(0xE8DCC8));
        UIManager.put("ComboBox.selectionForeground", new Color(0x1A1A1A));

        System.out.println("ToeicApp starting on main thread...");
        SwingUtilities.invokeLater(ToeicApp::new);
    }
}
