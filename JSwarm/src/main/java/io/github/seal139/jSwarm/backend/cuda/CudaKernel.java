package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.runtime.datatype.Vector;

public class CudaKernel implements Kernel {

    private final long   address;
    private final String name;

    long getAddress() { return address; }

    CudaKernel(CudaModule module, String name) throws CudaException {
        this.name = name;
        long r[] = CudaDriver.cudaGetKernel(module.getAddress(), name);

        if (r[0] != 0l) {
            throw new CudaException(r[0]);
        }

        this.address = r[1];
    }

    @Override
    public String getName() { return name; }

    @Override
    public void setArgument(int index, Vector<? extends Number> data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setGlobalNDSize(long... dim) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLocalNDSize(long... dim) {
        // TODO Auto-generated method stub

    }

}
