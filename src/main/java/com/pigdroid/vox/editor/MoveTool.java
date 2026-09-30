package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MoveTool implements Tool {
    private JPanel optionsPanel;

    private Integer startGridX;
    private Integer startGridY;

    private Map<Vector3D, Integer> originalVoxels;
    private String currentViewName;
    private VoxelModel currentModel;
    private GridPanel currentGridPanel;
    private java.util.Set<Vector3D> lastAddedVoxels;

    public MoveTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    public String getName() {
        return "Move";
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public void onMousePressed(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;

        this.currentViewName = viewName;
        this.currentModel = model;
        this.currentGridPanel = gridPanel;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        startGridX = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        startGridY = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());

        SelectionBox sel = model.getSelection();
        originalVoxels = new HashMap<>();

        lastAddedVoxels = new java.util.HashSet<>();

        if (sel != null) {
            Map<Vector3D, Integer> allVoxels = model.getVoxels();
            for (Map.Entry<Vector3D, Integer> entry : allVoxels.entrySet()) {
                Vector3D v = entry.getKey();
                if (v.x() >= sel.getMinX() && v.x() <= sel.getMaxX() &&
                    v.y() >= sel.getMinY() && v.y() <= sel.getMaxY() &&
                    v.z() >= sel.getMinZ() && v.z() <= sel.getMaxZ() &&
                    !model.isGhosted(v)) {
                    originalVoxels.put(v, entry.getValue());
                }
            }
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;
        if (startGridX == null || startGridY == null || originalVoxels == null || originalVoxels.isEmpty()) return;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int currentGridX = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        int currentGridY = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());

        int deltaU = currentGridX - startGridX;
        int deltaV = currentGridY - startGridY;

        int dx = 0, dy = 0, dz = 0;

        ReferenceSystem ref = model.getReferenceSystem();
        if (ref == ReferenceSystem.AMERICAN) {
            switch (viewName) {
                case "Front": case "Back": dx = deltaU; dz = deltaV; break;
                case "Top": case "Bottom": dx = deltaU; dy = deltaV; break;
                case "Left": case "Right": dy = deltaU; dz = deltaV; break;
            }
        } else { // EUROPEAN
            switch (viewName) {
                case "Front": case "Back": dx = deltaU; dy = deltaV; break;
                case "Top": case "Bottom": dx = deltaU; dz = deltaV; break;
                case "Left": case "Right": dz = deltaU; dy = deltaV; break;
            }
        }

        // To make movement look continuous without messing up selection bounds internally:
        // Clear all voxels from selection box
        SelectionBox sel = model.getSelection();
        if (sel != null) {
            model.clearSelection(); // temporarily clear selection so we can setVoxels
        }

        if (lastAddedVoxels.isEmpty()) {
            for (Vector3D v : originalVoxels.keySet()) {
                model.removeVoxel(v.x(), v.y(), v.z());
            }
        } else {
            for (Vector3D v : lastAddedVoxels) {
                model.removeVoxel(v.x(), v.y(), v.z());
            }
            lastAddedVoxels.clear();
        }

        for (Map.Entry<Vector3D, Integer> entry : originalVoxels.entrySet()) {
            Vector3D v = entry.getKey();
            int color = entry.getValue();
            Vector3D newV = new Vector3D(v.x() + dx, v.y() + dy, v.z() + dz);
            model.setVoxel(newV.x(), newV.y(), newV.z(), color);
            lastAddedVoxels.add(newV);
        }

        if (sel != null) {
            model.setSelection(new SelectionBox(
                sel.getMinX() + dx, sel.getMinY() + dy, sel.getMinZ() + dz,
                sel.getMaxX() + dx, sel.getMaxY() + dy, sel.getMaxZ() + dz
            ));
        }

        gridPanel.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        startGridX = null;
        startGridY = null;
        originalVoxels = null;
        if (lastAddedVoxels != null) {
            lastAddedVoxels.clear();
        }
        currentViewName = null;
        currentModel = null;
        currentGridPanel = null;
    }
}
