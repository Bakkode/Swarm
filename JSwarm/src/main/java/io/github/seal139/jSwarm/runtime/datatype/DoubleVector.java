package io.github.seal139.jSwarm.runtime.datatype;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seal139.jSwarm.core.NativeCleaner;

public final class DoubleVector extends Vector<Double> {

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

            fp64Clear(this.address);
            fp64Delete(this.address);

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public DoubleVector(int bufferSize, int bucketSize) {
        super(bufferSize, bucketSize);

        this.deallocator = new DeallocatorImpl(fp64Construct());
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
        return fp64Size(this.deallocator.address);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Double) {
            return fp64Remove(this.deallocator.address, ((Double) o).doubleValue());
        }

        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Double) {
            return fp64Contains(this.deallocator.address, ((Double) o).doubleValue());
        }

        return false;
    }

    @Override
    public void clear() {
        fp64Clear(this.deallocator.address);
    }

    @Override
    protected void sycnhronizeTo(Number[] t, int size) {
        double[] f = new double[size];

        for (int i = 0; i < size; i++) {
            f[i] = t[i].doubleValue();
        }

        fp64Sync(this.deallocator.address, f, size);
    }

    @Override
    protected Number[] sycnhronizeFrom(int beginIndex, int endIndex) {
        int size = (1 + endIndex) - beginIndex;

        double[] from = fp64Fetch(this.deallocator.address, beginIndex, endIndex);
        Double[] f    = new Double[size];

        for (int i = 0; i < size; i++) {
            f[i] = from[i];
        }

        return f;
    }

    @Override
    protected Double convert(Number n) {
        return n.doubleValue();
    }

    // -----======= Native Operation =======-----

    private static native long fp64Construct();
    private static native long fp64Delete(long address);

    private static native int fp64Size(long address);
    private static native void fp64Sync(long address, double[] num, int size);
    private static native double[] fp64Fetch(long address, int begin, int to);

    private static native boolean fp64Contains(long address, double num);
    private static native boolean fp64Remove(long address, double num);
    private static native void fp64Clear(long address);
}
