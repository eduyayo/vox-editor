package com.pigdroid.vox.editor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Vector3DTest {
    @Test
    public void testVector3D() {
        Vector3D vec = new Vector3D(1, 2, 3);
        assertEquals(1, vec.x());
        assertEquals(2, vec.y());
        assertEquals(3, vec.z());
    }
}
