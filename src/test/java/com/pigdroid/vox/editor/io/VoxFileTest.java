package com.pigdroid.vox.editor.io;

import com.pigdroid.vox.editor.VoxelModel;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class VoxFileTest {
    @Test
    public void testReadWrite() throws Exception {
        VoxelModel model = new VoxelModel();
        model.setVoxel(1, 2, 3, 0xFF0000FF);
        File tempFile = File.createTempFile("test", ".vox");
        tempFile.deleteOnExit();
        VoxFile.write(model, tempFile);

        VoxelModel loadedModel = VoxFile.read(tempFile);
        assertNotNull(loadedModel.getVoxel(0, 0, 0));
        assertEquals(0xFF0000FF, loadedModel.getVoxel(0, 0, 0));
    }

    @Test
    public void testReadInvalidMagic() throws Exception {
        File tempFile = File.createTempFile("test_invalid", ".vox");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[] { 'I', 'N', 'V', 'L' });
            fos.write(new byte[] { 0, 0, 0, 0 });
        }

        Exception exception = assertThrows(IOException.class, () -> {
            VoxFile.read(tempFile);
        });
        assertEquals("Not a valid VOX file", exception.getMessage());
    }

    @Test
    public void testReadMissingMain() throws Exception {
        File tempFile = File.createTempFile("test_nomain", ".vox");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[] { 'V', 'O', 'X', ' ' });
            fos.write(new byte[] { (byte) 150, 0, 0, 0 }); // version 150
            fos.write(new byte[] { 'N', 'O', 'T', 'M' }); // some other chunk
            fos.write(new byte[] { 0, 0, 0, 0 });
            fos.write(new byte[] { 0, 0, 0, 0 });
        }

        Exception exception = assertThrows(IOException.class, () -> {
            VoxFile.read(tempFile);
        });
        assertEquals("MAIN chunk not found", exception.getMessage());
    }

    @Test
    public void testEmptyModel() throws Exception {
        VoxelModel model = new VoxelModel();
        File tempFile = File.createTempFile("test_empty", ".vox");
        tempFile.deleteOnExit();
        VoxFile.write(model, tempFile);

        VoxelModel loadedModel = VoxFile.read(tempFile);
        assertTrue(loadedModel.getVoxels().isEmpty());
    }
}
