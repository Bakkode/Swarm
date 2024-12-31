package io.github.seal139.jSwarm.runtime.datatype;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seal139.jSwarm.core.NativeCleaner;

public final class FloatVector extends Vector<Float> {

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

            fp32Clear(this.address);
            fp32Delete(this.address);

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public FloatVector(int bufferSize, int bucketSize) {
        super(bufferSize, bucketSize);

        this.deallocator = new DeallocatorImpl(fp32Construct());
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
        return fp32Size(this.deallocator.address);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Float) {
            return fp32Remove(this.deallocator.address, ((Float) o).floatValue());
        }

        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Float) {
            return fp32Contains(this.deallocator.address, ((Float) o).floatValue());
        }

        return false;
    }

    @Override
    public void clear() {
        fp32Clear(this.deallocator.address);
    }

    @Override
    protected void sycnhronizeTo(Number[] t, int size) {
        float[] f = new float[size];

        for (int i = 0; i < size; i++) {
            f[i] = t[i].floatValue();
        }

        fp32Sync(this.deallocator.address, f, size);
    }

    @Override
    protected Number[] sycnhronizeFrom(int beginIndex, int endIndex) {
        int size = (1 + endIndex) - beginIndex;

        float[] from = fp32Fetch(this.deallocator.address, beginIndex, endIndex);
        Float[] f    = new Float[size];

        for (int i = 0; i < size; i++) {
            f[i] = from[i];
        }

        return f;
    }

    @Override
    protected Float convert(Number n) {
        return n.floatValue();
    }

    // -----======= Native Operation =======-----

    private static native long fp32Construct();
    private static native long fp32Delete(long address);

    private static native int fp32Size(long address);
    private static native void fp32Sync(long address, float[] num, int size);
    private static native float[] fp32Fetch(long address, int begin, int to);

    private static native boolean fp32Contains(long address, float num);
    private static native boolean fp32Remove(long address, float num);
    private static native void fp32Clear(long address);
}
