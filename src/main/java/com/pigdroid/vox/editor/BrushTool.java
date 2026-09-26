package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;

public class BrushTool implements Tool {
    private Color currentColor = Color.GRAY;
    private JPanel optionsPanel;

    public BrushTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(optionsPanel, "Choose Brush Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(currentColor);
            }
        });
        optionsPanel.add(colorButton);
    }

    @Override
    public String getName() {
        return "Brush";
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public void onMousePressed(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;
        applyTool(e, viewName, model, gridPanel);
    }

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;
        applyTool(e, viewName, model, gridPanel);
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
    }

    private void applyTool(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int u = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        int v = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());

        if (SwingUtilities.isLeftMouseButton(e)) {
            model.addProjection(viewName, u, v, currentColor.getRGB());
        } else if (SwingUtilities.isRightMouseButton(e)) {
            model.deleteProjection(viewName, u, v);
        }
        SwingUtilities.getWindowAncestor(gridPanel).repaint();
    }
}
