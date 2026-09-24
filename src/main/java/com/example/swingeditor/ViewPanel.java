package com.example.swingeditor;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class ViewPanel extends JPanel {
    private JComboBox<String> viewSelector;
    private GridPanel gridPanel;
    private VoxelModel model;

    public ViewPanel(String initialView, VoxelModel model) {
        this.model = model;
        setLayout(new BorderLayout());

        gridPanel = new GridPanel();

        viewSelector = new JComboBox<>(new String[]{"Front", "Top", "Bottom", "Left", "Right"});
        viewSelector.setSelectedItem(initialView);

        viewSelector.addActionListener(e -> updateBorderTitle());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(viewSelector);

        add(topPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);

        updateBorderTitle();
    }

    private void updateBorderTitle() {
        String selectedView = (String) viewSelector.getSelectedItem();
        setBorder(BorderFactory.createTitledBorder(selectedView));
    }

    public String getSelectedView() {
        return (String) viewSelector.getSelectedItem();
    }
}
