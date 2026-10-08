package dev.fe2o3;

/** JNI ABI v2: owned arrays plus a best-effort Blaze3D backend preference. */
public final class NativeBridge {
    private NativeBridge() { }

    public static native void initialize(int[] colorTables, String backendPreference);
    public static native int[] generate(int[] argb, int width, int height, int levels);
    public static native void shutdown();
}
