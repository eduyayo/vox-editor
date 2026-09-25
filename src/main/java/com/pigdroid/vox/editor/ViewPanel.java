package com.pigdroid.vox.editor;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

        viewSelector.addActionListener(e -> {
            updateBorderTitle();
            gridPanel.repaint();
        });

        gridPanel.setModel(model, () -> (String) viewSelector.getSelectedItem());

        gridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
                int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
                int u = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
                int v = Math.floorDiv(e.getY() - originY, gridPanel.getGridSize());
                String viewName = (String) viewSelector.getSelectedItem();

                if (SwingUtilities.isLeftMouseButton(e)) {
                    model.addProjection(viewName, u, v);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    model.deleteProjection(viewName, u, v);
                }
                SwingUtilities.getWindowAncestor(gridPanel).repaint();
            }
        });

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
