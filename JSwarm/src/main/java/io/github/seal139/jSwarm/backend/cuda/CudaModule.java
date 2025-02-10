package io.github.seal139.jSwarm.backend.cuda;

import java.util.HashMap;
import java.util.Map;

import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.NativeCleaner;
import io.github.seal139.jSwarm.core.NativeCleaner.DeallocatedException;
import io.github.seal139.jSwarm.misc.Common;
import io.github.seal139.jSwarm.misc.Log;
import sun.misc.Unsafe;

// Status: Development done

public class CudaModule implements Module {

    static final class DeallocatorImpl implements Deallocator {
        private final long address;
        private final long ctxAddress;

        private boolean isClosed = false;

        private DeallocatorImpl(long address, long ctxAddress) {
            this.address    = address;
            this.ctxAddress = ctxAddress;
        }

        @Override
        public void clean() {
            if (this.isClosed) {
                return;
            }

            int r2 = CudaDriver.cudaDeleteProgram(this.ctxAddress, this.address);
            if (r2 != 0) {
                Log.error(new CudaException(r2));
            }

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    private final CudaContext ctx;

    CudaModule(CudaContext ctx, String source) throws CudaException {
        final Unsafe mem = Common.getMemoryManagement();

        final long intptr = CudaDriver.cudaCreateProgram(ctx.getAddress(), source);

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new CudaException(errorCode);
        }

        this.ctx = ctx;

        this.deallocator = new DeallocatorImpl(mem.getLong(intptr + 8), ctx.getAddress());

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);
        NativeCleaner.register(this);
    }

    @Override
    public void close() throws Exception {
        this.deallocator.clean();
    }

    @Override
    public boolean isClosed() { return this.deallocator.isClosed; }

    // ============ Functionality Operation ==================

    private final Map<String, CudaKernel> kernelMap = new HashMap<>();

    @Override
    public CudaKernel getKernel(String kernelName) throws CudaException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        CudaKernel kernel = this.kernelMap.get(kernelName);
        if (kernel == null) {
            kernel = new CudaKernel(this.ctx, this, kernelName);
            this.kernelMap.put(kernelName, kernel);
        }

        return kernel;
    }

    // =============== Object Operation ======================

    long getAddress() { return this.deallocator.address; }

    @Override
    public int hashCode() {
        return (int) getAddress();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj.hashCode() == hashCode() //
        ) && (obj instanceof CudaModule //
        ) && (((CudaModule) obj).getAddress() == getAddress());
    }
}
