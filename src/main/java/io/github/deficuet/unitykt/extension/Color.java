//color.h

package io.github.deficuet.unitykt.extension;

public class Color {
    private Color() {  }

    public static int color(byte r, byte g, byte b, byte a) {
        return (a & 0xff) | (r & 0xff) << 8 | (g & 0xff) << 16 | (b & 0xff) << 24;
    }
}
