package io.github.seal139.jSwarm.backend.ocl;

import java.util.Map;
import java.util.WeakHashMap;

import io.github.seal139.jSwarm.backend.Executor;
import io.github.seal139.jSwarm.backend.Platform;
import io.github.seal139.jSwarm.misc.Common;
import sun.misc.Unsafe;

/**
 * CUDA low level driver. This class cannot be instantiated
 */
public final class Ocl implements Platform {

    private static Ocl obj;

    public static Ocl getInstance() throws Error, OclException {
        if (obj == null) {
            obj = new Ocl();
        }

        return obj;
    }

    // ===================================

    private final String version;
    private final String platform;

    private OclDevice[] exec;

    private final Map<Thread, Long> activeContext = new WeakHashMap<>();

    long getContextValue() { return this.activeContext.get(Thread.currentThread()).longValue(); }

    void setContext(long value) {
        this.activeContext.put(Thread.currentThread(), value);
    }

    private Ocl() throws Error, OclException {
        if (!OclDriver.isLoaded()) {
            throw new Error(OclDriver.getException());
        }

        this.version  = OclDriver.oclGetVersion();
        this.platform = OclDriver.oclGetPlatform();

        final Unsafe mem = Common.getMemoryManagement();

        final long intptr = OclDriver.oclEnumerateDevices();

        int errorCode = mem.getInt(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new OclException(errorCode);
        }

        final int length = mem.getInt(intptr + 8L);
        this.exec = new OclDevice[length];

        long lBound = intptr + 16L;
        long uBound = lBound + (length * 8);

        int indexer = -1;
        for (long devicePtr = lBound; devicePtr < uBound; devicePtr += 8) {
            this.exec[++indexer] = new OclDevice(mem.getLong(devicePtr), indexer);
        }

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);
    }

    @Override
    public String getName() { return this.platform; }

    @Override
    public String getFullName() { return "Open Computing Language"; }

    @Override
    public boolean isPrimary() { return true; }

    @Override
    public String getVersion() { return this.version; }

    @Override
    public Executor[] getDevices() { return this.exec.clone(); }
}
