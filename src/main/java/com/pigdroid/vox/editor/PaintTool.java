package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class PaintTool implements Tool {
    private Color currentColor = Color.GRAY;
    private JPanel optionsPanel;

    public PaintTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(optionsPanel, "Choose Paint Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(currentColor);
            }
        });
        optionsPanel.add(colorButton);
    }

    @Override
    public String getName() {
        return "Paint";
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
        int v = Math.floorDiv(e.getY() - originY, gridPanel.getGridSize());

        if (SwingUtilities.isLeftMouseButton(e)) {
            boolean changed = false;

            // Find all voxels mapped to (u,v) in the current view
            List<Vector3D> toUpdate = new ArrayList<>();
            for (Map.Entry<Vector3D, Integer> entry : model.getVoxels().entrySet()) {
                Vector3D vec = entry.getKey();
                Integer mappedU = model.getMappedU(viewName, vec);
                Integer mappedV = model.getMappedV(viewName, vec);
                if (mappedU != null && mappedV != null && mappedU == u && mappedV == v) {
                    if (!entry.getValue().equals(currentColor.getRGB())) {
                        toUpdate.add(vec);
                    }
                }
            }

            for (Vector3D vec : toUpdate) {
                model.setVoxel(vec.x(), vec.y(), vec.z(), currentColor.getRGB());
                changed = true;
            }

            // Note: we can also update matching projections if we want, but setVoxel handles the model state.
            // Collect them first to avoid ConcurrentModificationException since addProjection mutates the list.
            List<Projection> projsToUpdate = new ArrayList<>();
            for (Projection p : model.getProjections()) {
                if (p.viewName().equals(viewName) && p.u() == u && p.v() == v) {
                    if (p.color() != currentColor.getRGB()) {
                        projsToUpdate.add(p);
                    }
                }
            }
            for (Projection p : projsToUpdate) {
                model.addProjection(viewName, u, v, currentColor.getRGB());
                changed = true;
            }

            if (changed) {
                SwingUtilities.getWindowAncestor(gridPanel).repaint();
            }
        }
    }
}
