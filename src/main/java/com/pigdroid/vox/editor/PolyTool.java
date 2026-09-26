package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PolyTool implements Tool {
    private Color currentColor = Color.GRAY;
    private JPanel optionsPanel;
    private JCheckBox fillCheckBox;

    private List<java.awt.Point> currentPolygon = new ArrayList<>();
    private String currentViewName = null;
    private GridPanel currentGridPanel = null;

    public PolyTool() {
        optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(optionsPanel, "Choose Poly Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(currentColor);
            }
        });
        optionsPanel.add(colorButton);

        fillCheckBox = new JCheckBox("Fill", false);
        optionsPanel.add(fillCheckBox);
    }

    @Override
    public String getName() {
        return "Poly";
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public void onMousePressed(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        if (SwingUtilities.isMiddleMouseButton(e)) return;

        if (currentViewName != null && !currentViewName.equals(viewName)) {
            currentPolygon.clear();
        }
        currentViewName = viewName;
        currentGridPanel = gridPanel;

        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();
        int u = Math.floorDiv(e.getX() - originX, gridPanel.getGridSize());
        int v = Math.floorDiv(originY - e.getY(), gridPanel.getGridSize());

        if (SwingUtilities.isRightMouseButton(e)) {
            currentPolygon.clear();
            model.deleteProjection(viewName, u, v);
            SwingUtilities.getWindowAncestor(gridPanel).repaint();
            return;
        }

        java.awt.Point clickPoint = new java.awt.Point(u, v);

        if (currentPolygon.isEmpty()) {
            currentPolygon.add(clickPoint);
            model.addProjection(viewName, u, v, currentColor.getRGB());
        } else {
            java.awt.Point firstPoint = currentPolygon.get(0);
            if (currentPolygon.size() >= 2 && u == firstPoint.x && v == firstPoint.y) {
                // Close the polygon
                java.awt.Point lastPoint = currentPolygon.get(currentPolygon.size() - 1);
                drawLine(model, viewName, lastPoint.x, lastPoint.y, u, v, currentColor.getRGB());

                if (fillCheckBox.isSelected()) {
                    fillPolygon(model, viewName, currentPolygon, currentColor.getRGB());
                }

                currentPolygon.clear();
            } else {
                // Draw a line to the new point and add it
                java.awt.Point lastPoint = currentPolygon.get(currentPolygon.size() - 1);
                drawLine(model, viewName, lastPoint.x, lastPoint.y, u, v, currentColor.getRGB());
                currentPolygon.add(clickPoint);
            }
        }

        SwingUtilities.getWindowAncestor(gridPanel).repaint();
    }

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        // Not used
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        // Not used
    }

    private void drawLine(VoxelModel model, String viewName, int x0, int y0, int x1, int y1, int colorValue) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            model.addProjection(viewName, x0, y0, colorValue);

            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private void fillPolygon(VoxelModel model, String viewName, List<java.awt.Point> polygon, int colorValue) {
        if (polygon.size() < 3) return;

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (java.awt.Point p : polygon) {
            if (p.y < minY) minY = p.y;
            if (p.y > maxY) maxY = p.y;
        }

        for (int y = minY; y <= maxY; y++) {
            List<Double> intersections = new ArrayList<>();
            for (int i = 0; i < polygon.size(); i++) {
                java.awt.Point p1 = polygon.get(i);
                java.awt.Point p2 = polygon.get((i + 1) % polygon.size());

                if (p1.y == p2.y) continue;

                int yMin = Math.min(p1.y, p2.y);
                int yMax = Math.max(p1.y, p2.y);

                if (y >= yMin && y < yMax) {
                    double x = p1.x + (y - p1.y) * (double)(p2.x - p1.x) / (p2.y - p1.y);
                    intersections.add(x);
                }
            }
            Collections.sort(intersections);
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int startX = (int) Math.round(intersections.get(i));
                int endX = (int) Math.round(intersections.get(i + 1));
                for (int x = startX; x <= endX; x++) {
                    model.addProjection(viewName, x, y, colorValue);
                }
            }
        }
    }
}
