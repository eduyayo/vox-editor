package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;

public class SelectTool implements Tool {
    private JPanel optionsPanel;

    private enum DragHandle { NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, NEW_SELECTION }
    private DragHandle currentDrag = DragHandle.NONE;

    private Integer startGridU;
    private Integer startGridV;
    private Integer currentGridU;
    private Integer currentGridV;

    private Integer fixedGridU;
    private Integer fixedGridV;

    private String currentViewName;
    private VoxelModel currentModel;
    private GridPanel currentGridPanel;

    public SelectTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearBtn = new JButton("Clear Selection");
        clearBtn.addActionListener(e -> {
            if (currentModel != null) {
                currentModel.clearSelection();
                if (currentGridPanel != null) {
                    SwingUtilities.getWindowAncestor(currentGridPanel).repaint();
                }
            }
        });
        optionsPanel.add(clearBtn);
    }

    @Override
    public String getName() {
        return "Select";
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
        int gridSize = gridPanel.getGridSize();

        int gridU = Math.floorDiv(e.getX() - originX, gridSize);
        int gridV = Math.floorDiv(e.getY() - originY, gridSize);

        currentDrag = DragHandle.NEW_SELECTION;

        SelectionBox sel = model.getSelection();
        if (sel != null) {
            Integer uMin = model.getMappedU(viewName, new Vector3D(sel.getMinX(), sel.getMinY(), sel.getMinZ()));
            Integer vMin = model.getMappedV(viewName, new Vector3D(sel.getMinX(), sel.getMinY(), sel.getMinZ()));
            Integer uMax = model.getMappedU(viewName, new Vector3D(sel.getMaxX(), sel.getMaxY(), sel.getMaxZ()));
            Integer vMax = model.getMappedV(viewName, new Vector3D(sel.getMaxX(), sel.getMaxY(), sel.getMaxZ()));

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

        if (currentDrag == DragHandle.NEW_SELECTION) {
            startGridU = gridU;
            startGridV = gridV;
            fixedGridU = gridU;
            fixedGridV = gridV;
        } else {
            startGridU = fixedGridU;
            startGridV = fixedGridV;
        }

        currentGridU = gridU;
        currentGridV = gridV;
    }

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;
        if (currentDrag == DragHandle.NONE) return;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int gridSize = gridPanel.getGridSize();

        currentGridU = Math.floorDiv(e.getX() - originX, gridSize);
        currentGridV = Math.floorDiv(e.getY() - originY, gridSize);

        updateSelection();
        gridPanel.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (currentDrag != DragHandle.NONE) {
            updateSelection();
            if (currentGridPanel != null) {
                SwingUtilities.getWindowAncestor(currentGridPanel).repaint();
            }
        }
        currentDrag = DragHandle.NONE;
    }

    private void updateSelection() {
        if (currentModel == null || currentViewName == null || fixedGridU == null || fixedGridV == null || currentGridU == null || currentGridV == null) return;

        int u1 = fixedGridU;
        int v1 = fixedGridV;
        int u2 = currentGridU;
        int v2 = currentGridV;

        int minU = Math.min(u1, u2);
        int maxU = Math.max(u1, u2);
        int minV = Math.min(v1, v2);
        int maxV = Math.max(v1, v2);

        SelectionBox sel = currentModel.getSelection();
        int minDepth = 0;
        int maxDepth = 10;

        if (sel != null && currentDrag != DragHandle.NEW_SELECTION) {
            // keep existing depth
            Integer depth1 = getUnmappedCoordinate(currentViewName, new Vector3D(sel.getMinX(), sel.getMinY(), sel.getMinZ()));
            Integer depth2 = getUnmappedCoordinate(currentViewName, new Vector3D(sel.getMaxX(), sel.getMaxY(), sel.getMaxZ()));
            if (depth1 != null && depth2 != null) {
                minDepth = Math.min(depth1, depth2);
                maxDepth = Math.max(depth1, depth2);
            }
        } else {
            // calculate new depth based on model bounds
            boolean hasVoxels = false;
            int gMinDepth = Integer.MAX_VALUE;
            int gMaxDepth = Integer.MIN_VALUE;

            for (Vector3D v : currentModel.getVoxels().keySet()) {
                Integer depth = getUnmappedCoordinate(currentViewName, v);
                if (depth != null) {
                    hasVoxels = true;
                    if (depth < gMinDepth) gMinDepth = depth;
                    if (depth > gMaxDepth) gMaxDepth = depth;
                }
            }

            if (hasVoxels) {
                minDepth = gMinDepth;
                maxDepth = gMaxDepth;
            }
        }

        // build min/max vectors
        Vector3D pMin = constructVector(currentViewName, minU, minV, minDepth);
        Vector3D pMax = constructVector(currentViewName, maxU, maxV, maxDepth);

        if (pMin != null && pMax != null) {
            int finalMinX = Math.min(pMin.x(), pMax.x());
            int finalMaxX = Math.max(pMin.x(), pMax.x());
            int finalMinY = Math.min(pMin.y(), pMax.y());
            int finalMaxY = Math.max(pMin.y(), pMax.y());
            int finalMinZ = Math.min(pMin.z(), pMax.z());
            int finalMaxZ = Math.max(pMin.z(), pMax.z());

            currentModel.setSelection(new SelectionBox(finalMinX, finalMinY, finalMinZ, finalMaxX, finalMaxY, finalMaxZ));
        }
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
