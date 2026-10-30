package com.pigdroid.vox.editor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

public class VoxelModelTest {
    @Test
    public void testSetVoxel() {
        VoxelModel model = new VoxelModel();
        model.setVoxel(1, 2, 3, 0xFF00FF);
        assertEquals(0xFF00FF, model.getVoxel(1, 2, 3));
    }

    @Test
    public void testAddProjectionEuropean() {
        VoxelModel model = new VoxelModel();
        model.setReferenceSystem(ReferenceSystem.EUROPEAN);
        model.addProjection("Top", 1, 2, 0xFF0000);
        model.addProjection("Front", 1, 3, 0xFF0000);
        assertNotNull(model.getVoxel(1, 3, 2));
        assertEquals(0xFF0000, model.getVoxel(1, 3, 2));
    }

    @Test
    public void testAddProjectionAmerican() {
        VoxelModel model = new VoxelModel();
        model.setReferenceSystem(ReferenceSystem.AMERICAN);
        model.addProjection("Top", 1, 2, 0xFF0000);
        model.addProjection("Front", 1, 3, 0xFF0000);
        assertNotNull(model.getVoxel(1, 2, 3));
        assertEquals(0xFF0000, model.getVoxel(1, 2, 3));
    }

    @Test
    public void testMappedUVEuropean() {
        VoxelModel model = new VoxelModel();
        model.setReferenceSystem(ReferenceSystem.EUROPEAN);
        Vector3D vec = new Vector3D(1, 2, 3);
        assertEquals(1, model.getMappedU("Front", vec));
        assertEquals(2, model.getMappedV("Front", vec));
        assertEquals(1, model.getMappedU("Back", vec));
        assertEquals(2, model.getMappedV("Back", vec));
        assertEquals(1, model.getMappedU("Top", vec));
        assertEquals(3, model.getMappedV("Top", vec));
        assertEquals(1, model.getMappedU("Bottom", vec));
        assertEquals(3, model.getMappedV("Bottom", vec));
        assertEquals(3, model.getMappedU("Left", vec));
        assertEquals(2, model.getMappedV("Left", vec));
        assertEquals(3, model.getMappedU("Right", vec));
        assertEquals(2, model.getMappedV("Right", vec));
    }

    @Test
    public void testMappedUVAmerican() {
        VoxelModel model = new VoxelModel();
        model.setReferenceSystem(ReferenceSystem.AMERICAN);
        Vector3D vec = new Vector3D(1, 2, 3);
        assertEquals(1, model.getMappedU("Front", vec));
        assertEquals(3, model.getMappedV("Front", vec));
        assertEquals(1, model.getMappedU("Back", vec));
        assertEquals(3, model.getMappedV("Back", vec));
        assertEquals(1, model.getMappedU("Top", vec));
        assertEquals(2, model.getMappedV("Top", vec));
        assertEquals(1, model.getMappedU("Bottom", vec));
        assertEquals(2, model.getMappedV("Bottom", vec));
        assertEquals(2, model.getMappedU("Left", vec));
        assertEquals(3, model.getMappedV("Left", vec));
        assertEquals(2, model.getMappedU("Right", vec));
        assertEquals(3, model.getMappedV("Right", vec));
    }
}
