package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

public class CircleTool implements Tool {
    private Color currentColor = Color.GRAY;
    private JPanel optionsPanel;

    private Integer startGridX;
    private Integer startGridY;
    private Integer currentGridX;
    private Integer currentGridY;

    private String currentViewName;
    private VoxelModel currentModel;
    private GridPanel currentGridPanel;
    private boolean isRightClick;

    public CircleTool(ToolManager toolManager) {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(optionsPanel, "Choose Circle Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(currentColor);
            }
        });
        optionsPanel.add(colorButton);

        JButton eyedropperButton = new JButton("Eyedropper");
        eyedropperButton.addActionListener(e -> {
            toolManager.startEyedropper(color -> {
                currentColor = new Color(color);
                colorButton.setBackground(currentColor);
            });
        });
        optionsPanel.add(eyedropperButton);
    }

    @Override
    public String getName() {
        return "Circle";
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
        this.isRightClick = SwingUtilities.isRightMouseButton(e);

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        startGridX = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        startGridY = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());
        currentGridX = startGridX;
        currentGridY = startGridY;

        gridPanel.setPreviewProvider(this::drawPreview);
    }

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;
        if (startGridX == null || startGridY == null) return;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        currentGridX = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        currentGridY = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());

        gridPanel.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (currentGridPanel != null) {
            currentGridPanel.setPreviewProvider(null);
        }

        if (startGridX == null || startGridY == null || currentGridX == null || currentGridY == null) {
            resetState();
            return;
        }

        if (currentModel != null && currentViewName != null && currentGridPanel != null) {
            int minX = Math.min(startGridX, currentGridX);
            int maxX = Math.max(startGridX, currentGridX);
            int minY = Math.min(startGridY, currentGridY);
            int maxY = Math.max(startGridY, currentGridY);

            int width = maxX - minX + 1;
            int height = maxY - minY + 1;

            double cx = minX + width / 2.0;
            double cy = minY + height / 2.0;
            double rx = width / 2.0;
            double ry = height / 2.0;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    double dx = (x + 0.5) - cx;
                    double dy = (y + 0.5) - cy;
                    // Protect against division by zero if width or height is 0 (though +1 makes them at least 1)
                    if ((dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) <= 1.0) {
                        if (!isRightClick) {
                            currentModel.addProjection(currentViewName, x, y, currentColor.getRGB());
                        } else {
                            currentModel.deleteProjection(currentViewName, x, y);
                        }
                    }
                }
            }
            SwingUtilities.getWindowAncestor(currentGridPanel).repaint();
        }

        resetState();
    }

    private void drawPreview(Graphics g) {
        if (startGridX == null || startGridY == null || currentGridX == null || currentGridY == null || currentGridPanel == null) {
            return;
        }

        int originX = currentGridPanel.getWidth() / 2 + currentGridPanel.getPanX();
        int originY = currentGridPanel.getHeight() / 2 + currentGridPanel.getPanY();
        int gridSize = currentGridPanel.getGridSize();

        int minX = Math.min(startGridX, currentGridX);
        int maxX = Math.max(startGridX, currentGridX);
        int minY = Math.min(startGridY, currentGridY);
        int maxY = Math.max(startGridY, currentGridY);

        if (isRightClick) {
            g.setColor(new Color(255, 0, 0, 128)); // Semi-transparent red for delete
        } else {
            g.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 128)); // Semi-transparent current color
        }

        int width = (maxX - minX + 1);
        int height = (maxY - minY + 1);

        double cx = minX + width / 2.0;
        double cy = minY + height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                double dx = (x + 0.5) - cx;
                double dy = (y + 0.5) - cy;
                if ((dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) <= 1.0) {
                    int px = currentGridPanel.gridToScreenX(x);
                    int py = currentGridPanel.gridToScreenY(y);
                    g.fillRect(px, py, gridSize, gridSize);
                }
            }
        }
    }

    private void resetState() {
        startGridX = null;
        startGridY = null;
        currentGridX = null;
        currentGridY = null;
        currentViewName = null;
        currentModel = null;
        currentGridPanel = null;
    }
}
