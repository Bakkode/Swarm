package io.github.seal139.jSwarm.runtime.datatype;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seal139.jSwarm.core.NativeCleaner;
import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;

public final class FloatVector extends Vector<Float> implements NativeResources {

    final class DeallocatorImpl implements Deallocator {
        private final ExecutorService synchronizer = Executors.newSingleThreadExecutor();

        private final long address;
        private boolean    isClosed = false;

        private DeallocatorImpl(long address) {
            this.address = address;
        }

        @Override
        public void clean() {
            waitAll();

            // fp32Clear(this.address);
            fp32Delete(this.address);

            this.synchronizer.shutdown();
            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public FloatVector() {
        // -reach 15999600 ns (15.9ms) for write
        // -reach 3685900 ns (3.2ms) for read
        this(262136, 32);
    }

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

    private final class Bucket extends AbstractBucket {

        volatile int lockCount = 0;

        private int     maxIndex;
        private float[] storage;

        Bucket(int size) {
            this.maxIndex = size;
            this.storage  = new float[size];
        }

        @Override
        AbstractBucket setNext(AbstractBucket bucket) {
            this.next = bucket;

            return bucket;
        }

        @Override
        protected boolean waitBucket() {
            boolean b = false;
            while (this.lockCount > 0) {
                b = true;
            }

            return b;
        }

        @Override
        Vector<Float>.AbstractBucket addIncr(Float t) {
            if (waitBucket()) {
                // this.maxIndex *= 1.5;
                // this.storage = new float[this.maxIndex];
            }

            this.storage[this.indexPointer++] = t;

            if (this.indexPointer < this.maxIndex) {
                return this;
            }

            flush();

            return this.next;
        }

        @Override
        void fetch(final int from, final int to) {
            waitBucket();

            this.lockCount += 1;
            FloatVector.this.getSynchronizer().submit(() -> {
                this.indexPointer = 0;

                this.size = to - from;
                float[] t = sycnhronizeFrom(from, to - 1);
                for (int i = 0; i < this.size; i++) {
                    this.storage[i] = t[i];
                }

                this.lockCount -= 1;
            });
        }

        @Override
        void flush() {
            if (this.indexPointer == 0) {
                return;
            }

            this.waitBucket();

            this.lockCount += 1;
            getSynchronizer().submit(() -> {
                sycnhronizeTo(this.storage, this.indexPointer);
                this.indexPointer = 0;

                this.lockCount -= 1;

//
//                if (++this.flushCounter == 5) {
//                    this.flushCounter = 0;
//
//                }
            });
        }

        @Override
        Float getStorageValue(int index) {
            return this.storage[index];
        }
    }

    @Override
    protected Vector<Float>.AbstractBucket newBucket(int size) {
        return new Bucket(size);
    }

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

    protected void sycnhronizeTo(float[] t, int size) {
        // long ctr = System.nanoTime();
        fp32Sync(this.deallocator.address, t, size);
        // System.out.println("Sync time: " + ((System.nanoTime() - ctr) / 1000000.0));
    }

    protected float[] sycnhronizeFrom(int beginIndex, int endIndex) {
        return fp32Fetch(this.deallocator.address, beginIndex, endIndex);
    }

    @Override
    protected Float convert(Number n) {
        return n.floatValue();
    }

    // -----======= Native Operation =======-----

    private static native long fp32Construct();
    private static native void fp32Delete(long address);

    private static native int fp32Size(long address);
    private static native void fp32Sync(long address, float[] num, int size);
    private static native float[] fp32Fetch(long address, int begin, int to);

    private static native boolean fp32Contains(long address, float num);
    private static native boolean fp32Remove(long address, float num);
    private static native void fp32Clear(long address);
}
