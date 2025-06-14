package io.github.seal139.jSwarm.backend.hip;

import java.util.HashMap;
import java.util.Map;

import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.misc.Common;
import io.github.seal139.jSwarm.misc.Log;
import io.github.seal139.jSwarm.misc.NativeCleaner;
import io.github.seal139.jSwarm.misc.NativeCleaner.DeallocatedException;
import sun.misc.Unsafe;

// Status: Development done

public class HipModule implements Module {

    static final class DeallocatorImpl implements Deallocator {
        private final long address;

        private boolean isClosed = false;

        private DeallocatorImpl(long address) {
            this.address = address;
        }

        @Override
        public void clean() {
            if (this.isClosed) {
                return;
            }

            int r2 = HipDriver.hipDeleteProgram(this.address);
            if (r2 != 0) {
                Log.error(new HipException(r2));
            }

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    private final HipContext ctx;

    HipModule(HipContext ctx, String source) throws HipException {
        final Unsafe mem = Common.getMemoryManagement();

        final long intptr = HipDriver.hipCreateProgram(source);

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new HipException(errorCode);
        }

        this.ctx = ctx;

        this.deallocator = new DeallocatorImpl(mem.getLong(intptr + 8));

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

    private final Map<String, HipKernel> kernelMap = new HashMap<>();

    @Override
    public HipKernel getKernel(String kernelName) throws HipException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        HipKernel kernel = this.kernelMap.get(kernelName);
        if (kernel == null) {
            kernel = new HipKernel(this.ctx, this, kernelName);
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
        ) && (obj instanceof HipModule //
        ) && (((HipModule) obj).getAddress() == getAddress());
    }
}
