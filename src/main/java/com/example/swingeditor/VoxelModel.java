package com.example.swingeditor;

import java.util.HashMap;
import java.util.Map;

public class VoxelModel {
    private final Map<Vector3D, Integer> voxels;

    public VoxelModel() {
        this.voxels = new HashMap<>();
    }

    public void setVoxel(int x, int y, int z, int value) {
        voxels.put(new Vector3D(x, y, z), value);
    }

    public Integer getVoxel(int x, int y, int z) {
        return voxels.get(new Vector3D(x, y, z));
    }

    public void removeVoxel(int x, int y, int z) {
        voxels.remove(new Vector3D(x, y, z));
    }

    public Map<Vector3D, Integer> getVoxels() {
        return new HashMap<>(voxels);
    }
}
