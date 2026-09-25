package com.pigdroid.vox.editor;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;

public class FreeformTool implements Tool {
    private JPanel optionsPanel;
    private JCheckBox keepWidthCheckbox;

    private enum DragHandle { NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    private DragHandle currentDrag = DragHandle.NONE;

    private Integer fixedGridU;
    private Integer fixedGridV;

    private String currentViewName;
    private VoxelModel currentModel;
    private GridPanel currentGridPanel;

    private VoxelModel initialModelSnapshot;
    private SelectionBox initialSelection;

    public FreeformTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keepWidthCheckbox = new JCheckBox("Keep line width", true);
        optionsPanel.add(keepWidthCheckbox);
    }

    @Override
    public String getName() {
        return "Freeform/scale";
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    private void cancelDrag() {
        if (currentDrag != DragHandle.NONE && currentModel != null && initialModelSnapshot != null) {
            currentModel.copyFrom(initialModelSnapshot);
            currentDrag = DragHandle.NONE;
            if (currentGridPanel != null) {
                SwingUtilities.getWindowAncestor(currentGridPanel).repaint();
            }
        }
    }

    private void setupCancelKeystroke(GridPanel gridPanel) {
        gridPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelFreeform");
        gridPanel.getActionMap().put("cancelFreeform", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDrag();
            }
        });
    }

    @Override
    public void onMousePressed(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;

        this.currentViewName = viewName;
        this.currentModel = model;
        this.currentGridPanel = gridPanel;

        setupCancelKeystroke(gridPanel);

        SelectionBox sel = model.getSelection();

        if (sel == null) {
            int gMinX = Integer.MAX_VALUE;
            int gMaxX = Integer.MIN_VALUE;
            int gMinY = Integer.MAX_VALUE;
            int gMaxY = Integer.MIN_VALUE;
            int gMinZ = Integer.MAX_VALUE;
            int gMaxZ = Integer.MIN_VALUE;
            boolean hasVoxels = false;

            for (Vector3D v : model.getVoxels().keySet()) {
                if (v.x() < gMinX) gMinX = v.x();
                if (v.x() > gMaxX) gMaxX = v.x();
                if (v.y() < gMinY) gMinY = v.y();
                if (v.y() > gMaxY) gMaxY = v.y();
                if (v.z() < gMinZ) gMinZ = v.z();
                if (v.z() > gMaxZ) gMaxZ = v.z();
                hasVoxels = true;
            }
            if (hasVoxels) {
                sel = new SelectionBox(gMinX, gMinY, gMinZ, gMaxX, gMaxY, gMaxZ);
                model.setSelection(sel);
            } else {
                return; // Nothing to scale
            }
        }

        this.initialModelSnapshot = model.clone();
        this.initialSelection = sel.clone();

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int gridSize = gridPanel.getGridSize();

        Integer uMin = model.getMappedU(viewName, new Vector3D(sel.getMinX(), sel.getMinY(), sel.getMinZ()));
        Integer vMin = model.getMappedV(viewName, new Vector3D(sel.getMinX(), sel.getMinY(), sel.getMinZ()));
        Integer uMax = model.getMappedU(viewName, new Vector3D(sel.getMaxX(), sel.getMaxY(), sel.getMaxZ()));
        Integer vMax = model.getMappedV(viewName, new Vector3D(sel.getMaxX(), sel.getMaxY(), sel.getMaxZ()));

        currentDrag = DragHandle.NONE;

        if (uMin != null && vMin != null && uMax != null && vMax != null) {
            int minU = Math.min(uMin, uMax);
            int maxU = Math.max(uMin, uMax);
            int minV = Math.min(vMin, vMax);
            int maxV = Math.max(vMin, vMax);

            int pxMin = gridPanel.gridToScreenX(minU);
            int pyMin = gridPanel.gridToScreenY(minV);
            int pxMax = gridPanel.gridToScreenX(maxU) + gridSize;
            int pyMax = gridPanel.gridToScreenY(maxV) + gridSize;

            int handleSize = 6;
            int mx = e.getX();
            int my = e.getY();

            if (Math.abs(mx - pxMin) <= handleSize && Math.abs(my - pyMin) <= handleSize) {
                currentDrag = DragHandle.TOP_LEFT;
                fixedGridU = maxU; fixedGridV = maxV;
            } else if (Math.abs(mx - pxMax) <= handleSize && Math.abs(my - pyMin) <= handleSize) {
                currentDrag = DragHandle.TOP_RIGHT;
                fixedGridU = minU; fixedGridV = maxV;
            } else if (Math.abs(mx - pxMin) <= handleSize && Math.abs(my - pyMax) <= handleSize) {
                currentDrag = DragHandle.BOTTOM_LEFT;
                fixedGridU = maxU; fixedGridV = minV;
            } else if (Math.abs(mx - pxMax) <= handleSize && Math.abs(my - pyMax) <= handleSize) {
                currentDrag = DragHandle.BOTTOM_RIGHT;
                fixedGridU = minU; fixedGridV = minV;
            }
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;
        if (currentDrag == DragHandle.NONE) return;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int gridSize = gridPanel.getGridSize();

        int currentGridU = Math.floorDiv(e.getX() - originX, gridSize);
        int currentGridV = Math.floorDiv(e.getY() - originY, gridSize);

        updateScale(currentGridU, currentGridV);
        gridPanel.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (currentDrag != DragHandle.NONE) {
            int originX = currentGridPanel.getWidth() / 2 + currentGridPanel.getPanX();
            int originY = currentGridPanel.getHeight() / 2 + currentGridPanel.getPanY();
            int gridSize = currentGridPanel.getGridSize();

            int currentGridU = Math.floorDiv(e.getX() - originX, gridSize);
            int currentGridV = Math.floorDiv(e.getY() - originY, gridSize);

            updateScale(currentGridU, currentGridV);
            if (currentGridPanel != null) {
                SwingUtilities.getWindowAncestor(currentGridPanel).repaint();
                currentGridPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                currentGridPanel.getActionMap().remove("cancelFreeform");
            }
        }
        currentDrag = DragHandle.NONE;
    }

    private void updateScale(int currentGridU, int currentGridV) {
        if (currentModel == null || currentViewName == null || fixedGridU == null || fixedGridV == null || initialSelection == null) return;

        currentModel.copyFrom(initialModelSnapshot);

        int u1 = fixedGridU;
        int v1 = fixedGridV;
        int u2 = currentGridU;
        int v2 = currentGridV;

        int newMinU = Math.min(u1, u2);
        int newMaxU = Math.max(u1, u2);
        int newMinV = Math.min(v1, v2);
        int newMaxV = Math.max(v1, v2);

        Integer oldUMin = initialModelSnapshot.getMappedU(currentViewName, new Vector3D(initialSelection.getMinX(), initialSelection.getMinY(), initialSelection.getMinZ()));
        Integer oldVMin = initialModelSnapshot.getMappedV(currentViewName, new Vector3D(initialSelection.getMinX(), initialSelection.getMinY(), initialSelection.getMinZ()));
        Integer oldUMax = initialModelSnapshot.getMappedU(currentViewName, new Vector3D(initialSelection.getMaxX(), initialSelection.getMaxY(), initialSelection.getMaxZ()));
        Integer oldVMax = initialModelSnapshot.getMappedV(currentViewName, new Vector3D(initialSelection.getMaxX(), initialSelection.getMaxY(), initialSelection.getMaxZ()));

        if (oldUMin == null || oldVMin == null || oldUMax == null || oldVMax == null) return;

        int oldMinU = Math.min(oldUMin, oldUMax);
        int oldMaxU = Math.max(oldUMin, oldUMax);
        int oldMinV = Math.min(oldVMin, oldVMax);
        int oldMaxV = Math.max(oldVMin, oldVMax);

        int oldWidth = oldMaxU - oldMinU + 1;
        int oldHeight = oldMaxV - oldMinV + 1;

        int newWidth = newMaxU - newMinU + 1;
        int newHeight = newMaxV - newMinV + 1;

        if (oldWidth == 0 || oldHeight == 0 || newWidth == 0 || newHeight == 0) return;

        Map<Vector3D, Integer> unselectedVoxels = new HashMap<>();
        Map<Vector3D, Integer> selectedVoxels = new HashMap<>();

        for (Map.Entry<Vector3D, Integer> entry : initialModelSnapshot.getVoxels().entrySet()) {
            Vector3D v = entry.getKey();
            if (v.x() >= initialSelection.getMinX() && v.x() <= initialSelection.getMaxX() &&
                v.y() >= initialSelection.getMinY() && v.y() <= initialSelection.getMaxY() &&
                v.z() >= initialSelection.getMinZ() && v.z() <= initialSelection.getMaxZ()) {
                selectedVoxels.put(v, entry.getValue());
                currentModel.removeVoxel(v.x(), v.y(), v.z());
            } else {
                unselectedVoxels.put(v, entry.getValue());
            }
        }

        // Compute depth min/max
        int depthMin = Integer.MAX_VALUE;
        int depthMax = Integer.MIN_VALUE;

        for (Vector3D v : selectedVoxels.keySet()) {
            Integer depth = getUnmappedCoordinate(currentViewName, v);
            if (depth != null) {
                if (depth < depthMin) depthMin = depth;
                if (depth > depthMax) depthMax = depth;
            }
        }

        Vector3D newBoundsMin = constructVector(currentViewName, newMinU, newMinV, depthMin);
        Vector3D newBoundsMax = constructVector(currentViewName, newMaxU, newMaxV, depthMax);

        if (newBoundsMin != null && newBoundsMax != null) {
            int finalMinX = Math.min(newBoundsMin.x(), newBoundsMax.x());
            int finalMaxX = Math.max(newBoundsMin.x(), newBoundsMax.x());
            int finalMinY = Math.min(newBoundsMin.y(), newBoundsMax.y());
            int finalMaxY = Math.max(newBoundsMin.y(), newBoundsMax.y());
            int finalMinZ = Math.min(newBoundsMin.z(), newBoundsMax.z());
            int finalMaxZ = Math.max(newBoundsMin.z(), newBoundsMax.z());
            currentModel.setSelection(new SelectionBox(finalMinX, finalMinY, finalMinZ, finalMaxX, finalMaxY, finalMaxZ));
        }

        boolean keepWidth = keepWidthCheckbox.isSelected();

        if (!keepWidth) {
            // Backward mapping
            for (int u = newMinU; u <= newMaxU; u++) {
                for (int v = newMinV; v <= newMaxV; v++) {
                    int srcU = oldMinU + (int) Math.round((u - newMinU) * (oldWidth - 1.0) / Math.max(1, newWidth - 1));
                    int srcV = oldMinV + (int) Math.round((v - newMinV) * (oldHeight - 1.0) / Math.max(1, newHeight - 1));

                    for (int depth = depthMin; depth <= depthMax; depth++) {
                        Vector3D srcVec = constructVector(currentViewName, srcU, srcV, depth);
                        if (srcVec != null && selectedVoxels.containsKey(srcVec)) {
                            Vector3D destVec = constructVector(currentViewName, u, v, depth);
                            if (destVec != null) {
                                currentModel.setVoxel(destVec.x(), destVec.y(), destVec.z(), selectedVoxels.get(srcVec));
                            }
                        }
                    }
                }
            }
        } else {
            // Forward mapping
            for (Map.Entry<Vector3D, Integer> entry : selectedVoxels.entrySet()) {
                Vector3D srcVec = entry.getKey();
                Integer u = currentModel.getMappedU(currentViewName, srcVec);
                Integer v = currentModel.getMappedV(currentViewName, srcVec);
                Integer depth = getUnmappedCoordinate(currentViewName, srcVec);

                if (u != null && v != null && depth != null) {
                    int destU = newMinU + (int) Math.round((u - oldMinU) * (newWidth - 1.0) / Math.max(1, oldWidth - 1));
                    int destV = newMinV + (int) Math.round((v - oldMinV) * (newHeight - 1.0) / Math.max(1, oldHeight - 1));

                    Vector3D destVec = constructVector(currentViewName, destU, destV, depth);
                    if (destVec != null) {
                        currentModel.setVoxel(destVec.x(), destVec.y(), destVec.z(), entry.getValue());
                    }
                }
            }

            // Gap filling: for adjacent voxels in the source, we interpolate between their mapped destinations
            // To do this simply, we check neighbors of each source voxel
            int[][] dirs = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
            for (Map.Entry<Vector3D, Integer> entry : selectedVoxels.entrySet()) {
                Vector3D srcVec = entry.getKey();
                Integer u1_src = currentModel.getMappedU(currentViewName, srcVec);
                Integer v1_src = currentModel.getMappedV(currentViewName, srcVec);
                Integer d1_src = getUnmappedCoordinate(currentViewName, srcVec);

                if (u1_src == null || v1_src == null || d1_src == null) continue;

                int destU1 = newMinU + (int) Math.round((u1_src - oldMinU) * (newWidth - 1.0) / Math.max(1, oldWidth - 1));
                int destV1 = newMinV + (int) Math.round((v1_src - oldMinV) * (newHeight - 1.0) / Math.max(1, oldHeight - 1));

                for (int[] dir : dirs) {
                    Vector3D neighbor = new Vector3D(srcVec.x() + dir[0], srcVec.y() + dir[1], srcVec.z() + dir[2]);
                    if (selectedVoxels.containsKey(neighbor)) {
                        Integer u2_src = currentModel.getMappedU(currentViewName, neighbor);
                        Integer v2_src = currentModel.getMappedV(currentViewName, neighbor);
                        Integer d2_src = getUnmappedCoordinate(currentViewName, neighbor);

                        if (u2_src == null || v2_src == null || d2_src == null) continue;

                        int destU2 = newMinU + (int) Math.round((u2_src - oldMinU) * (newWidth - 1.0) / Math.max(1, oldWidth - 1));
                        int destV2 = newMinV + (int) Math.round((v2_src - oldMinV) * (newHeight - 1.0) / Math.max(1, oldHeight - 1));

                        // Bresenham line between (destU1, destV1, d1_src) and (destU2, destV2, d2_src)
                        drawLine3D(destU1, destV1, d1_src, destU2, destV2, d2_src, entry.getValue(), currentViewName);
                    }
                }
            }
        }
    }

    private void drawLine3D(int x0, int y0, int z0, int x1, int y1, int z1, int color, String viewName) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int dz = Math.abs(z1 - z0);

        int xs = x0 < x1 ? 1 : -1;
        int ys = y0 < y1 ? 1 : -1;
        int zs = z0 < z1 ? 1 : -1;

        if (dx >= dy && dx >= dz) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;
            while (x0 != x1) {
                Vector3D v = constructVector(viewName, x0, y0, z0);
                if (v != null) currentModel.setVoxel(v.x(), v.y(), v.z(), color);
                x0 += xs;
                if (p1 >= 0) {
                    y0 += ys;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    z0 += zs;
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy;
                p2 += 2 * dz;
            }
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;
            while (y0 != y1) {
                Vector3D v = constructVector(viewName, x0, y0, z0);
                if (v != null) currentModel.setVoxel(v.x(), v.y(), v.z(), color);
                y0 += ys;
                if (p1 >= 0) {
                    x0 += xs;
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    z0 += zs;
                    p2 -= 2 * dy;
                }
                p1 += 2 * dx;
                p2 += 2 * dz;
            }
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;
            while (z0 != z1) {
                Vector3D v = constructVector(viewName, x0, y0, z0);
                if (v != null) currentModel.setVoxel(v.x(), v.y(), v.z(), color);
                z0 += zs;
                if (p1 >= 0) {
                    y0 += ys;
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    x0 += xs;
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy;
                p2 += 2 * dx;
            }
        }
        Vector3D v = constructVector(viewName, x0, y0, z0);
        if (v != null) currentModel.setVoxel(v.x(), v.y(), v.z(), color);
    }

    private Integer getUnmappedCoordinate(String viewName, Vector3D v) {
        ReferenceSystem ref = currentModel.getReferenceSystem();
        if (ref == ReferenceSystem.EUROPEAN) {
            switch (viewName) {
                case "Front": case "Back": return v.y();
                case "Top": case "Bottom": return v.z();
                case "Left": case "Right": return v.x();
            }
        } else { // AMERICAN
            switch (viewName) {
                case "Front": case "Back": return v.z();
                case "Top": case "Bottom": return v.y();
                case "Left": case "Right": return v.x();
            }
        }
        return null;
    }

    private Vector3D constructVector(String viewName, int u, int v, int depth) {
        if (currentModel == null) return null;
        ReferenceSystem ref = currentModel.getReferenceSystem();
        if (ref == ReferenceSystem.EUROPEAN) {
            switch (viewName) {
                case "Front": case "Back": return new Vector3D(u, depth, v);
                case "Top": case "Bottom": return new Vector3D(u, v, depth);
                case "Left": case "Right": return new Vector3D(depth, u, v);
            }
        } else { // AMERICAN
            switch (viewName) {
                case "Front": case "Back": return new Vector3D(u, v, depth);
                case "Top": case "Bottom": return new Vector3D(u, depth, v);
                case "Left": case "Right": return new Vector3D(depth, v, u);
            }
        }
        return null;
    }
}
