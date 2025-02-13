package io.github.seal139.jSwarm.backend.hip;

import io.github.seal139.jSwarm.core.DeviceType;
import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.misc.Common;
import sun.misc.Unsafe;

public class HipDevice implements Executor {
    private final DeviceType type = DeviceType.GPU_ACCELERATOR;

    private final int  deviceId;
    private HipContext context;

    private final String name;
    private final long   computeUnit;
    private final long   totalMemory;
    private final int    maxNdRange = 3;
    private final long[] maxNdRangeVal;
    private final long   maxThreadNdRange;
    private final long   flops;

    HipDevice(int index) throws HipException {
        this.deviceId = index;

        final Unsafe mem = Common.getMemoryManagement();

        this.name = HipDriver.hipGetDeviceName(index);
        final long infos = HipDriver.hipGetDeviceInfo(index);
        {
            this.computeUnit   = mem.getLong(infos);
            this.totalMemory   = mem.getLong(8L + infos);
            this.maxNdRangeVal = new long[] {
                    mem.getLong(16 + infos),             // x
                    mem.getLong(24L + infos),            // y
                    mem.getLong(32L + infos)             // z
            };

            this.maxThreadNdRange = mem.getLong(40L + infos);
            this.flops            = mem.getLong(48L + infos);
        }
        mem.freeMemory(infos);
    }

    int getDeviceId() { return this.deviceId; }

    @Override
    public HipContext getDefaultContext() throws HipException {
        int err = HipDriver.hipSetDevice(getDeviceId());

        if (err != 0) {
            throw new HipException(err);
        }

        if ((this.context == null) || this.context.isClosed()) {
            this.context = new HipContext(this);
        }

        return this.context;
    }

    // ===== Information only =====

    @Override
    public DeviceType getType() { return this.type; }

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
    public long getMaxThreadPerDimension() { return this.maxThreadNdRange; }

    @Override
    public long getFlops() { return this.flops; }

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
        return (obj.hashCode() == hashCode()) && (obj instanceof HipDevice);
    }
}
