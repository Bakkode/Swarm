package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.DeviceType;
import io.github.seal139.jSwarm.core.Executor;

public class CudaDevice implements Executor {

    private final long  address;
    private CudaContext context;

    CudaDevice(long ptr) throws CudaException {
        address = ptr;
        context = new CudaContext(address);
    }

    long getAddress() { return address; }

    @Override
    public CudaContext getDefaultContext() throws CudaException {
        if (context.isClosed()) {
            context = newContext();
        }

        return context;
    }

    @Override
    public CudaContext newContext() throws CudaException {
        return new CudaContext(address);
    }

    // ===== Information only =====

    @Override
    public DeviceType getType() { return null; }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getComputeUnit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getTotalMemory() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxNDRange() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long[] getMaxIndexPerDimension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long[] getMaxThreadPerDimension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxThread() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getFlops() {
        // TODO Auto-generated method stub
        return 0;
    }

}
