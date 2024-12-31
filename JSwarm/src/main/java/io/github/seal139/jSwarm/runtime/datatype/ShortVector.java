package io.github.seal139.jSwarm.runtime.datatype;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seal139.jSwarm.core.NativeCleaner;

public final class ShortVector extends Vector<Short> {

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

            int16Clear(this.address);
            int16Delete(this.address);

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public ShortVector(int bufferSize, int bucketSize) {
        super(bufferSize, bucketSize);

        this.deallocator = new DeallocatorImpl(int16Construct());
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
        return int16Size(this.deallocator.address);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Short) {
            return int16Remove(this.deallocator.address, ((Short) o).shortValue());
        }

        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Short) {
            return int16Contains(this.deallocator.address, ((Short) o).shortValue());
        }

        return false;
    }

    @Override
    public void clear() {
        int16Clear(this.deallocator.address);
    }

    @Override
    protected void sycnhronizeTo(Number[] t, int size) {
        short[] f = new short[size];

        for (int i = 0; i < size; i++) {
            f[i] = t[i].shortValue();
        }

        int16Sync(this.deallocator.address, f, size);
    }

    @Override
    protected Number[] sycnhronizeFrom(int beginIndex, int endIndex) {
        int size = (1 + endIndex) - beginIndex;

        short[] from = int16Fetch(this.deallocator.address, beginIndex, endIndex);
        Short[] f    = new Short[size];

        for (int i = 0; i < size; i++) {
            f[i] = from[i];
        }

        return f;
    }

    @Override
    protected Short convert(Number n) {
        return n.shortValue();
    }

    // -----======= Native Operation =======-----

    private static native long int16Construct();
    private static native void int16Delete(long address);

    private static native int int16Size(long address);
    private static native void int16Sync(long address, short[] num, int size);
    private static native short[] int16Fetch(long address, int begin, int to);

    private static native boolean int16Contains(long address, short num);
    private static native boolean int16Remove(long address, short num);
    private static native void int16Clear(long address);
}
