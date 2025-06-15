package io.github.seal139.jSwarm.backend.jvm;

import io.github.seal139.jSwarm.backend.BackendException;
import io.github.seal139.jSwarm.backend.Context;
import io.github.seal139.jSwarm.backend.Executor;
import io.github.seal139.jSwarm.runtime.DeviceType;

public class JvmDevice implements Executor {

    private final JvmContext context = new JvmContext();

    @Override
    public Context getDefaultContext() throws BackendException { return this.context; }

    @Override
    public DeviceType getType() { return DeviceType.CPU; }

    @Override
    public String getUuid() { // TODO Auto-generated method stub
        return "6e6f4d0e-4b33-4d9e-92e4-fb69b0ef51c6";
    }

    @Override
    public String getName() { // TODO Auto-generated method stub
        return "JVM";
    }

    @Override
    public long getComputeUnit() { // TODO Auto-generated method stub
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public long getTotalMemory() { // TODO Auto-generated method stub
        return Runtime.getRuntime().maxMemory();
    }

    @Override
    public int getMaxNDRange() { // TODO Auto-generated method stub
        return 3;
    }

    @Override
    public long[] getMaxGlobalSize() { // TODO Auto-generated method stub
        return new long[] {
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
    }

    @Override
    public long[] getMaxLocalSize() { // TODO Auto-generated method stub
        return new long[] {
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
    }

    @Override
    public long getMaxLocalThread() { return Integer.MAX_VALUE; }

    @Override
    public long getFlops() { return 0; }

}
