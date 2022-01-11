package io.github.deficuet.unitykt.extension;

public class TextureDecoder {
    private TextureDecoder() {  }

    public static native void decodeETC2A8(byte[] data, int w, int h, byte[] out);
    public static native void unpackCrunch(byte[] data);
}
