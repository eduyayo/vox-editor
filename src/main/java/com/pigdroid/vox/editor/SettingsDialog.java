package com.pigdroid.vox.editor;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {

    public SettingsDialog(JFrame parent) {
        super(parent, "Settings", true);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel bgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bgPanel.add(new JLabel("View Background Color:"));

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
        bgPanel.add(colorButton);

        JButton resetButton = new JButton("Reset to Default");
        resetButton.addActionListener(e -> {
            Color defaultColor = Color.DARK_GRAY;
            colorButton.setBackground(defaultColor);
            Settings.getInstance().setViewBackgroundColor(defaultColor);
        });
        bgPanel.add(resetButton);
        mainPanel.add(bgPanel);

        JPanel axisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        axisPanel.add(new JLabel("Axis Reference Color:"));

        JButton axisColorButton = new JButton();
        axisColorButton.setPreferredSize(new Dimension(30, 30));
        axisColorButton.setBackground(Settings.getInstance().getAxisColor());
        axisColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Axis Reference Color", axisColorButton.getBackground());
            if (newColor != null) {
                axisColorButton.setBackground(newColor);
                Settings.getInstance().setAxisColor(newColor);
            }
        });
        axisPanel.add(axisColorButton);

        JButton axisResetButton = new JButton("Reset to Default");
        axisResetButton.addActionListener(e -> {
            Color defaultColor = new Color(192, 192, 192); // Silver gray
            axisColorButton.setBackground(defaultColor);
            Settings.getInstance().setAxisColor(defaultColor);
        });
        axisPanel.add(axisResetButton);
        mainPanel.add(axisPanel);

        add(mainPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }
}
