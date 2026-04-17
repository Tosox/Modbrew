package de.tosox.zonerelay;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            JOptionPane.showMessageDialog(null, throwable.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        });

        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            Application app = new Application();
            app.start();
        });
    }
}
