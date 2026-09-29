package com.pigdroid.vox.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoxelModel implements Cloneable {
    private final Map<Vector3D, Integer> voxels;
    private final List<Projection> projections;
    private ReferenceSystem referenceSystem = ReferenceSystem.EUROPEAN;
    private int changeCount = 0;
    private SelectionBox selection;
    private Map<String, SliceState> sliceStates = new HashMap<>();

    public static class SliceState implements Cloneable {
        public boolean vEnabled = false;
        public boolean hEnabled = false;
        public int vLine = 0;
        public int hLine = 0;

        @Override
        public SliceState clone() {
            try {
                return (SliceState) super.clone();
            } catch (CloneNotSupportedException e) {
                return new SliceState();
            }
        }
    }

    public VoxelModel() {
        this.voxels = new HashMap<>();
        this.projections = new ArrayList<>();
        String[] views = {"Front", "Back", "Top", "Bottom", "Left", "Right"};
        for (String view : views) {
            sliceStates.put(view, new SliceState());
        }
    }

    public int getChangeCount() {
        return changeCount;
    }

    public void copyFrom(VoxelModel other) {
        this.voxels.clear();
        this.voxels.putAll(other.voxels);
        this.projections.clear();
        this.projections.addAll(other.projections);
        this.referenceSystem = other.referenceSystem;
        this.selection = other.selection != null ? other.selection.clone() : null;
        this.sliceStates.clear();
        for (Map.Entry<String, SliceState> entry : other.sliceStates.entrySet()) {
            this.sliceStates.put(entry.getKey(), entry.getValue().clone());
        }
        this.changeCount++;
    }

    @Override
    public VoxelModel clone() {
        VoxelModel clone = new VoxelModel();
        clone.voxels.putAll(this.voxels);
        clone.projections.addAll(this.projections);
        clone.referenceSystem = this.referenceSystem;
        clone.selection = this.selection != null ? this.selection.clone() : null;
        clone.sliceStates = new HashMap<>();
        for (Map.Entry<String, SliceState> entry : this.sliceStates.entrySet()) {
            clone.sliceStates.put(entry.getKey(), entry.getValue().clone());
        }
        clone.changeCount = this.changeCount;
        return clone;
    }

    public SelectionBox getSelection() {
        return selection;
    }

    public void setSelection(SelectionBox selection) {
        this.selection = selection;
        this.changeCount++;
    }

    public void clearSelection() {
        if (this.selection != null) {
            this.selection = null;
            this.changeCount++;
        }
    }

    public SliceState getSliceState(String viewName) {
        return sliceStates.get(viewName);
    }

    public void setSliceState(String viewName, SliceState state) {
        sliceStates.put(viewName, state);
        changeCount++;
    }

    public boolean isGhosted(Vector3D v) {
        for (Map.Entry<String, SliceState> entry : sliceStates.entrySet()) {
            String viewName = entry.getKey();
            SliceState state = entry.getValue();

            if (!state.vEnabled && !state.hEnabled) continue;

            Integer mappedU = getMappedU(viewName, v);
            Integer mappedV = getMappedV(viewName, v);

            if (mappedU == null || mappedV == null) continue;

            if (state.vEnabled && mappedU <= state.vLine) {
                return true;
            }
            if (state.hEnabled && mappedV <= state.hLine) {
                return true;
            }
        }
        return false;
    }

    public ReferenceSystem getReferenceSystem() {
        return referenceSystem;
    }

    public void setReferenceSystem(ReferenceSystem referenceSystem) {
        if (this.referenceSystem != referenceSystem) {
            this.referenceSystem = referenceSystem;
            this.changeCount++;
        }
    }

    public Integer getMappedU(String viewName, Vector3D v) {
        if (referenceSystem == ReferenceSystem.AMERICAN) {
            // XY is floor, Z is up
            switch (viewName) {
                case "Front": return v.x();
                case "Back": return v.x();
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
                case "Back": return v.x();
                case "Top": return v.x();
                case "Bottom": return v.x();
                case "Left": return v.z();
                case "Right": return v.z();
                default: return null;
            }
        }
    }

    public Integer getMappedV(String viewName, Vector3D v) {
        if (referenceSystem == ReferenceSystem.AMERICAN) {
            // XY is floor, Z is up
            switch (viewName) {
                case "Front": return v.z();
                case "Back": return v.z();
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
                case "Back": return v.y();
                case "Top": return v.z();
                case "Bottom": return v.z();
                case "Left": return v.y();
                case "Right": return v.y();
                default: return null;
            }
        }
    }

    public boolean isUvInsideSelection(String viewName, int u, int v) {
        if (selection == null) return true;

        Vector3D minVec = new Vector3D(selection.getMinX(), selection.getMinY(), selection.getMinZ());
        Vector3D maxVec = new Vector3D(selection.getMaxX(), selection.getMaxY(), selection.getMaxZ());

        Integer u1 = getMappedU(viewName, minVec);
        Integer u2 = getMappedU(viewName, maxVec);
        Integer v1 = getMappedV(viewName, minVec);
        Integer v2 = getMappedV(viewName, maxVec);

        if (u1 == null || u2 == null || v1 == null || v2 == null) return true;

        int minU = Math.min(u1, u2);
        int maxU = Math.max(u1, u2);
        int minV = Math.min(v1, v2);
        int maxV = Math.max(v1, v2);

        return u >= minU && u <= maxU && v >= minV && v <= maxV;
    }

    public void setVoxel(int x, int y, int z, int value) {
        if (selection != null) {
            if (x < selection.getMinX() || x > selection.getMaxX() ||
                y < selection.getMinY() || y > selection.getMaxY() ||
                z < selection.getMinZ() || z > selection.getMaxZ()) {
                return;
            }
        }
        Integer old = voxels.put(new Vector3D(x, y, z), value);
        if (old == null || !old.equals(value)) {
            changeCount++;
        }
    }

    public Integer getVoxel(int x, int y, int z) {
        return voxels.get(new Vector3D(x, y, z));
    }

    public void removeVoxel(int x, int y, int z) {
        if (selection != null) {
            if (x < selection.getMinX() || x > selection.getMaxX() ||
                y < selection.getMinY() || y > selection.getMaxY() ||
                z < selection.getMinZ() || z > selection.getMaxZ()) {
                return;
            }
        }
        Integer removed = voxels.remove(new Vector3D(x, y, z));
        if (removed != null) {
            changeCount++;
        }
    }

    public Map<Vector3D, Integer> getVoxels() {
        return new HashMap<>(voxels);
    }

    public List<Projection> getProjections() {
        return new ArrayList<>(projections);
    }

    public void addProjection(String viewName, int u, int v, int colorValue) {
        if (!isUvInsideSelection(viewName, u, v)) return;

        for (Projection p : projections) {
            if (p.viewName().equals(viewName) && p.u() == u && p.v() == v) {
                // If it exists with a different color, we could update it,
                // but let's prevent adding multiple for the same position.
                // To allow painting over, we should remove the old and add the new,
                // or just update it. For now, we'll avoid duplicate list growth.
                if (p.color() == colorValue) return;

                // Let's remove the old one to replace it with the new color
                projections.remove(p);
                changeCount++;
                break;
            }
        }

        Projection newProjection = new Projection(viewName, u, v, colorValue);
        projections.add(newProjection);
        changeCount++;

        for (Projection p : projections) {
            if (p == newProjection) continue;

            Integer x = null;
            Integer y = null;
            Integer z = null;

            // Resolve from new projection
            if (referenceSystem == ReferenceSystem.AMERICAN) {
                switch (newProjection.viewName()) {
                    case "Front":
                    case "Back": x = newProjection.u(); z = newProjection.v(); break;
                    case "Top": x = newProjection.u(); y = newProjection.v(); break;
                    case "Bottom": x = newProjection.u(); y = newProjection.v(); break;
                    case "Left": y = newProjection.u(); z = newProjection.v(); break;
                    case "Right": y = newProjection.u(); z = newProjection.v(); break;
                }
            } else {
                switch (newProjection.viewName()) {
                    case "Front":
                    case "Back": x = newProjection.u(); y = newProjection.v(); break;
                    case "Top": x = newProjection.u(); z = newProjection.v(); break;
                    case "Bottom": x = newProjection.u(); z = newProjection.v(); break;
                    case "Left": z = newProjection.u(); y = newProjection.v(); break;
                    case "Right": z = newProjection.u(); y = newProjection.v(); break;
                }
            }

            // Resolve from existing projection
            boolean conflict = false;
            if (referenceSystem == ReferenceSystem.AMERICAN) {
                switch (p.viewName()) {
                    case "Front":
                    case "Back":
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
                    case "Back":
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
                Vector3D candidate = new Vector3D(x, y, z);
                if (!isGhosted(candidate)) {
                    setVoxel(x, y, z, colorValue);
                } else {
                    int dx = 0, dy = 0, dz = 0;
                    if (referenceSystem == ReferenceSystem.EUROPEAN) {
                        switch (newProjection.viewName()) {
                            case "Front": dy = 1; break;
                            case "Back": dy = -1; break;
                            case "Top": dz = -1; break;
                            case "Bottom": dz = 1; break;
                            case "Left": dx = 1; break;
                            case "Right": dx = -1; break;
                        }
                    } else {
                        switch (newProjection.viewName()) {
                            case "Front": dz = 1; break;
                            case "Back": dz = -1; break;
                            case "Top": dy = -1; break;
                            case "Bottom": dy = 1; break;
                            case "Left": dx = 1; break;
                            case "Right": dx = -1; break;
                        }
                    }
                    int cx = x; int cy = y; int cz = z;
                    boolean found = false;
                    boolean columnHasNonGhosted = false;
                    for (int i = 0; i < 100; i++) {
                        Vector3D movedCandidate = new Vector3D(x + dx * i, y + dy * i, z + dz * i);
                        if (!isGhosted(movedCandidate)) {
                            columnHasNonGhosted = true;
                            break;
                        }
                    }
                    if (columnHasNonGhosted) {
                        for (int i = 0; i < 100; i++) {
                            cx += dx; cy += dy; cz += dz;
                            Vector3D movedCandidate = new Vector3D(cx, cy, cz);
                            if (!isGhosted(movedCandidate)) {
                                setVoxel(cx, cy, cz, colorValue);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!columnHasNonGhosted || !found) {
                        setVoxel(x, y, z, colorValue);
                    }
                }
            }
        }
    }

    public void deleteProjection(String viewName, int u, int v) {
        // We do NOT check isGhosted here because projections don't have enough 3D coordinates.
        // We just delete the projection.
        boolean removed = projections.removeIf(p -> p.viewName().equals(viewName) && p.u() == u && p.v() == v);
        if (removed) {
            changeCount++;
        }

        List<Vector3D> toRemove = new ArrayList<>();
        for (Vector3D voxel : voxels.keySet()) {
            if (isGhosted(voxel)) continue;
            Integer mappedU = getMappedU(viewName, voxel);
            Integer mappedV = getMappedV(viewName, voxel);
            if (mappedU != null && mappedU == u && mappedV != null && mappedV == v) {
                toRemove.add(voxel);
            }
        }

        // If we found no non-ghosted voxels, fallback to deleting ghosted voxels
        if (toRemove.isEmpty()) {
            for (Vector3D voxel : voxels.keySet()) {
                Integer mappedU = getMappedU(viewName, voxel);
                Integer mappedV = getMappedV(viewName, voxel);
                if (mappedU != null && mappedU == u && mappedV != null && mappedV == v) {
                    toRemove.add(voxel);
                }
            }
        }

        for (Vector3D voxel : toRemove) {
            voxels.remove(voxel);
        }
    }
}
