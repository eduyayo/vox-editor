package com.pigdroid.vox.editor;

public class SelectionBox implements Cloneable {
    private int minX, minY, minZ, maxX, maxY, maxZ;

    public SelectionBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public int getMinX() { return minX; }
    public void setMinX(int minX) { this.minX = minX; }

    public int getMinY() { return minY; }
    public void setMinY(int minY) { this.minY = minY; }

    public int getMinZ() { return minZ; }
    public void setMinZ(int minZ) { this.minZ = minZ; }

    public int getMaxX() { return maxX; }
    public void setMaxX(int maxX) { this.maxX = maxX; }

    public int getMaxY() { return maxY; }
    public void setMaxY(int maxY) { this.maxY = maxY; }

    public int getMaxZ() { return maxZ; }
    public void setMaxZ(int maxZ) { this.maxZ = maxZ; }

    @Override
    public SelectionBox clone() {
        return new SelectionBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
