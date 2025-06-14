package io.github.seal139.jSwarm.backend.hip;

/**
 * Driver native API <br/>
 * <br/>
 *
 * Accessing native method is dangerous and can cause crash or memory leak. This
 * part will is not visible to public and was intended for internal use.
 * However, access through reflection is still possible with extra caution.
 * <br/>
 * <br/>
 * Please bear in mind that this class implementation can be changed with no
 * guarantee of backward compatibility as this class was not intended for public
 * use.
 */
final class HipDriver {
    private static final boolean   loaded;
    private static final Throwable exception;

    private static final String PLATFORM = "AMD HIP";

    static {
        boolean   b = false;
        Throwable t = null;

        try {
//            System.load(Common.getDriverByOs(PLATFORM, "cudriver"));
            System.load("C:\\wsl\\Programming\\Swarm\\C\\swarmhip\\out\\build\\x64-release\\swarmhip.dll");
            b = true;
        }
        catch (Throwable e) {
            t = e;
        }

        loaded    = b;
        exception = t;
    }

    static boolean isLoaded() { return loaded; }

    static Throwable getException() { return exception; }

    // ==== Functionality ====
    static native String hipGetVersion();

    static String hipGetPlatform() {
        return PLATFORM;
    }

    // Device (n Device)
    static native long hipEnumerateDevices(); // Ok

    static native long hipGetDeviceInfo(int deviceIndex);
    static native String hipGetDeviceName(int deviceIndex);

    static native int hipSetDevice(int deviceIndex);

    // Queue (1 Context (device) : n Queue)
    static native long hipAddQueue(int count); // Ok
    static native int hipDeleteQueue(long[] queue, int count); // Ok

    // Module (1 Context : n Module)
    static native long hipCreateProgram(String source); // Ok
    static native int hipDeleteProgram(long module); // Ok

    // Kernel (1 Module : n Kernel)
    static native long hipGetKernel(long program, String name); // Ok

    // ==== Launcher ====
    static native void hipLaunch(long kernel, long queue, //
                                 int x, int y, int z, //
                                 int lx, int ly, int lz, //
                                 long[] arguments, int count); // Ok

    // ==== buffer memory management ====

    static native long hipHook(long size); // ok

    static native int hipSyncDataTo(long queue, long hostMemory, long deviceMemory, long size); // Ok
    static native int hipSyncDataFrom(long queue, long hostMemory, long deviceMemory, long size); // Ok

    static native int hipUnhook(long hookAddress); // Ok

    // ==== Fence ====
    static native int hipWaitAll(); // Native not yet
}
