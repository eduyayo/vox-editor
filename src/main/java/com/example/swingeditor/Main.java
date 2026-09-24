package com.example.swingeditor;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EditorFrame frame = new EditorFrame();
            frame.setVisible(true);
        });
    }
}
