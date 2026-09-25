package com.pigdroid.vox.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoxelModel {
    private final Map<Vector3D, Integer> voxels;
    private final List<Projection> projections;

    public VoxelModel() {
        this.voxels = new HashMap<>();
        this.projections = new ArrayList<>();
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

    public List<Projection> getProjections() {
        return new ArrayList<>(projections);
    }

    public void addProjection(String viewName, int u, int v) {
        Projection newProjection = new Projection(viewName, u, v);
        projections.add(newProjection);

        for (Projection p : projections) {
            if (p == newProjection) continue;

            Integer x = null;
            Integer y = null;
            Integer z = null;

            // Resolve from new projection
            switch (newProjection.viewName()) {
                case "Front": x = newProjection.u(); y = newProjection.v(); break;
                case "Top": x = newProjection.u(); z = newProjection.v(); break;
                case "Bottom": x = newProjection.u(); z = newProjection.v(); break;
                case "Left": z = newProjection.u(); y = newProjection.v(); break;
                case "Right": z = newProjection.u(); y = newProjection.v(); break;
            }

            // Resolve from existing projection
            boolean conflict = false;
            switch (p.viewName()) {
                case "Front":
                    if (x != null && x != p.u()) conflict = true; else x = p.u();
                    if (y != null && y != p.v()) conflict = true; else y = p.v();
                    break;
                case "Top":
                case "Bottom":
                    if (x != null && x != p.u()) conflict = true; else x = p.u();
                    if (z != null && z != p.v()) conflict = true; else z = p.v();
                    break;
                case "Left":
                case "Right":
                    if (z != null && z != p.u()) conflict = true; else z = p.u();
                    if (y != null && y != p.v()) conflict = true; else y = p.v();
                    break;
            }

            if (!conflict && x != null && y != null && z != null) {
                setVoxel(x, y, z, 1);
            }
        }
    }
}
