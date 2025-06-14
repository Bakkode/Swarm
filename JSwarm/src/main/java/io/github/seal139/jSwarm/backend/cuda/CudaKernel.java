package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.backend.Kernel;
import io.github.seal139.jSwarm.misc.Common;
import sun.misc.Unsafe;

public class CudaKernel implements Kernel {

    private final long   address;
    private final String name;

    long getAddress() { return this.address; }

    CudaKernel(CudaContext context, CudaModule module, String name) throws CudaException {
        final Unsafe mem = Common.getMemoryManagement();

        long intptr = CudaDriver.cudaGetKernel(module.getAddress(), name);

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new CudaException(errorCode);
        }

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);

        this.name    = name;
        this.address = mem.getLong(intptr + 8);
    }

    @Override
    public String getName() { return this.name; }

    // ==== Object ====
    @Override
    public String toString() {
        return getName() + "@" + String.valueOf(this.address);
    }

    @Override
    public int hashCode() {
        return (int) this.address;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj.hashCode() == hashCode()) //
               && (obj instanceof CudaKernel) //
               && (((CudaKernel) obj).address == this.address);
    }
}
