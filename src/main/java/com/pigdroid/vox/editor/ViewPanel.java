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
    private UndoManager undoManager;

    public ViewPanel(String initialView, VoxelModel model, ToolManager toolManager, UndoManager undoManager) {
        this.model = model;
        this.undoManager = undoManager;
        setLayout(new BorderLayout());

        gridPanel = new GridPanel();
        gridPanel.setToolManager(toolManager);

        viewSelector = new JComboBox<>(new String[]{"Front", "Back", "Top", "Bottom", "Left", "Right"});
        viewSelector.setSelectedItem(initialView);

        viewSelector.addActionListener(e -> {
            updateBorderTitle();
            gridPanel.repaint();
        });

        gridPanel.setModel(model, () -> (String) viewSelector.getSelectedItem());

        MouseAdapter ma = new MouseAdapter() {
            private VoxelModel snapshot;
            private int initialChangeCount;

            @Override
            public void mousePressed(MouseEvent e) {
                snapshot = model.clone();
                initialChangeCount = model.getChangeCount();

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

                if (model.getChangeCount() != initialChangeCount && undoManager != null) {
                    undoManager.recordState(snapshot);
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

    public GridPanel getGridPanel() {
        return gridPanel;
    }
}
