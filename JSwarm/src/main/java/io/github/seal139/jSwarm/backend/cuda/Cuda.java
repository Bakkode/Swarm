package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.core.Platform;

/**
 * CUDA low level driver. This class cannot be instantiated
 */
public final class Cuda implements Platform {

    private final String version;
    private final String platform;

    private Executor[] exec;

    private Cuda() throws Error {
        if (!CudaDriver.isLoaded()) {
            throw new Error(CudaDriver.getException());
        }

        this.version  = CudaDriver.getVersion();
        this.platform = CudaDriver.getPlatform();
    }

    @Override
    public void init() throws CudaException {
        final long[] devices = CudaDriver.cudaEnumerateCuDevice();
        final int    length  = devices.length;

        exec = new Executor[length];

        for (int i = 0; i < length; i++) {
            exec[i] = new CudaDevice(devices[i]);
        }
    }

    @Override
    public String getName() { return platform; }

    @Override
    public String getFullName() { return "NVIDIA CUDA"; }

    @Override
    public boolean isPrimary() { return true; }

    @Override
    public String getVersion() { return version; }

    @Override
    public Executor[] getDevices() { return exec.clone(); }
}
