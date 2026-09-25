package com.pigdroid.vox.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoxelModel {
    private final Map<Vector3D, Integer> voxels;
    private final List<Projection> projections;
    private ReferenceSystem referenceSystem = ReferenceSystem.EUROPEAN;

    public VoxelModel() {
        this.voxels = new HashMap<>();
        this.projections = new ArrayList<>();
    }

    public ReferenceSystem getReferenceSystem() {
        return referenceSystem;
    }

    public void setReferenceSystem(ReferenceSystem referenceSystem) {
        this.referenceSystem = referenceSystem;
    }

    public Integer getMappedU(String viewName, Vector3D v) {
        if (referenceSystem == ReferenceSystem.EUROPEAN) {
            // XY is floor, Z is up
            switch (viewName) {
                case "Front": return v.x();
                case "Top": return v.x();
                case "Bottom": return v.x();
                case "Left": return v.y();
                case "Right": return v.y();
                default: return null;
            }
        } else {
            // AMERICAN: XZ is floor, Y is up
            switch (viewName) {
                case "Front": return v.x();
                case "Top": return v.x();
                case "Bottom": return v.x();
                case "Left": return v.z();
                case "Right": return v.z();
                default: return null;
            }
        }
    }

    public Integer getMappedV(String viewName, Vector3D v) {
        if (referenceSystem == ReferenceSystem.EUROPEAN) {
            // XY is floor, Z is up
            switch (viewName) {
                case "Front": return v.z();
                case "Top": return v.y();
                case "Bottom": return v.y();
                case "Left": return v.z();
                case "Right": return v.z();
                default: return null;
            }
        } else {
            // AMERICAN: XZ is floor, Y is up
            switch (viewName) {
                case "Front": return v.y();
                case "Top": return v.z();
                case "Bottom": return v.z();
                case "Left": return v.y();
                case "Right": return v.y();
                default: return null;
            }
        }
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

    public void addProjection(String viewName, int u, int v, int colorValue) {
        for (Projection p : projections) {
            if (p.viewName().equals(viewName) && p.u() == u && p.v() == v) {
                // If it exists with a different color, we could update it,
                // but let's prevent adding multiple for the same position.
                // To allow painting over, we should remove the old and add the new,
                // or just update it. For now, we'll avoid duplicate list growth.
                if (p.color() == colorValue) return;

                // Let's remove the old one to replace it with the new color
                projections.remove(p);
                break;
            }
        }

        Projection newProjection = new Projection(viewName, u, v, colorValue);
        projections.add(newProjection);

        for (Projection p : projections) {
            if (p == newProjection) continue;

            Integer x = null;
            Integer y = null;
            Integer z = null;

            // Resolve from new projection
            if (referenceSystem == ReferenceSystem.EUROPEAN) {
                switch (newProjection.viewName()) {
                    case "Front": x = newProjection.u(); z = newProjection.v(); break;
                    case "Top": x = newProjection.u(); y = newProjection.v(); break;
                    case "Bottom": x = newProjection.u(); y = newProjection.v(); break;
                    case "Left": y = newProjection.u(); z = newProjection.v(); break;
                    case "Right": y = newProjection.u(); z = newProjection.v(); break;
                }
            } else {
                switch (newProjection.viewName()) {
                    case "Front": x = newProjection.u(); y = newProjection.v(); break;
                    case "Top": x = newProjection.u(); z = newProjection.v(); break;
                    case "Bottom": x = newProjection.u(); z = newProjection.v(); break;
                    case "Left": z = newProjection.u(); y = newProjection.v(); break;
                    case "Right": z = newProjection.u(); y = newProjection.v(); break;
                }
            }

            // Resolve from existing projection
            boolean conflict = false;
            if (referenceSystem == ReferenceSystem.EUROPEAN) {
                switch (p.viewName()) {
                    case "Front":
                        if (x != null && x != p.u()) conflict = true; else x = p.u();
                        if (z != null && z != p.v()) conflict = true; else z = p.v();
                        break;
                    case "Top":
                    case "Bottom":
                        if (x != null && x != p.u()) conflict = true; else x = p.u();
                        if (y != null && y != p.v()) conflict = true; else y = p.v();
                        break;
                    case "Left":
                    case "Right":
                        if (y != null && y != p.u()) conflict = true; else y = p.u();
                        if (z != null && z != p.v()) conflict = true; else z = p.v();
                        break;
                }
            } else {
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
            }

            if (!conflict && x != null && y != null && z != null) {
                setVoxel(x, y, z, colorValue);
            }
        }
    }

    public void deleteProjection(String viewName, int u, int v) {
        projections.removeIf(p -> p.viewName().equals(viewName) && p.u() == u && p.v() == v);

        List<Vector3D> toRemove = new ArrayList<>();
        for (Vector3D voxel : voxels.keySet()) {
            Integer mappedU = getMappedU(viewName, voxel);
            Integer mappedV = getMappedV(viewName, voxel);
            if (mappedU != null && mappedU == u && mappedV != null && mappedV == v) {
                toRemove.add(voxel);
            }
        }
        for (Vector3D voxel : toRemove) {
            voxels.remove(voxel);
        }
    }
}
