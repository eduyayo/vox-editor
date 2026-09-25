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

    public ViewPanel(String initialView, VoxelModel model, ToolManager toolManager) {
        this.model = model;
        setLayout(new BorderLayout());

        gridPanel = new GridPanel();

        viewSelector = new JComboBox<>(new String[]{"Front", "Back", "Top", "Bottom", "Left", "Right"});
        viewSelector.setSelectedItem(initialView);

        viewSelector.addActionListener(e -> {
            updateBorderTitle();
            gridPanel.repaint();
        });

        gridPanel.setModel(model, () -> (String) viewSelector.getSelectedItem());

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Tool tool = toolManager.getActiveTool();
                if (tool != null) {
                    tool.onMousePressed(e, (String) viewSelector.getSelectedItem(), model, gridPanel);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Tool tool = toolManager.getActiveTool();
                if (tool != null) {
                    tool.onMouseDragged(e, (String) viewSelector.getSelectedItem(), model, gridPanel);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Tool tool = toolManager.getActiveTool();
                if (tool != null) {
                    tool.onMouseReleased(e);
                }
            }
        };

        gridPanel.addMouseListener(ma);
        gridPanel.addMouseMotionListener(ma);

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
