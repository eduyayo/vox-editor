package com.pigdroid.vox.editor.io;

import com.pigdroid.vox.editor.Vector3D;
import com.pigdroid.vox.editor.VoxelModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoxFile {

    private static final int[] DEFAULT_PALETTE = {
            0x00000000, 0xffffffff, 0xffccffff, 0xff99ffff, 0xff66ffff, 0xff33ffff, 0xff00ffff, 0xffffccff, 0xffccccff, 0xff99ccff, 0xff66ccff, 0xff33ccff, 0xff00ccff, 0xffff99ff, 0xffcc99ff, 0xff9999ff,
            0xff6699ff, 0xff3399ff, 0xff0099ff, 0xffff66ff, 0xffcc66ff, 0xff9966ff, 0xff6666ff, 0xff3366ff, 0xff0066ff, 0xffff33ff, 0xffcc33ff, 0xff9933ff, 0xff6633ff, 0xff3333ff, 0xff0033ff, 0xffff00ff,
            0xffcc00ff, 0xff9900ff, 0xff6600ff, 0xff3300ff, 0xff0000ff, 0xffffffcc, 0xffccffcc, 0xff99ffcc, 0xff66ffcc, 0xff33ffcc, 0xff00ffcc, 0xffffcccc, 0xffcccccc, 0xff99cccc, 0xff66cccc, 0xff33cccc,
            0xff00cccc, 0xffff99cc, 0xffcc99cc, 0xff9999cc, 0xff6699cc, 0xff3399cc, 0xff0099cc, 0xffff66cc, 0xffcc66cc, 0xff9966cc, 0xff6666cc, 0xff3366cc, 0xff0066cc, 0xffff33cc, 0xffcc33cc, 0xff9933cc,
            0xff6633cc, 0xff3333cc, 0xff0033cc, 0xffff00cc, 0xffcc00cc, 0xff9900cc, 0xff6600cc, 0xff3300cc, 0xff0000cc, 0xffffff99, 0xffccff99, 0xff99ff99, 0xff66ff99, 0xff33ff99, 0xff00ff99, 0xffffcc99,
            0xffcccc99, 0xff99cc99, 0xff66cc99, 0xff33cc99, 0xff00cc99, 0xffff9999, 0xffcc9999, 0xff999999, 0xff669999, 0xff339999, 0xff009999, 0xffff6699, 0xffcc6699, 0xff996699, 0xff666699, 0xff336699,
            0xff006699, 0xffff3399, 0xffcc3399, 0xff993399, 0xff663399, 0xff333399, 0xff003399, 0xffff0099, 0xffcc0099, 0xff990099, 0xff660099, 0xff330099, 0xff000099, 0xffffff66, 0xffccff66, 0xff99ff66,
            0xff66ff66, 0xff33ff66, 0xff00ff66, 0xffffcc66, 0xffcccc66, 0xff99cc66, 0xff66cc66, 0xff33cc66, 0xff00cc66, 0xffff9966, 0xffcc9966, 0xff999966, 0xff669966, 0xff339966, 0xff009966, 0xffff6666,
            0xffcc6666, 0xff996666, 0xff666666, 0xff336666, 0xff006666, 0xffff3366, 0xffcc3366, 0xff993366, 0xff663366, 0xff333366, 0xff003366, 0xffff0066, 0xffcc0066, 0xff990066, 0xff660066, 0xff330066,
            0xff000066, 0xffffff33, 0xffccff33, 0xff99ff33, 0xff66ff33, 0xff33ff33, 0xff00ff33, 0xffffcc33, 0xffcccc33, 0xff99cc33, 0xff66cc33, 0xff33cc33, 0xff00cc33, 0xffff9933, 0xffcc9933, 0xff999933,
            0xff669933, 0xff339933, 0xff009933, 0xffff6633, 0xffcc6633, 0xff996633, 0xff666633, 0xff336633, 0xff006633, 0xffff3333, 0xffcc3333, 0xff993333, 0xff663333, 0xff333333, 0xff003333, 0xffff0033,
            0xffcc0033, 0xff990033, 0xff660033, 0xff330033, 0xff000033, 0xffffff00, 0xffccff00, 0xff99ff00, 0xff66ff00, 0xff33ff00, 0xff00ff00, 0xffffcc00, 0xffcccc00, 0xff99cc00, 0xff66cc00, 0xff33cc00,
            0xff00cc00, 0xffff9900, 0xffcc9900, 0xff999900, 0xff669900, 0xff339900, 0xff009900, 0xffff6600, 0xffcc6600, 0xff996600, 0xff666600, 0xff336600, 0xff006600, 0xffff3300, 0xffcc3300, 0xff993300,
            0xff663300, 0xff333300, 0xff003300, 0xffff0000, 0xffcc0000, 0xff990000, 0xff660000, 0xff330000, 0xff0000ee, 0xff0000dd, 0xff0000bb, 0xff0000aa, 0xff000088, 0xff000077, 0xff000055, 0xff000044,
            0xff000022, 0xff000011, 0xff00ee00, 0xff00dd00, 0xff00bb00, 0xff00aa00, 0xff008800, 0xff007700, 0xff005500, 0xff004400, 0xff002200, 0xff001100, 0xffee0000, 0xffdd0000, 0xffbb0000, 0xffaa0000,
            0xff880000, 0xff770000, 0xff550000, 0xff440000, 0xff220000, 0xff110000, 0xffeeeeee, 0xffdddddd, 0xffbbbbbb, 0xffaaaaaa, 0xff888888, 0xff777777, 0xff555555, 0xff444444, 0xff222222, 0xff111111
    };

    public static VoxelModel read(File file) throws IOException {
        VoxelModel model = new VoxelModel();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileBytes = fis.readAllBytes();
            ByteBuffer buffer = ByteBuffer.wrap(fileBytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            // Read Magic
            byte[] magic = new byte[4];
            buffer.get(magic);
            if (!Arrays.equals(magic, new byte[]{'V', 'O', 'X', ' '})) {
                throw new IOException("Not a valid VOX file");
            }

            int version = buffer.getInt();
            if (version != 150) {
                // Warning, we support 150 but usually it's fine for newer versions
            }

            // Read MAIN chunk
            byte[] mainId = new byte[4];
            buffer.get(mainId);
            if (!Arrays.equals(mainId, new byte[]{'M', 'A', 'I', 'N'})) {
                throw new IOException("MAIN chunk not found");
            }

            int mainContentSize = buffer.getInt();
            int mainChildrenSize = buffer.getInt();

            int sizeX = 0, sizeY = 0, sizeZ = 0;
            int[][] voxelData = null; // store xyzc
            int[] palette = Arrays.copyOf(DEFAULT_PALETTE, 256); // Use default palette if none exists

            // Read children
            while (buffer.hasRemaining()) {
                byte[] chunkIdBytes = new byte[4];
                buffer.get(chunkIdBytes);
                String chunkId = new String(chunkIdBytes);

                int chunkContentSize = buffer.getInt();
                int chunkChildrenSize = buffer.getInt();

                int startPos = buffer.position();

                if (chunkId.equals("SIZE")) {
                    sizeX = buffer.getInt();
                    sizeY = buffer.getInt();
                    sizeZ = buffer.getInt();
                } else if (chunkId.equals("XYZI")) {
                    int numVoxels = buffer.getInt();
                    voxelData = new int[numVoxels][4];
                    for (int i = 0; i < numVoxels; i++) {
                        int x = buffer.get() & 0xFF;
                        int y = buffer.get() & 0xFF;
                        int z = buffer.get() & 0xFF;
                        int colorIndex = buffer.get() & 0xFF;
                        voxelData[i][0] = x;
                        voxelData[i][1] = y;
                        voxelData[i][2] = z;
                        voxelData[i][3] = colorIndex;
                    }
                } else if (chunkId.equals("RGBA")) {
                    for (int i = 0; i < 256; i++) {
                        int r = buffer.get() & 0xFF;
                        int g = buffer.get() & 0xFF;
                        int b = buffer.get() & 0xFF;
                        int a = buffer.get() & 0xFF;
                        // Build color int (ARGB)
                        int color = (a << 24) | (r << 16) | (g << 8) | b;
                        // For palette, index 1-255 map to 0-254.
                        palette[i] = color;
                    }
                }

                buffer.position(startPos + chunkContentSize);
            }

            if (voxelData != null) {
                for (int[] v : voxelData) {
                    int x = v[0];
                    int y = v[1];
                    int z = v[2];
                    int colorIndex = v[3];

                    int color = palette[colorIndex - 1]; // colorIndex in file is 1..255, matching index 0..254 in palette array

                    // Center the model? Or just place it as is.
                    model.setVoxel(x - sizeX/2, y - sizeY/2, z - sizeZ/2, color);
                }
            }
        }
        return model;
    }

    public static void write(VoxelModel model, File file) throws IOException {
        Map<Vector3D, Integer> voxels = model.getVoxels();

        // 1. Shift coordinates to be positive and calculate size
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (Vector3D v : voxels.keySet()) {
            minX = Math.min(minX, v.x());
            minY = Math.min(minY, v.y());
            minZ = Math.min(minZ, v.z());
            maxX = Math.max(maxX, v.x());
            maxY = Math.max(maxY, v.y());
            maxZ = Math.max(maxZ, v.z());
        }

        int sizeX = voxels.isEmpty() ? 0 : maxX - minX + 1;
        int sizeY = voxels.isEmpty() ? 0 : maxY - minY + 1;
        int sizeZ = voxels.isEmpty() ? 0 : maxZ - minZ + 1;

        // 2. Extract Palette
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Integer> colorToIndex = new HashMap<>(); // Color to 1-based index

        for (Integer color : voxels.values()) {
            if (!colorToIndex.containsKey(color)) {
                palette.add(color);
                colorToIndex.put(color, palette.size()); // 1-based index
                if (palette.size() == 255) {
                    break; // Max 255 colors supported
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Calculate sizes
            int sizeChunkContentSize = 12; // 3 * 4 bytes (int)
            int sizeChunkTotalSize = 12 + sizeChunkContentSize;

            int xyziChunkContentSize = 4 + voxels.size() * 4; // numVoxels (int) + voxels * 4 bytes
            int xyziChunkTotalSize = 12 + xyziChunkContentSize;

            int rgbaChunkContentSize = 256 * 4; // 256 colors * 4 bytes
            int rgbaChunkTotalSize = 12 + rgbaChunkContentSize;

            int mainChildrenSize = sizeChunkTotalSize + xyziChunkTotalSize + rgbaChunkTotalSize;

            // Build the byte buffer
            ByteBuffer buffer = ByteBuffer.allocate(8 + 12 + mainChildrenSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            // Magic
            buffer.put(new byte[]{'V', 'O', 'X', ' '});
            buffer.putInt(150); // version

            // MAIN
            buffer.put(new byte[]{'M', 'A', 'I', 'N'});
            buffer.putInt(0); // MAIN content size is 0
            buffer.putInt(mainChildrenSize); // MAIN children size

            // SIZE
            buffer.put(new byte[]{'S', 'I', 'Z', 'E'});
            buffer.putInt(sizeChunkContentSize);
            buffer.putInt(0); // no children
            buffer.putInt(sizeX);
            buffer.putInt(sizeY);
            buffer.putInt(sizeZ);

            // XYZI
            buffer.put(new byte[]{'X', 'Y', 'Z', 'I'});
            buffer.putInt(xyziChunkContentSize);
            buffer.putInt(0); // no children
            buffer.putInt(voxels.size());

            for (Map.Entry<Vector3D, Integer> entry : voxels.entrySet()) {
                Vector3D v = entry.getKey();
                int color = entry.getValue();

                int px = v.x() - minX;
                int py = v.y() - minY;
                int pz = v.z() - minZ;
                int cIndex = colorToIndex.getOrDefault(color, 1);

                buffer.put((byte)(px & 0xFF));
                buffer.put((byte)(py & 0xFF));
                buffer.put((byte)(pz & 0xFF));
                buffer.put((byte)(cIndex & 0xFF));
            }

            // RGBA
            buffer.put(new byte[]{'R', 'G', 'B', 'A'});
            buffer.putInt(rgbaChunkContentSize);
            buffer.putInt(0); // no children

            for (int i = 0; i < 256; i++) {
                int c = 0;
                if (i < palette.size()) {
                    c = palette.get(i);
                } else if (i < 256) {
                    c = DEFAULT_PALETTE[i];
                }

                int a = (c >> 24) & 0xFF;
                int r = (c >> 16) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = c & 0xFF;

                // MagicaVoxel stores palette as R, G, B, A bytes per color
                buffer.put((byte)r);
                buffer.put((byte)g);
                buffer.put((byte)b);
                buffer.put((byte)a);
            }

            fos.write(buffer.array());
        }
    }
}
