package io.github.seal139.jSwarm.runtime.datatype;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seal139.jSwarm.core.NativeCleaner;

public final class LongVector extends Vector<Long> {

    static final class DeallocatorImpl implements Deallocator {
        private final ExecutorService synchronizer = Executors.newSingleThreadExecutor();

        private final long address;
        private boolean    isClosed = false;

        private DeallocatorImpl(long address) {
            this.address = address;
        }

        @Override
        public void clean() {
            this.synchronizer.shutdown();

            int64Clear(this.address);
            int64Delete(this.address);

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public LongVector(int bufferSize, int bucketSize) {
        super(bufferSize, bucketSize);

        this.deallocator = new DeallocatorImpl(int64Construct());
        NativeCleaner.register(this);
    }

    @Override
    public void close() throws Exception {
        this.deallocator.clean();
    }

    @Override
    public boolean isClosed() { return this.deallocator.isClosed; }

    // ============ Functionality Operation ==================

    @Override
    protected ExecutorService getSynchronizer() { return this.deallocator.synchronizer; }

    @Override
    public int size() {
        return int64Size(this.deallocator.address);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Long) {
            return int64Remove(this.deallocator.address, ((Long) o).longValue());
        }

        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Long) {
            return int64Contains(this.deallocator.address, ((Long) o).longValue());
        }

        return false;
    }

    @Override
    public void clear() {
        int64Clear(this.deallocator.address);
    }

    @Override
    protected void sycnhronizeTo(Number[] t, int size) {
        long[] f = new long[size];

        for (int i = 0; i < size; i++) {
            f[i] = t[i].longValue();
        }

        int64Sync(this.deallocator.address, f, size);
    }

    @Override
    protected Number[] sycnhronizeFrom(int beginIndex, int endIndex) {
        int size = (1 + endIndex) - beginIndex;

        long[] from = int64Fetch(this.deallocator.address, beginIndex, endIndex);
        Long[] f    = new Long[size];

        for (int i = 0; i < size; i++) {
            f[i] = from[i];
        }

        return f;
    }

    @Override
    protected Long convert(Number n) {
        return n.longValue();
    }

    // -----======= Native Operation =======-----

    private static native long int64Construct();
    private static native void int64Delete(long address);

    private static native int int64Size(long address);
    private static native void int64Sync(long address, long[] num, int size);
    private static native long[] int64Fetch(long address, int begin, int to);

    private static native boolean int64Contains(long address, long num);
    private static native boolean int64Remove(long address, long num);
    private static native void int64Clear(long address);
}
