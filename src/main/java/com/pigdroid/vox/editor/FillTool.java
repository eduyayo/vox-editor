package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class FillTool implements Tool {
    private Color currentColor = Color.GRAY;
    private JPanel optionsPanel;

    public FillTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(optionsPanel, "Choose Fill Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(currentColor);
            }
        });
        optionsPanel.add(colorButton);
    }

    @Override
    public String getName() {
        return "Fill";
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
        // Fill typically triggers only on press
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
    }

    private void applyTool(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int u = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        int v = Math.floorDiv(e.getY() - originY, gridPanel.getGridSize());

        int replacementColor = currentColor.getRGB();

        boolean changed = false;

        // 1. Find the target color(s) to replace. We start from voxels mapped to (u, v)
        // If there are multiple voxels at (u, v) with different colors, we fill for all of them.
        List<Vector3D> startVoxels = new ArrayList<>();
        for (Map.Entry<Vector3D, Integer> entry : model.getVoxels().entrySet()) {
            Vector3D vec = entry.getKey();
            Integer mappedU = model.getMappedU(viewName, vec);
            Integer mappedV = model.getMappedV(viewName, vec);
            if (mappedU != null && mappedV != null && mappedU == u && mappedV == v) {
                if (entry.getValue() != replacementColor) {
                    startVoxels.add(vec);
                }
            }
        }

        if (startVoxels.isEmpty()) return;

        SelectionBox sel = model.getSelection();

        int[][] dirs = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
        };

        Set<Vector3D> visited = new HashSet<>();
        Queue<Vector3D> queue = new LinkedList<>();

        for (Vector3D startVec : startVoxels) {
            if (visited.contains(startVec)) continue;

            int targetColor = model.getVoxel(startVec.x(), startVec.y(), startVec.z());
            queue.add(startVec);
            visited.add(startVec);

            while (!queue.isEmpty()) {
                Vector3D curr = queue.poll();

                // Replace color
                model.setVoxel(curr.x(), curr.y(), curr.z(), replacementColor);
                changed = true;

                // Check neighbors
                for (int[] dir : dirs) {
                    Vector3D neighbor = new Vector3D(curr.x() + dir[0], curr.y() + dir[1], curr.z() + dir[2]);

                    if (!visited.contains(neighbor)) {
                        // Check bounds if selection exists
                        boolean inBounds = true;
                        if (sel != null) {
                            if (neighbor.x() < sel.getMinX() || neighbor.x() > sel.getMaxX() ||
                                neighbor.y() < sel.getMinY() || neighbor.y() > sel.getMaxY() ||
                                neighbor.z() < sel.getMinZ() || neighbor.z() > sel.getMaxZ()) {
                                inBounds = false;
                            }
                        }

                        if (inBounds) {
                            Integer neighborColor = model.getVoxel(neighbor.x(), neighbor.y(), neighbor.z());
                            if (neighborColor != null && neighborColor == targetColor) {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        if (changed) {
            SwingUtilities.getWindowAncestor(gridPanel).repaint();
        }
    }
}
