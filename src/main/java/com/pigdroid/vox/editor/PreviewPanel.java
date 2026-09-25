package com.pigdroid.vox.editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreviewPanel extends JPanel {
    private VoxelModel model;

    // View state
    private double rotateX = Math.PI / 6; // Isometric-like starting angle (30 degrees)
    private double rotateY = -Math.PI / 4; // Isometric-like starting angle (-45 degrees)
    private double panX = 0;
    private double panY = 0;
    private double zoom = 20.0;

    // Default view state for reset
    private final double defRotateX = Math.PI / 6;
    private final double defRotateY = -Math.PI / 4;
    private final double defPanX = 0;
    private final double defPanY = 0;
    private final double defZoom = 20.0;

    private int lastMouseX;
    private int lastMouseY;

    public PreviewPanel(VoxelModel model) {
        this.model = model;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top panel for reset button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        JButton resetButton = new JButton("Reset View");
        resetButton.addActionListener(e -> resetView());
        topPanel.add(resetButton);
        add(topPanel, BorderLayout.NORTH);

        // Mouse Listeners
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Rotate
                    rotateY += dx * 0.01;
                    rotateX += dy * 0.01;

                    // Limit X rotation to avoid flipping completely over if needed,
                    // but for general 3D viewing, free rotation is often fine.
                    // Let's constrain X rotation a bit to keep it intuitive
                    if (rotateX > Math.PI / 2) rotateX = Math.PI / 2;
                    if (rotateX < -Math.PI / 2) rotateX = -Math.PI / 2;

                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    // Pan
                    panX += dx;
                    panY += dy;
                }

                lastMouseX = e.getX();
                lastMouseY = e.getY();
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = 1.1;
                if (e.getWheelRotation() < 0) {
                    zoom *= zoomFactor;
                } else {
                    zoom /= zoomFactor;
                }
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    private void resetView() {
        rotateX = defRotateX;
        rotateY = defRotateY;
        panX = defPanX;
        panY = defPanY;
        zoom = defZoom;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2 + (int) panX;
        int centerY = getHeight() / 2 + (int) panY;

        Map<Vector3D, Integer> voxels = model.getVoxels();

        // Center the model in local coordinates
        double avgX = 0, avgY = 0, avgZ = 0;
        if (!voxels.isEmpty()) {
            for (Vector3D v : voxels.keySet()) {
                avgX += v.x();
                avgY += v.y();
                avgZ += v.z();
            }
            avgX /= voxels.size();
            avgY /= voxels.size();
            avgZ /= voxels.size();
        }

        List<VoxelDrawData> drawData = new ArrayList<>();

        if (!voxels.isEmpty()) {
            for (Map.Entry<Vector3D, Integer> entry : voxels.entrySet()) {
            Vector3D v = entry.getKey();

            // Model coordinates centered
            double mx = v.x() - avgX;
            double my = v.y() - avgY;
            double mz = v.z() - avgZ;

            // Apply Y rotation
            double x1 = mx * Math.cos(rotateY) - mz * Math.sin(rotateY);
            double z1 = mx * Math.sin(rotateY) + mz * Math.cos(rotateY);

            // Apply X rotation
            double y2 = my * Math.cos(rotateX) - z1 * Math.sin(rotateX);
            double z2 = my * Math.sin(rotateX) + z1 * Math.cos(rotateX);

            // Project to 2D
            double px = x1 * zoom;
            double py = y2 * zoom; // Invert Y in projection is common but let's see how it looks

            // We need 3D center for sorting painter's algorithm. Deeper is larger z.
            // Wait, Z axis points towards viewer? Typically -Z is into the screen.
            // Let's sort by z2. Smaller z2 means further away.
                drawData.add(new VoxelDrawData(centerX + px, centerY + py, z2, v));
            }
        }

        // Sort by depth (painter's algorithm)
        drawData.sort((a, b) -> Double.compare(b.depth, a.depth)); // Descending so further drawn first? Wait.
        // Let's check: if z points towards viewer, smaller z is further away. So we sort ascending.
        // Let's try ascending first.
        drawData.sort((a, b) -> Double.compare(a.depth, b.depth));

        // Let's actually draw cubes instead of points to give volume
        // We will project the 8 corners of each voxel.

        List<CubeDrawData> cubes = new ArrayList<>();

        if (!voxels.isEmpty()) {
            for (Map.Entry<Vector3D, Integer> entry : voxels.entrySet()) {
            Vector3D v = entry.getKey();
            double mx = v.x() - avgX;
            double my = v.y() - avgY; // y goes down in screen coords usually, let's keep it simple
            double mz = v.z() - avgZ;

            // Calculate center depth for sorting
            double z1_center = mx * Math.sin(rotateY) + mz * Math.cos(rotateY);
            double depth_center = my * Math.sin(rotateX) + z1_center * Math.cos(rotateX);

                cubes.add(new CubeDrawData(mx, my, mz, depth_center, entry.getValue()));
            }
        }

        cubes.sort((a, b) -> Double.compare(a.depth, b.depth));

        double halfSize = 0.5;

        for (CubeDrawData cube : cubes) {
            double cx = cube.x;
            double cy = cube.y;
            double cz = cube.z;

            // Define 8 corners
            Point3D[] corners = {
                new Point3D(cx - halfSize, cy - halfSize, cz - halfSize),
                new Point3D(cx + halfSize, cy - halfSize, cz - halfSize),
                new Point3D(cx + halfSize, cy + halfSize, cz - halfSize),
                new Point3D(cx - halfSize, cy + halfSize, cz - halfSize),
                new Point3D(cx - halfSize, cy - halfSize, cz + halfSize),
                new Point3D(cx + halfSize, cy - halfSize, cz + halfSize),
                new Point3D(cx + halfSize, cy + halfSize, cz + halfSize),
                new Point3D(cx - halfSize, cy + halfSize, cz + halfSize)
            };

            Point[] projCorners = new Point[8];
            for (int i = 0; i < 8; i++) {
                double x1 = corners[i].x * Math.cos(rotateY) - corners[i].z * Math.sin(rotateY);
                double z1 = corners[i].x * Math.sin(rotateY) + corners[i].z * Math.cos(rotateY);

                double y2 = corners[i].y * Math.cos(rotateX) - z1 * Math.sin(rotateX);
                // double z2 = corners[i].y * Math.sin(rotateX) + z1 * Math.cos(rotateX);

                projCorners[i] = new Point((int) (centerX + x1 * zoom), (int) (centerY - y2 * zoom)); // Note -y2 for typical 3D to 2D
            }

            // Draw faces (painter's algorithm on faces)
            // Define 6 faces (indices of corners)
            int[][] faces = {
                {0, 1, 2, 3}, // Front (z - halfSize)
                {5, 4, 7, 6}, // Back (z + halfSize)
                {4, 0, 3, 7}, // Left
                {1, 5, 6, 2}, // Right
                {4, 5, 1, 0}, // Bottom
                {3, 2, 6, 7}  // Top
            };

            // The value is meant to be a color. For this simple editor, let's just
            // treat the integer value as an RGB color if possible, or fall back to a base color.
            // Assuming value is an RGB integer:
            Color baseColor = new Color(cube.value);
            if (cube.value == 1) { // Default value in VoxelModel is 1
                baseColor = new Color(200, 200, 200);
            }

            int r = baseColor.getRed();
            int gCol = baseColor.getGreen(); // 'g' is used for Graphics
            int b = baseColor.getBlue();

            // Shading multipliers for different faces
            double[] shading = {
                1.0,  // Front
                0.7,  // Back
                0.8,  // Left
                0.9,  // Right
                0.5,  // Bottom
                1.1   // Top
            };

            // Calculate normals and draw if facing camera
            for (int i = 0; i < 6; i++) {
                int[] f = faces[i];
                Polygon p = new Polygon();
                for (int j = 0; j < 4; j++) {
                    p.addPoint(projCorners[f[j]].x, projCorners[f[j]].y);
                }

                // Backface culling using cross product
                int ax = projCorners[f[1]].x - projCorners[f[0]].x;
                int ay = projCorners[f[1]].y - projCorners[f[0]].y;
                int bx = projCorners[f[2]].x - projCorners[f[1]].x;
                int by = projCorners[f[2]].y - projCorners[f[1]].y;

                int crossz = ax * by - ay * bx;

                if (crossz > 0) { // Facing camera
                    int faceR = Math.min(255, Math.max(0, (int)(r * shading[i])));
                    int faceG = Math.min(255, Math.max(0, (int)(gCol * shading[i])));
                    int faceB = Math.min(255, Math.max(0, (int)(b * shading[i])));

                    g2d.setColor(new Color(faceR, faceG, faceB));
                    g2d.fillPolygon(p);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(p);
                }
            }
        }

        // Draw 3D Axes
        draw3DAxis(g2d, centerX, centerY, avgX, avgY, avgZ);
    }

    private void draw3DAxis(Graphics2D g2d, int centerX, int centerY, double avgX, double avgY, double avgZ) {
        double axisLength = 5.0;

        // Origin (0,0,0) offset by avg center
        double ox = 0 - avgX;
        double oy = 0 - avgY;
        double oz = 0 - avgZ;

        // Helper to project 3D to 2D using existing math
        java.util.function.BiFunction<Point3D, Color, Void> drawAxis = (endPt, col) -> {
            double ex = endPt.x - avgX;
            double ey = endPt.y - avgY;
            double ez = endPt.z - avgZ;

            // Origin projection
            double ox1 = ox * Math.cos(rotateY) - oz * Math.sin(rotateY);
            double oz1 = ox * Math.sin(rotateY) + oz * Math.cos(rotateY);
            double oy2 = oy * Math.cos(rotateX) - oz1 * Math.sin(rotateX);
            int pxO = (int) (centerX + ox1 * zoom);
            int pyO = (int) (centerY - oy2 * zoom);

            // End point projection
            double ex1 = ex * Math.cos(rotateY) - ez * Math.sin(rotateY);
            double ez1 = ex * Math.sin(rotateY) + ez * Math.cos(rotateY);
            double ey2 = ey * Math.cos(rotateX) - ez1 * Math.sin(rotateX);
            int pxE = (int) (centerX + ex1 * zoom);
            int pyE = (int) (centerY - ey2 * zoom);

            g2d.setColor(col);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(pxO, pyO, pxE, pyE);
            g2d.setStroke(new BasicStroke(1.0f));

            return null;
        };

        // X-axis (Red)
        drawAxis.apply(new Point3D(axisLength, 0, 0), Color.RED);
        // Y-axis (Green)
        drawAxis.apply(new Point3D(0, axisLength, 0), Color.GREEN);
        // Z-axis (Blue)
        drawAxis.apply(new Point3D(0, 0, axisLength), Color.BLUE);

        // Draw labels
        java.util.function.Consumer<Point3D> drawLabel = (pt) -> {
            double lx = pt.x - avgX;
            double ly = pt.y - avgY;
            double lz = pt.z - avgZ;

            double lx1 = lx * Math.cos(rotateY) - lz * Math.sin(rotateY);
            double lz1 = lx * Math.sin(rotateY) + lz * Math.cos(rotateY);
            double ly2 = ly * Math.cos(rotateX) - lz1 * Math.sin(rotateX);
            int pX = (int) (centerX + lx1 * zoom);
            int pY = (int) (centerY - ly2 * zoom);

            String label = "";
            if (pt.x > 0) label = "X";
            if (pt.y > 0) label = "Y";
            if (pt.z > 0) label = "Z";

            g2d.setColor(Color.BLACK);
            g2d.drawString(label, pX + 5, pY + 5);
        };

        drawLabel.accept(new Point3D(axisLength + 0.5, 0, 0));
        drawLabel.accept(new Point3D(0, axisLength + 0.5, 0));
        drawLabel.accept(new Point3D(0, 0, axisLength + 0.5));
    }

    private static class VoxelDrawData {
        double x, y, depth;
        Vector3D v;

        public VoxelDrawData(double x, double y, double depth, Vector3D v) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.v = v;
        }
    }

    private static class CubeDrawData {
        double x, y, z, depth;
        int value;

        public CubeDrawData(double x, double y, double z, double depth, int value) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.depth = depth;
            this.value = value;
        }
    }

    private static class Point3D {
        double x, y, z;
        public Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
