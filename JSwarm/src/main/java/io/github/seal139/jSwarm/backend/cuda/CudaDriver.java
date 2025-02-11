package io.github.seal139.jSwarm.backend.cuda;

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
final class CudaDriver {
    private static final boolean   loaded;
    private static final Throwable exception;

    private static final String PLATFORM = "cuda";

    static {
        boolean   b = false;
        Throwable t = null;

        try {
//            System.load(Common.getDriverByOs(PLATFORM, "cudriver"));
            System.load("C:\\wsl\\Programming\\Swarm\\C\\swarmcu\\out\\build\\x64-release\\swarmcu.dll");
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
    static native String cudaGetVersion();

    static String cudaGetPlatform() {
        return PLATFORM;
    }

    // Device (n Device)
    static native long cudaEnumerateDevices(); // Ok

    static native long cudaGetDeviceInfo(int deviceIndex);
    static native String cudaGetDeviceName(int deviceIndex);

    // Context (1 device : n Context [1 default])
    static native long cudaCreateContext(int device); // Ok
    static native int cudaDeleteContext(long context); // Ok

    static native int cudaSetContext(long context);

    // Queue (1 Context : n Queue)
    static native long cudaAddQueue(int count); // Ok
    static native int cudaDeleteQueue(long[] queue, int count); // Ok

    // Module (1 Context : n Module)
    static native long cudaCreateProgram(String source); // Ok
    static native int cudaDeleteProgram(long module); // Ok

    // Kernel (1 Module : n Kernel)
    static native long cudaGetKernel(long program, String name); // Ok

    // ==== Launcher ====
    static native void cudaLaunch(long kernel, long queue, //
                                  int x, int y, int z, //
                                  int lx, int ly, int lz, //
                                  long[] arguments, int count); // Ok

    // ==== buffer memory management ====

    static native long cudaHook(long size); // ok

    static native int cudaSyncDataTo(long queue, long hostMemory, long deviceMemory, long size); // Ok
    static native int cudaSyncDataFrom(long queue, long hostMemory, long deviceMemory, long size); // Ok

    static native int cudaUnhook(long hookAddress); // Ok

    // ==== Fence ====
    static native int cudaWaitAll(); // Native not yet
}
