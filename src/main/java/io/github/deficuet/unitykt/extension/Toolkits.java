package io.github.deficuet.unitykt.extension;

import java.nio.ByteBuffer;

public class Toolkits {
    private Toolkits() {  }

    public static void copyBlockBuffer(byte[] buffer, byte[] out, int bx, int by, int w, int h, int bw, int bh) {
        int x = bw * bx * 4;
        int xl = (bw * (bx + 1) > w ? w - bw * bx : bw) * 4;
        int bufferEnd = bw * bh * 4;
        for (
            int y = by * bh * 4, bufferPtr = 0;
            bufferPtr < bufferEnd && y < h * 4;
            bufferPtr += bw * 4, y += 4
        ) {
            System.arraycopy(buffer, bufferPtr, out, y * w + x, xl);
        }
    }

    public static void putInt(byte[] array, int index, int value) {
        System.arraycopy(
            ByteBuffer.allocate(4).putInt(value).array(), 0, array, index * 4, 4
        );
    }

    public static int compareArrayUnsigned(byte[] a1, byte[] a2) {
        for (int i = 0; i < Math.min(a1.length, a2.length); i++) {
            int result = Integer.compare(a1[i] & 0xff, a2[i] & 0xff);
            if (result != 0) return result;
        }
        return Integer.compare(a1.length, a2.length);
    }
}
