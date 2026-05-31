package main;

import javax.swing.*;
import ui.ToeicApp;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        System.out.println("ToeicApp starting on main thread...");
        SwingUtilities.invokeLater(ToeicApp::new);
    }
}
