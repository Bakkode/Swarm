package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.Context;
import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.Queue;
import io.github.seal139.jSwarm.misc.Common;

/**
 * Driver native API <br/>
 * <br/>
 *
 * Accessing native method is dangerous and can cause crash or memory leak. This
 * part will is not visible to public and was intended for internal use.
 * However, access through reflection is still possible with extra caution.
 */
final class CudaDriver {
    private static final boolean   loaded;
    private static final Throwable exception;

    private static final String PLATFORM = "cuda";

    static {
        boolean   b = false;
        Throwable t = null;

        try {
            System.load(Common.getDriverByOs(PLATFORM, "cudriver"));
            b = true;
        }
        catch (Throwable e) {
            t = e;
        }

        loaded    = b;
        exception = t;
    }

    static boolean isLoaded() { return loaded; }

    static String getPlatform() { return PLATFORM; }

    static Throwable getException() { return exception; }

    /**
     * Get runtime version
     * 
     * @return Runtime version
     */
    static native String getVersion();

    /**
     * Enumerate all device on platform.
     * 
     * @return [0] Error code. <br/>
     *         [1] Address of array of {@link Executor}.
     */
    static native long[] cudaEnumerateCuDevice();

    /**
     * Initialize default configuration
     * 
     * @param device Pointer of {@link Executor}.
     * @return [0] Error code. <br/>
     *         [1] Pointer of {@link Context}. <br/>
     *         [2] Pointer of array of Queue.
     */
    static native long[] cudaInitDefault(long device);

    /**
     * Create CUDA context.
     *
     * @param device Pointer of {@link Executor}.
     * @return [0] Error code. <br/>
     *         [1] Pointer of {@link Context}.
     */
    static native long[] cudaCreateContext(long device);

    /**
     * Delete context and release resources used.
     * 
     * @param context Pointer of {@link Context}.
     * 
     * @return Error code
     */
    static native long cudaDeleteContext(long context);

    /**
     * Create CUDA program based on source.
     * 
     * @param context Pointer of {@link Context}.
     * @param source  Program source code that contains kernel function.
     * @return [0] Error code. <br/>
     *         [1] Pointer of {@link Module}.
     */
    static native long[] cudaCreateProgram(long context, String source);

    /**
     * Delete program and release resources used .
     *
     * @param module      Array of pointer of {@link Module}.
     * @param moduleCount Number of {@link Module} object count.
     * @return
     */
    static native long cudaDeleteProgram(long module);

    /**
     * Get CUDA kernel
     * 
     * @param program Pointer of {@link Module} that contains {@link Kernel}.
     * @param name    {@link Kernel} name.
     * 
     * @return [0] Error code. <br/>
     *         [1] Pointer of {@link Kernel}.
     */
    static native long[] cudaGetKernel(long program, String name);

    /**
     * Delete kernel and release resources used.
     * 
     * @param kernel      Array of pointer of {@link Kernel}.
     * @param kernelCount Number of {@link Kernel} object count.
     * 
     * @return Error code.
     */
    static native long cudaDeleteKernel(long[] kernel, int kernelCount);

    /**
     * Add CUDA stream (queue).
     * 
     * @param oldAddress Pointer of old queue.
     * @param oldCount   Old queue count.
     * @param addedCount new additional count (will be added with old count).
     * @return [0] Error Code. <br/>
     *         [1] Pointer of array of Queue.
     */
    static native long[] cudaCreateQueue(long oldAddress, int oldCount, int addedCount);

    static native long[] cudaDeleteQueue(long address, int count);

    /**
     * Create native container that holds kernel arguments.
     * 
     * @param argCount Argument counts.
     * @return [0] Error Code. <br/>
     *         [1] Pointer of container.
     */
    static native long[] cudaCreateArgumentCtr(int argCount);

    /**
     * Delete argument container. This function did not also delete it's content.
     * 
     * @param ctr      Array of pointer of container.
     * @param argCount Number of container object count.
     * 
     * @return Error code
     */
    static native long cudaDeleteArgumentCtr(long[] ctr, int argCount);

    /**
     * Synchronize memory from host to device.
     * 
     * @param context      Pointer of {@link Context}.
     * @param queue        Pointer of {@link Queue}.
     * @param queueIdx     {@link Queue} cyclic index.
     * @param ref          Pointer of host memory as reference data.
     * @param allocatedMem Total data size
     * 
     * @return [0] Error Code. <br/>
     *         [1] Pointer of device memory.
     */
    static native long[] cudaSyncDataTo(long context, long queue, int queueIdx, long ref, long allocatedMem);

    /**
     * Delete unused data at device memory and release resources used.
     * 
     * @param deviceMemory Array of pointer of device memory.
     * @param memCount     Number of device memory object count.
     * 
     * @return Error code.
     */
    static native long[] cudaDeleteSynchedData(long[] deviceMemory, int memCount);

    /**
     * Synchronize memory from device to host.
     * 
     * @param queue        Pointer of {@link Queue}
     * @param queueIdx     {@link Queue} cyclic index.
     * @param ref          Pointer of device memory.
     * @param target       Pointer of host memory.
     * @param allocatedMem Total data size.
     * 
     * @return Error code.
     */
    static native long cudaSyncDataFrom(long queue, int queueIdx, long ref, long target, long allocatedMem);

    /**
     * Add argument value to argument container for kernel execution
     * 
     * @param ctr      Pointer of container
     * @param argValue Array of pointer of device memory
     * @param argCount Argument count.
     */
    static native void cudaInitArgumentsToCtr(long ctr, long[] argValue, int argCount);

    /**
     * Launch kernel
     * 
     * @param context  Pointer of {@link Context}.
     * @param kernel   Pointer of {@link Kernel}.
     * @param queue    Pointer of {@link Queue}.
     * @param queueIdx {@link Queue} cyclic index.
     * @param argCtr   Pointer of container.
     * @param argCount Argument count.
     * @param gx       Global -X dimension.
     * @param gy       Global -Y dimension.
     * @param gz       Global -Z dimension.
     * @param lx       Local -X dimension.
     * @param ly       Local -Y dimension.
     * @param lz       Local -Z dimension.
     * 
     * @return Error code.
     */
    static native long cudaLaunch(long context, long kernel, long queue, int queueIdx, long argCtr, long argCount, long gx, long gy, long gz, long lx,
                                  long ly, long lz);

    /**
     * Wait for pending operation to complete.
     * 
     * @param queue      Pointer of {@link Queue}.
     * @param queueCount count of queue to wait.
     * 
     * @return Error code.
     */
    static native long cudaWaitAll(long queue, int queueCount);

    /**
     * Get error message based on CUDA error code
     *
     * @param errCode
     * @return
     */
    static native String getErrorMessage(long errCode);

    static native long getLastError();
}
