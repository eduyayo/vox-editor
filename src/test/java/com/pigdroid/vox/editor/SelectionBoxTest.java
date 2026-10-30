package com.pigdroid.vox.editor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SelectionBoxTest {
    @Test
    public void testSelectionBox() {
        SelectionBox box = new SelectionBox(1, 2, 3, 4, 5, 6);
        assertEquals(1, box.getMinX());
        assertEquals(2, box.getMinY());
        assertEquals(3, box.getMinZ());
        assertEquals(4, box.getMaxX());
        assertEquals(5, box.getMaxY());
        assertEquals(6, box.getMaxZ());

        box.setMinX(10);
        box.setMinY(20);
        box.setMinZ(30);
        box.setMaxX(40);
        box.setMaxY(50);
        box.setMaxZ(60);

        assertEquals(10, box.getMinX());
        assertEquals(20, box.getMinY());
        assertEquals(30, box.getMinZ());
        assertEquals(40, box.getMaxX());
        assertEquals(50, box.getMaxY());
        assertEquals(60, box.getMaxZ());
    }

    @Test
    public void testClone() {
        SelectionBox box = new SelectionBox(1, 2, 3, 4, 5, 6);
        SelectionBox cloned = box.clone();
        assertNotSame(box, cloned);
        assertEquals(box.getMinX(), cloned.getMinX());
        assertEquals(box.getMaxZ(), cloned.getMaxZ());
    }
}
