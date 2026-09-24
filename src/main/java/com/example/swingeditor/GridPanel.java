package com.example.swingeditor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

public class GridPanel extends JPanel {
    private int gridSize = 20;

    public GridPanel() {
        setBackground(Color.WHITE);
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
