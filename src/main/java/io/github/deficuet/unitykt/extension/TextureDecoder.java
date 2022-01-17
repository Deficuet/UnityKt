package io.github.deficuet.unitykt.extension;

public final class TextureDecoder {
    private TextureDecoder() {  }

    static {
        System.loadLibrary("TextureDecoder");
    }

    public static native void decodeDXT1(byte[] data, int w, int h, byte[] out);
    public static native void decodeDXT5(byte[] data, int w, int h, byte[] out);
    public static native void decodeBC4(byte[] data, int w, int h, byte[] out);
    public static native void decodeBC5(byte[] data, int w, int h, byte[] out);
    public static native void decodeBC6(byte[] data, int w, int h, byte[] out);
    public static native void decodeBC7(byte[] data, int w, int h, byte[] out);
    public static native void decodePVRTC(byte[] data, int w, int h, byte[] out, boolean is2bpp);
    public static native void decodeETC1(byte[] data, int w, int h, byte[] out);
    public static native void decodeETC2(byte[] data, int w, int h, byte[] out);
    public static native void decodeETC2A1(byte[] data, int w, int h, byte[] out);
    public static native void decodeETC2A8(byte[] data, int w, int h, byte[] out);
    public static native void decodeATCRGB4(byte[] data, int w, int h, byte[] out);
    public static native void decodeATCRGBA8(byte[] data, int w, int h, byte[] out);
    public static native void decodeASTC(byte[] data, int w, int h, byte[] out, int blockSize);
    public static native void decodeEACR(byte[] data, int w, int h, byte[] out);
    public static native void decodeEACRSigned(byte[] data, int w, int h, byte[] out);
    public static native void decodeEACRG(byte[] data, int w, int h, byte[] out);
    public static native void decodeEACRGSigned(byte[] data, int w, int h, byte[] out);
    public static native byte[] unpackCrunch(byte[] data);
    public static native byte[] unpackUnityCrunch(byte[] data);
}
