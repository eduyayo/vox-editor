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

    public CircleTool() {
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

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.BLACK);
            g2d.fillOval(0, 0, width, height);
            g2d.dispose();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if ((img.getRGB(x, y) & 0xFF000000) != 0) {
                        int gridX = minX + x;
                        int gridY = minY + y;
                        if (!isRightClick) {
                            currentModel.addProjection(currentViewName, gridX, gridY, currentColor.getRGB());
                        } else {
                            currentModel.deleteProjection(currentViewName, gridX, gridY);
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

        int px = currentGridPanel.gridToScreenX(minX);
        int py = currentGridPanel.gridToScreenY(maxY);
        int width = (maxX - minX + 1) * gridSize;
        int height = (maxY - minY + 1) * gridSize;

        g.fillOval(px, py, width, height);
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
