package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.DeviceType;
import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.misc.Common;
import sun.misc.Unsafe;

public class CudaDevice implements Executor {

    private final int   deviceId;
    private CudaContext context;

    private final String name;
    private final long   computeUnit;
    private final long   totalMemory;
    private final int    maxNdRange;
    private final long[] maxNdRangeVal;
    private final long[] maxThreadNdRange;
    private final int    maxThread;
    private final int    type;
    private final float  flops;

    CudaDevice(int ptr, int index) throws CudaException {
        this.deviceId = ptr;
        this.context  = new CudaContext(this);

        final Unsafe mem = Common.getMemoryManagement();

        this.name = CudaDriver.cudaGetDeviceName(index);
        final long infos = CudaDriver.cudaGetDeviceInfo(index);
        {
            this.computeUnit   = mem.getLong(infos);
            this.totalMemory   = mem.getLong(8L + infos);
            this.maxNdRange    = 3;
            this.maxNdRangeVal = new long[] {
                    mem.getLong(16 + infos), mem.getLong(24L + infos), mem.getLong(32L + infos) };

            this.maxThreadNdRange = new long[] {
                    mem.getLong(40L + infos), mem.getLong(48L + infos), mem.getLong(56 + infos) };

            this.type      = mem.getInt(64L + infos);
            this.maxThread = mem.getInt(68L + infos);

            this.flops = mem.getFloat(70L + infos);
        }
        mem.freeMemory(infos);
    }

    int getDeviceId() { return this.deviceId; }

    @Override
    public CudaContext getDefaultContext() throws CudaException {
        if (this.context.isClosed()) {
            this.context = newContext();
        }

        return this.context;
    }

    @Override
    public CudaContext newContext() throws CudaException {
        return new CudaContext(this);
    }

    // ===== Information only =====

    @Override
    public DeviceType getType() { return DeviceType.values()[this.type]; }

    @Override
    public String getName() { return this.name; }

    @Override
    public long getComputeUnit() { return this.computeUnit; }

    @Override
    public long getTotalMemory() { return this.totalMemory; }

    @Override
    public int getMaxNDRange() { return this.maxNdRange; }

    @Override
    public long[] getMaxIndexPerDimension() { return this.maxNdRangeVal; }

    @Override
    public long[] getMaxThreadPerDimension() { return this.maxThreadNdRange; }

    @Override
    public int getMaxThread() { return this.maxThread; }

    @Override
    public double getFlops() { return this.flops; }

    // ==== Object ====
    @Override
    public String toString() {
        return getName() + "(" + getType().name() + ")";
    }

    @Override
    public int hashCode() {
        return this.deviceId;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj.hashCode() == hashCode()) && (obj instanceof CudaDevice);
    }
}
