package io.github.seal139.jSwarm.backend.cuda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.seal139.jSwarm.core.Log;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.NativeCleaner;

public class CudaModule implements Module {

    static final class DeallocatorImpl implements Deallocator {
        private final long       address;
        private final List<Long> kernelAddress = new ArrayList<>();

        private DeallocatorImpl(long address) {
            this.address = address;
        }

        @Override
        public void clean() {
            long[] addr = new long[kernelAddress.size()];
            for (int i = 0; i < kernelAddress.size(); i++) {
                addr[i] = kernelAddress.get(i).longValue();
            }

            long r1 = CudaDriver.cudaDeleteKernel(addr, kernelAddress.size());
            if (r1 != 0l) {
                Log.error(new CudaException(r1));
            }

            long r2 = CudaDriver.cudaDeleteProgram(address);
            if (r2 != 0l) {
                Log.error(new CudaException(r2));
            }
        }
    }

    private final DeallocatorImpl         deallocator;
    private final Map<String, CudaKernel> kernelMap = new HashMap<>();

    @Override
    public Deallocator getDeallocator() { return deallocator; }

    CudaModule(CudaContext ctx, String source) throws CudaException {
        long[] r = CudaDriver.cudaCreateProgram(ctx.getAddress(), source);

        if (r[0] != 0l) {
            throw new CudaException(r[0]);
        }

        deallocator = new DeallocatorImpl(r[1]);
        NativeCleaner.register(this);
    }

    long getAddress() { return deallocator.address; }

    @Override
    public void close() throws Exception {
        deallocator.clean();
    }

    @Override
    public CudaKernel getKernel(String kernelName) throws CudaException {
        CudaKernel kernel = kernelMap.get(kernelName);
        if (kernel != null) {
            return kernel;
        }

        kernel = new CudaKernel(this, kernelName);
        deallocator.kernelAddress.add(kernel.getAddress());

        kernelMap.put(kernelName, kernel);

        return kernel;
    }

    // =================================================================================

    @Override
    public int hashCode() {
        // Use native memory address instead
        return (int) getAddress();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == hashCode() && obj instanceof CudaModule && ((CudaModule) obj).getAddress() == getAddress();
    }
}
