package com.pigdroid.vox.editor;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {

    public SettingsDialog(JFrame parent) {
        super(parent, "Settings", true);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("View Background Color:"));

        JButton colorButton = new JButton();
        colorButton.setPreferredSize(new Dimension(30, 30));
        colorButton.setBackground(Settings.getInstance().getViewBackgroundColor());
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Background Color", colorButton.getBackground());
            if (newColor != null) {
                colorButton.setBackground(newColor);
                Settings.getInstance().setViewBackgroundColor(newColor);
            }
        });
        panel.add(colorButton);

        JButton resetButton = new JButton("Reset to Default");
        resetButton.addActionListener(e -> {
            Color defaultColor = Color.DARK_GRAY;
            colorButton.setBackground(defaultColor);
            Settings.getInstance().setViewBackgroundColor(defaultColor);
        });
        panel.add(resetButton);

        add(panel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }
}
