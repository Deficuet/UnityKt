package io.github.deficuet.unitykt.extension;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ETCDecoder {
    private ETCDecoder() {  }

    private static final int[] WRITE_ORDER_TABLE = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};

    private static final int[] WRITE_ORDER_TABLE_REV = {15, 11, 7, 3, 14, 10, 6, 2, 13, 9, 5, 1, 12, 8, 4, 0};

    private static final int[][] ETC1_MODIFIER_TABLE = {
        {2, 8},   {5, 17},  {9, 29},   {13, 42},
        {18, 60}, {24, 80}, {33, 106}, {47, 183}
    };

    private static final int[][][] ETC2_MODIFIER_TABLE = {
        {
            {0, 8}, {0, 17}, {0, 29}, {0, 42}, {0, 60}, {0, 80}, {0, 106}, {0, 183}
        },
        {
            {2, 8}, {5, 17}, {9, 29}, {13, 42}, {18, 60}, {24, 80}, {33, 106}, {47, 183}
        }
    };

    private static final int[][] ETC1_SUB_BLOCK_TABLE = {
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
        {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1}
    };

    private static final byte[] ETC2_DISTANCE_TABLE = {3, 6, 11, 16, 23, 32, 41, 64};

    private static final byte[][] ETC2_ALPHA_MOD_TABLE = {
        {-3, -6, -9, -15, 2, 5, 8, 14}, {-3, -7, -10, -13, 2, 6, 9, 12}, {-2, -5, -8, -13, 1, 4, 7, 12},
        {-2, -4, -6, -13, 1, 3, 5, 12}, {-3, -6, -8, -12, 2, 5, 7, 11},  {-3, -7, -9, -11, 2, 6, 8, 10},
        {-4, -7, -8, -11, 3, 6, 7, 10}, {-3, -5, -8, -11, 2, 4, 7, 10},  {-2, -6, -8, -10, 1, 5, 7, 9},
        {-2, -5, -8, -10, 1, 4, 7, 9},  {-2, -4, -8, -10, 1, 3, 7, 9},   {-2, -5, -7, -10, 1, 4, 6, 9},
        {-3, -4, -7, -10, 2, 3, 6, 9},  {-1, -2, -3, -10, 0, 1, 2, 9},   {-4, -6, -8, -9, 3, 5, 7, 8},
        {-3, -5, -7, -9, 2, 4, 6, 8}
    };

    private static byte clamp(int n) {
        return (byte)(n < 0 ? 0 : Math.min(n, 255));
    }

    private static int getColor(byte[] c, int m) {
        return Color.color(
            clamp((c[0] & 0xff) + m),
            clamp((c[1] & 0xff) + m),
            clamp((c[2] & 0xff) + m),
            (byte) 255
        );
    }

    private static int getColorRaw(byte[] c) {
        return Color.color(c[0], c[1], c[2], (byte) 255);
    }

    public static void decodeETC2A8(byte[] data, final int width, final int height, byte[] out) {
        final int blocksX = (width + 3) / 4;
        final int blocksY = (height + 3) / 4;
        byte[] buffer = new byte[64];
        for (int by = 0, dataPtr = 0; by < blocksY; by++) {
            for (int bx = 0; bx < blocksX; bx++, dataPtr += 16) {
                decodeETC2Block(Arrays.copyOfRange(data, dataPtr + 8, data.length), buffer);
                decodeETC2A8Block(Arrays.copyOfRange(data, dataPtr, data.length), buffer);
                Toolkits.copyBlockBuffer(buffer, out, bx, by, width, height, 4, 4);
            }
        }
    }

    private static void decodeETC2Block(byte[] data, byte[] buffer) {
        int j = (data[6] & 0xff) << 8 | (data[7] & 0xff);
        int k = (data[4] & 0xff) << 8 | (data[5] & 0xff);
        byte[][] c = new byte[3][3];
        if (((data[3] & 0xff) & 2) != 0) {
            int r = (data[0] & 0xff) & 0xf8;
            int dr = ((data[0] & 0xff) << 3 & 0x18) - ((data[0] & 0xff) << 3 & 0x20);
            int g = (data[1] & 0xff) & 0xf8;
            int dg = ((data[1] & 0xff) << 3 & 0x18) - ((data[1] & 0xff) << 3 & 0x20);
            int b = (data[2] & 0xff) & 0xf8;
            int db = ((data[2] & 0xff) << 3 & 0x18) - ((data[2] & 0xff) << 3 & 0x20);
            if (r + dr < 0 || r + dr > 255) {
                c[0][0] = (byte) (((data[0] & 0xff) << 3 & 0xc0) | ((data[0] & 0xff) << 4 & 0x30) |
                                  ((data[0] & 0xff) >> 1 & 0xc) | ((data[0] & 0xff) & 3));
                c[0][1] = (byte) (((data[1] & 0xff) & 0xf0) | (data[1] & 0xff) >> 4);
                c[0][2] = (byte) (((data[1] & 0xff) & 0x0f) | (data[1] & 0xff) << 4);
                c[1][0] = (byte) (((data[2] & 0xff) & 0xf0) | (data[2] & 0xff) >> 4);
                c[1][1] = (byte) (((data[2] & 0xff) & 0x0f) | (data[2] & 0xff) << 4);
                c[1][2] = (byte) (((data[3] & 0xff) & 0xf0) | (data[3] & 0xff) >> 4);
                int d = ETC2_DISTANCE_TABLE[((data[3] & 0xff) >> 1 & 6) | ((data[3] & 0xff) & 1)];
                int[] colorSet = {getColorRaw(c[0]), getColor(c[1], d), getColorRaw(c[1]), getColor(c[1], -d)};
                k <<= 1;
                for (int i = 0; i < 16; i++, j >>= 1, k >>= 1) {
                    Toolkits.putInt(buffer, WRITE_ORDER_TABLE[i], colorSet[(k & 2) | (j & 1)]);
                }
            } else if (g + dg < 0 || g + dg > 255) {
                c[0][0] = (byte) (((data[0] & 0xff) << 1 & 0xf0) | ((data[0] & 0xff) >> 3 & 0xf));
                c[0][1] = (byte) (((data[0] & 0xff) << 5 & 0xe0) | ((data[1] & 0xff) & 0x10));
                c[0][1] |= (c[0][1] & 0xff) >> 4;
                c[0][2] = (byte) (((data[1] & 0xff) & 8) | ((data[1] & 0xff) << 1 & 6) | (data[2] & 0xff) >> 7);
                c[0][2] |= (c[0][2] & 0xff) << 4;
                c[1][0] = (byte) (((data[2] & 0xff) << 1 & 0xf0) | ((data[2] & 0xff) >> 3 & 0xf));
                c[1][1] = (byte) (((data[2] & 0xff) << 5 & 0xe0) | ((data[3] & 0xff) >> 3 & 0x10));
                c[1][1] |= (c[1][1] & 0xff) >> 4;
                c[1][2] = (byte) (((data[3] & 0xff) << 1 & 0xf0) | ((data[3] & 0xff) >> 3 & 0xf));
                int d = ((data[3] & 0xff) & 4) | ((data[3] & 0xff) << 1 & 2);
                if (Toolkits.compareArray(c[0], c[1]) >= 0) ++d;
                d = ETC2_DISTANCE_TABLE[d];
                int[] colorSet = {getColor(c[0], d), getColor(c[0], -d), getColor(c[1], d), getColor(c[1], -d)};
                k <<= 1;
                for (int i = 0; i < 16; i++, j >>= 1, k >>= 1) {
                    Toolkits.putInt(buffer, WRITE_ORDER_TABLE[i], colorSet[(k & 2) | (j & 1)]);
                }
            } else if (b + db < 0 || b + db > 255) {
                c[0][0] = (byte) (((data[0] & 0xff) << 1 & 0xfc) | ((data[0] & 0xff) >> 5 & 3));
                c[0][1] = (byte) (((data[0] & 0xff) << 7 & 0x80) | ((data[1] & 0xff) & 0x7e) |
                        ((data[0] & 0xff) & 1));
                c[0][2] = (byte) (((data[1] & 0xff) << 7 & 0x80) | ((data[2] & 0xff) << 2 & 0x60) |
                                  ((data[2] & 0xff) << 3 & 0x18) | ((data[3] & 0xff) >> 5 & 4));
                c[0][2] |= (c[0][2] & 0xff) >> 6;
                c[1][0] = (byte) (((data[3] & 0xff) << 1 & 0xf8) | ((data[3] & 0xff) << 2 & 4) |
                        ((data[3] & 0xff) >> 5 & 3));
                c[1][1] = (byte) (((data[4] & 0xff) & 0xfe) | (data[4] & 0xff) >> 7);
                c[1][2] = (byte) (((data[4] & 0xff) << 7 & 0x80) | ((data[5] & 0xff) >> 1 & 0x7c));
                c[1][2] |= (c[1][2] & 0xff) >> 6;
                c[2][0] = (byte) (((data[5] & 0xff) << 5 & 0xe0) | ((data[6] & 0xff) >> 3 & 0x1c) |
                        ((data[5] & 0xff) >> 1 & 3));
                c[2][1] = (byte) (((data[6] & 0xff) << 3 & 0xf8) | ((data[7] & 0xff) >> 5 & 0x6) |
                        ((data[6] & 0xff) >> 4 & 1));
                c[2][2] = (byte) ((data[7] & 0xff) << 2 | ((data[7] & 0xff) >> 4 & 3));
                for (int y = 0, i = 0; y < 4; y++) {
                    for (int x = 0; x < 4; x++, i++) {
                        byte rr = clamp(
                            (x * ((c[1][0] & 0xff) - (c[0][0] & 0xff)) +
                                    y * ((c[2][0] & 0xff) - (c[0][0] & 0xff)) +
                                    4 * (c[0][0] & 0xff) + 2) >> 2
                        );
                        byte gg = clamp(
                            (x * ((c[1][1] & 0xff) - (c[0][1] & 0xff)) +
                                    y * ((c[2][1] & 0xff) - (c[0][1] & 0xff)) +
                                    4 * (c[0][1] & 0xff) + 2) >> 2
                        );
                        byte bb = clamp(
                            (x * ((c[1][2] & 0xff) - (c[0][2] & 0xff)) +
                                    y * ((c[2][2] & 0xff) - (c[0][2] & 0xff)) +
                                    4 * (c[0][2] & 0xff) + 2) >> 2
                        );
                        Toolkits.putInt(buffer, i, Color.color(rr, gg, bb, (byte) 255));
                    }
                }
            } else {
                int[] code = {(data[3] & 0xff) >> 5, (data[3] & 0xff) >> 2 & 7};
                int[] table = ETC1_SUB_BLOCK_TABLE[(data[3] & 0xff) & 1];
                c[0][0] = (byte) (r | r >> 5);
                c[0][1] = (byte) (g | g >> 5);
                c[0][2] = (byte) (b | b >> 5);
                c[1][0] = (byte) (r + dr);
                c[1][1] = (byte) (g + dg);
                c[1][2] = (byte) (b + db);
                c[1][0] |= (c[1][0] & 0xff) >> 5;
                c[1][1] |= (c[1][1] & 0xff) >> 5;
                c[1][2] |= (c[1][2] & 0xff) >> 5;
                for (int i = 0; i < 16; i++, j >>= 1, k >>= 1) {
                    int s = table[i];
                    int m = ETC1_MODIFIER_TABLE[code[s]][j & 1];
                    Toolkits.putInt(buffer, WRITE_ORDER_TABLE[i], getColor(c[s], (k & 1) != 0 ? -m : m));
                }
            }
        } else {
            int[] code = {(data[3] & 0xff) >> 5, (data[3] & 0xff) >> 2 & 7};
            int[] table = ETC1_SUB_BLOCK_TABLE[(data[3] & 0xff) & 1];
            c[0][0] = (byte) (((data[0] & 0xff) & 0xf0) | (data[0] & 0xff) >> 4);
            c[1][0] = (byte) (((data[0] & 0xff) & 0x0f) | (data[0] & 0xff) << 4);
            c[0][1] = (byte) (((data[1] & 0xff) & 0xf0) | (data[1] & 0xff) >> 4);
            c[1][1] = (byte) (((data[1] & 0xff) & 0x0f) | (data[1] & 0xff) << 4);
            c[0][2] = (byte) (((data[2] & 0xff) & 0xf0) | (data[2] & 0xff) >> 4);
            c[1][2] = (byte) (((data[2] & 0xff) & 0x0f) | (data[2] & 0xff) << 4);
            for (int i = 0; i < 16; i++, j >>= 1, k >>= 1) {
                int s = table[i];
                int m = ETC1_MODIFIER_TABLE[code[s]][j & 1];
                Toolkits.putInt(buffer, WRITE_ORDER_TABLE[i], getColor(c[s], (k & 1) != 0 ? -m : m));
            }
        }
    }

    private static void decodeETC2A8Block(byte[] data, byte[] buffer) {
        if (((data[1] & 0xff) & 0xf0) != 0) {
            int multiplier = (data[1] & 0xff) >> 4;
            byte[] table = ETC2_ALPHA_MOD_TABLE[(data[1] & 0xff) & 0xf];
            long l = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 8)).getLong();
            for (int i = 0; i < 16; i++, l >>>= 3) {
                buffer[WRITE_ORDER_TABLE_REV[i] * 4 + 3] =
                        clamp((data[0] & 0xff) + multiplier * table[(int) (l & 7)]);
            }
        } else {
            for (int i = 0; i < 16; i++) {
                buffer[i * 4 + 3] = data[0];
            }
        }
    }
}
