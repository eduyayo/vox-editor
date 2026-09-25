package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.util.function.Supplier;

public class GridPanel extends JPanel {
    private int gridSize = 20;
    private VoxelModel model;
    private Supplier<String> viewNameSupplier;

    public GridPanel() {
        setBackground(Color.WHITE);
    }

    public void setModel(VoxelModel model, Supplier<String> viewNameSupplier) {
        this.model = model;
        this.viewNameSupplier = viewNameSupplier;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        g.setColor(Color.LIGHT_GRAY);

        for (int x = 0; x < width; x += gridSize) {
            g.drawLine(x, 0, x, height);
        }

        for (int y = 0; y < height; y += gridSize) {
            g.drawLine(0, y, width, y);
        }

        if (model != null && viewNameSupplier != null) {
            String viewName = viewNameSupplier.get();
            if (viewName != null) {
                // Draw projections
                g.setColor(Color.CYAN);
                for (Projection p : model.getProjections()) {
                    if (viewName.equals(p.viewName())) {
                        g.fillRect(p.u() * gridSize, p.v() * gridSize, gridSize, gridSize);
                    }
                }

                // Draw mapped voxels
                g.setColor(Color.RED);
                for (Vector3D v : model.getVoxels().keySet()) {
                    Integer mappedU = null;
                    Integer mappedV = null;
                    switch (viewName) {
                        case "Front": mappedU = v.x(); mappedV = v.y(); break;
                        case "Top": mappedU = v.x(); mappedV = v.z(); break;
                        case "Bottom": mappedU = v.x(); mappedV = v.z(); break;
                        case "Left": mappedU = v.z(); mappedV = v.y(); break;
                        case "Right": mappedU = v.z(); mappedV = v.y(); break;
                    }
                    if (mappedU != null && mappedV != null) {
                        g.fillRect(mappedU * gridSize, mappedV * gridSize, gridSize, gridSize);
                    }
                }
            }
        }

        // Draw axes
        g.setColor(Color.BLACK);
        g.drawLine(width / 2, 0, width / 2, height);
        g.drawLine(0, height / 2, width, height / 2);
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
        repaint();
    }
}
