package io.github.seal139.jSwarm.backend.hip;

import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.core.Platform;
import io.github.seal139.jSwarm.misc.Common;
import sun.misc.Unsafe;

/**
 * CUDA low level driver. This class cannot be instantiated
 */
public final class Hip implements Platform {

    private static Hip obj;

    public static Hip getInstance() throws Error, HipException {
        if (obj == null) {
            obj = new Hip();
        }

        return obj;
    }

    // ===================================

    private final String version;
    private final String platform;

    private HipDevice[] exec;

    private Hip() throws Error, HipException {
        if (!HipDriver.isLoaded()) {
            throw new Error(HipDriver.getException());
        }

        this.version  = HipDriver.hipGetVersion();
        this.platform = HipDriver.hipGetPlatform();

        final Unsafe mem = Common.getMemoryManagement();

        final long intptr = HipDriver.hipEnumerateDevices();

        int errorCode = mem.getInt(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new HipException(errorCode);
        }

        final int length = mem.getInt(intptr + 4L);
        this.exec = new HipDevice[length];

        for (int devicePtr = 0; devicePtr < length; devicePtr++) {
            this.exec[devicePtr] = new HipDevice(devicePtr);
        }

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);
    }

    @Override
    public String getName() { return this.platform; }

    @Override
    public String getFullName() { return "AMD HIP"; }

    @Override
    public boolean isPrimary() { return true; }

    @Override
    public String getVersion() { return this.version; }

    @Override
    public Executor[] getDevices() { return this.exec.clone(); }
}
