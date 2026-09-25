package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;

public class SquareTool implements Tool {
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

    public SquareTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(optionsPanel, "Choose Square Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(currentColor);
            }
        });
        optionsPanel.add(colorButton);
    }

    @Override
    public String getName() {
        return "Square";
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
        startGridY = Math.floorDiv(e.getY() - originY, gridPanel.getGridSize());
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
        currentGridY = Math.floorDiv(e.getY() - originY, gridPanel.getGridSize());

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

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    if (!isRightClick) {
                        currentModel.addProjection(currentViewName, x, y, currentColor.getRGB());
                    } else {
                        currentModel.deleteProjection(currentViewName, x, y);
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

        int px = originX + minX * gridSize;
        int py = originY + minY * gridSize;
        int width = (maxX - minX + 1) * gridSize;
        int height = (maxY - minY + 1) * gridSize;

        g.fillRect(px, py, width, height);
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
