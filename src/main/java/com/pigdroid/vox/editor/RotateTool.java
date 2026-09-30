package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class RotateTool implements Tool {
    private JPanel optionsPanel;

    private Double startAngle;
    private Double centerX;
    private Double centerY;
    private Map<Vector3D, Integer> originalVoxels;
    private String currentViewName;
    private VoxelModel currentModel;
    private GridPanel currentGridPanel;
    private SelectionBox originalSelection;
    private java.util.Set<Vector3D> lastAddedVoxels;

    public RotateTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    public String getName() {
        return "Rotate";
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

        SelectionBox sel = model.getSelection();
        if (sel == null) return;
        originalSelection = sel.clone();

        Vector3D minVec = new Vector3D(sel.getMinX(), sel.getMinY(), sel.getMinZ());
        Vector3D maxVec = new Vector3D(sel.getMaxX(), sel.getMaxY(), sel.getMaxZ());

        Integer u1 = model.getMappedU(viewName, minVec);
        Integer u2 = model.getMappedU(viewName, maxVec);
        Integer v1 = model.getMappedV(viewName, minVec);
        Integer v2 = model.getMappedV(viewName, maxVec);

        if (u1 == null || u2 == null || v1 == null || v2 == null) return;

        int minU = Math.min(u1, u2);
        int maxU = Math.max(u1, u2);
        int minV = Math.min(v1, v2);
        int maxV = Math.max(v1, v2);

        centerX = minU + (maxU - minU) / 2.0;
        centerY = minV + (maxV - minV) / 2.0;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int mouseGridX = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        int mouseGridY = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());

        startAngle = Math.atan2(mouseGridY - centerY, mouseGridX - centerX);

        originalVoxels = new HashMap<>();
        lastAddedVoxels = new java.util.HashSet<>();

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

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;
        if (startAngle == null || originalVoxels == null || originalVoxels.isEmpty() || centerX == null || centerY == null) return;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int currentGridX = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        int currentGridY = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());

        double currentAngle = Math.atan2(currentGridY - centerY, currentGridX - centerX);
        double deltaAngle = currentAngle - startAngle;

        // We clear the selection to allow inserting rotated voxels that might fall outside the initial bounding box.
        model.clearSelection();

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

        double cosA = Math.cos(deltaAngle);
        double sinA = Math.sin(deltaAngle);

        int newMinX = Integer.MAX_VALUE, newMinY = Integer.MAX_VALUE, newMinZ = Integer.MAX_VALUE;
        int newMaxX = Integer.MIN_VALUE, newMaxY = Integer.MIN_VALUE, newMaxZ = Integer.MIN_VALUE;

        ReferenceSystem ref = model.getReferenceSystem();

        for (Map.Entry<Vector3D, Integer> entry : originalVoxels.entrySet()) {
            Vector3D v = entry.getKey();
            int color = entry.getValue();

            int u = model.getMappedU(viewName, v);
            int vCoord = model.getMappedV(viewName, v);

            double du = u - centerX;
            double dv = vCoord - centerY;

            int rotU = (int) Math.round(centerX + du * cosA - dv * sinA);
            int rotV = (int) Math.round(centerY + du * sinA + dv * cosA);

            int x = v.x(), y = v.y(), z = v.z();

            if (ref == ReferenceSystem.AMERICAN) {
                switch (viewName) {
                    case "Front": case "Back": x = rotU; z = rotV; break;
                    case "Top": case "Bottom": x = rotU; y = rotV; break;
                    case "Left": case "Right": y = rotU; z = rotV; break;
                }
            } else { // EUROPEAN
                switch (viewName) {
                    case "Front": case "Back": x = rotU; y = rotV; break;
                    case "Top": case "Bottom": x = rotU; z = rotV; break;
                    case "Left": case "Right": z = rotU; y = rotV; break;
                }
            }

            Vector3D newV = new Vector3D(x, y, z);
            model.setVoxel(x, y, z, color);
            lastAddedVoxels.add(newV);

            newMinX = Math.min(newMinX, x);
            newMinY = Math.min(newMinY, y);
            newMinZ = Math.min(newMinZ, z);
            newMaxX = Math.max(newMaxX, x);
            newMaxY = Math.max(newMaxY, y);
            newMaxZ = Math.max(newMaxZ, z);
        }

        if (originalSelection != null) {
            model.setSelection(new SelectionBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ));
        }

        gridPanel.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        startAngle = null;
        originalVoxels = null;
        if (lastAddedVoxels != null) {
            lastAddedVoxels.clear();
        }
        currentViewName = null;
        currentModel = null;
        currentGridPanel = null;
        centerX = null;
        centerY = null;
        originalSelection = null;
    }
}
