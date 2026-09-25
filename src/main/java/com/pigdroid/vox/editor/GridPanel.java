package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.util.function.Supplier;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import java.awt.FontMetrics;
import java.util.function.Consumer;

public class GridPanel extends JPanel {
    private int gridSize = 20;
    private VoxelModel model;
    private Supplier<String> viewNameSupplier;
    private Consumer<Graphics> previewProvider;

    private int panX = 0;
    private int panY = 0;
    private int lastMouseX;
    private int lastMouseY;

    public GridPanel() {
        setBackground(Color.WHITE);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;
                    panX += dx;
                    panY += dy;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public int getPanX() {
        return panX;
    }

    public int getPanY() {
        return panY;
    }

    public void setModel(VoxelModel model, Supplier<String> viewNameSupplier) {
        this.model = model;
        this.viewNameSupplier = viewNameSupplier;
        repaint();
    }

    public void setPreviewProvider(Consumer<Graphics> previewProvider) {
        this.previewProvider = previewProvider;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        int originX = width / 2 + panX;
        int originY = height / 2 + panY;

        g.setColor(Color.LIGHT_GRAY);

        int startX = (originX % gridSize);
        if (startX < 0) startX += gridSize;
        for (int x = startX; x < width; x += gridSize) {
            g.drawLine(x, 0, x, height);
        }

        int startY = (originY % gridSize);
        if (startY < 0) startY += gridSize;
        for (int y = startY; y < height; y += gridSize) {
            g.drawLine(0, y, width, y);
        }

        String viewName = null;
        if (model != null && viewNameSupplier != null) {
            viewName = viewNameSupplier.get();
            if (viewName != null) {
                // Draw projections
                g.setColor(Color.CYAN);
                for (Projection p : model.getProjections()) {
                    if (viewName.equals(p.viewName())) {
                        g.fillRect(originX + p.u() * gridSize, originY + p.v() * gridSize, gridSize, gridSize);
                    }
                }

                // Draw mapped voxels
                g.setColor(Color.RED);
                for (Vector3D v : model.getVoxels().keySet()) {
                    Integer mappedU = model.getMappedU(viewName, v);
                    Integer mappedV = model.getMappedV(viewName, v);

                    if (mappedU != null && mappedV != null) {
                        g.fillRect(originX + mappedU * gridSize, originY + mappedV * gridSize, gridSize, gridSize);
                    }
                }
            }
        }

        // Draw axes
        g.setColor(Color.BLACK);
        g.drawLine(originX, 0, originX, height);
        g.drawLine(0, originY, width, originY);

        // Tags and coordinates
        if (viewName != null && model != null) {
            String uAxis = "";
            String vAxis = "";

            boolean isEuropean = (model.getReferenceSystem() == ReferenceSystem.EUROPEAN);
            if (isEuropean) {
                switch (viewName) {
                    case "Front": uAxis = "X"; vAxis = "Z"; break;
                    case "Back": uAxis = "X"; vAxis = "Z"; break;
                    case "Top": uAxis = "X"; vAxis = "Y"; break;
                    case "Bottom": uAxis = "X"; vAxis = "Y"; break;
                    case "Left": uAxis = "Y"; vAxis = "Z"; break;
                    case "Right": uAxis = "Y"; vAxis = "Z"; break;
                }
            } else { // AMERICAN
                switch (viewName) {
                    case "Front": uAxis = "X"; vAxis = "Y"; break;
                    case "Back": uAxis = "X"; vAxis = "Y"; break;
                    case "Top": uAxis = "X"; vAxis = "Z"; break;
                    case "Bottom": uAxis = "X"; vAxis = "Z"; break;
                    case "Left": uAxis = "Z"; vAxis = "Y"; break;
                    case "Right": uAxis = "Z"; vAxis = "Y"; break;
                }
            }

            g.setColor(Color.BLUE);
            FontMetrics fm = g.getFontMetrics();

            // Draw axis names
            if (!uAxis.isEmpty()) {
                g.drawString(uAxis, width - fm.stringWidth(uAxis) - 5, originY - 5);
            }
            if (!vAxis.isEmpty()) {
                g.drawString(vAxis, originX + 5, fm.getAscent() + 5);
            }

            // Draw coordinates
            g.setColor(Color.DARK_GRAY);
            for (int x = startX; x < width; x += gridSize) {
                if (x == originX) continue;
                int coord = (x - originX) / gridSize;
                if (coord % 5 == 0) {
                    String text = String.valueOf(coord);
                    g.drawString(text, x + 2, originY - 2);
                    g.drawLine(x, originY - 3, x, originY + 3);
                }
            }
            for (int y = startY; y < height; y += gridSize) {
                if (y == originY) continue;
                int coord = (y - originY) / gridSize;
                if (coord % 5 == 0) {
                    String text = String.valueOf(coord);
                    g.drawString(text, originX + 2, y - 2);
                    g.drawLine(originX - 3, y, originX + 3, y);
                }
            }
        }

        if (previewProvider != null) {
            previewProvider.accept(g);
        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
        repaint();
    }
}
