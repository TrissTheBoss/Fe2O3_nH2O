package dev.fe2o3;

/** JNI ABI v1: owned ARGB int arrays, no retained Java memory or native pointers. */
public final class NativeBridge {
    private NativeBridge() { }

    public static native void initialize(int[] colorTables);
    public static native int[] generate(int[] argb, int width, int height, int levels);
    public static native void shutdown();
}

