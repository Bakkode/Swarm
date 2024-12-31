package io.github.seal139.jSwarm.runtime.datatype;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seal139.jSwarm.core.NativeCleaner;

public final class IntegerVector extends Vector<Integer> {

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

            int32Clear(this.address);
            int32Delete(this.address);

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public IntegerVector(int bufferSize, int bucketSize) {
        super(bufferSize, bucketSize);

        this.deallocator = new DeallocatorImpl(int32Construct());
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
        return int32Size(this.deallocator.address);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Integer) {
            return int32Remove(this.deallocator.address, ((Integer) o).intValue());
        }

        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            return int32Contains(this.deallocator.address, ((Integer) o).intValue());
        }

        return false;
    }

    @Override
    public void clear() {
        int32Clear(this.deallocator.address);
    }

    @Override
    protected void sycnhronizeTo(Number[] t, int size) {
        int[] f = new int[size];

        for (int i = 0; i < size; i++) {
            f[i] = t[i].intValue();
        }

        int32Sync(this.deallocator.address, f, size);
    }

    @Override
    protected Number[] sycnhronizeFrom(int beginIndex, int endIndex) {
        int size = (1 + endIndex) - beginIndex;

        int[]     from = int32Fetch(this.deallocator.address, beginIndex, endIndex);
        Integer[] f    = new Integer[size];

        for (int i = 0; i < size; i++) {
            f[i] = from[i];
        }

        return f;
    }

    @Override
    protected Integer convert(Number n) {
        return n.intValue();
    }

    // -----======= Native Operation =======-----

    private static native long int32Construct();
    private static native void int32Delete(long address);

    private static native int int32Size(long address);
    private static native void int32Sync(long address, int[] num, int size);
    private static native int[] int32Fetch(long address, int begin, int to);

    private static native boolean int32Contains(long address, int num);
    private static native boolean int32Remove(long address, int num);
    private static native void int32Clear(long address);
}
