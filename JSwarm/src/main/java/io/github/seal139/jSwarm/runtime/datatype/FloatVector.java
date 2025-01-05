package io.github.seal139.jSwarm.runtime.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seal139.jSwarm.core.NativeCleaner;
import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;
import io.github.seal139.jSwarm.core.NativeException;

public final class FloatVector extends Vector<Float> implements NativeResources {

    final class DeallocatorImpl implements Deallocator {
        private final ExecutorService synchronizer = Executors.newSingleThreadExecutor();

        private final long       address;
        private final List<Long> nativeStorage = new ArrayList<>();

        private boolean isClosed = false;

        private DeallocatorImpl(long address) {
            this.address = address;
        }

        void add(long nativeStorage) {
            this.nativeStorage.add(nativeStorage);
        }

        @Override
        public void clean() {
            waitAll();

            long[] caches = new long[this.nativeStorage.size()];
            for (int i = 0; i < this.nativeStorage.size(); i++) {
                caches[i] = this.nativeStorage.get(i);
            }

            fp32Delete(this.address, caches);

            this.synchronizer.shutdown();
            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public FloatVector() throws NativeException {
        this(1, 262136, 32);
    }

    public FloatVector(int initial, int bufferSize, int bucketSize) throws NativeException {
        super(bufferSize, bucketSize);

        long ctr = System.nanoTime();
        fp32Construct(initial);
        ctr = System.nanoTime() - ctr;
        System.out.println("Allocation: " + (((double) ctr) / 1_000_000));

        this.deallocator = new DeallocatorImpl(fp32Construct(initial));
        NativeCleaner.register(this);

        this.bucketAddress.forEach(v -> this.deallocator.add(v));
    }

    @Override
    public void close() throws Exception {
        this.deallocator.clean();
    }

    @Override
    public boolean isClosed() { return this.deallocator.isClosed; }

    // ============ Functionality Operation ==================

    private final class Bucket extends AbstractBucket {

        private int               maxIndex;
        private transient float[] storage;

        private transient long rwWrapper;

        private native long hook();

        Bucket(int size) {
            this.maxIndex = size;
            this.storage  = new float[size];

            try {
                this.rwWrapper = hook();
                FloatVector.this.bucketAddress.add(this.rwWrapper);
            }
            catch (Throwable e) {
                e.printStackTrace();
            }

        }

        @Override
        AbstractBucket setNext(AbstractBucket bucket) {
            this.next = bucket;

            return bucket;
        }

        @Override
        protected boolean waitBucket() {
            boolean b = false;

            while (this.locked) {
                b = true;
            }

            return b;
        }

        Long ctr = null;

        @Override
        Vector<Float>.AbstractBucket addIncr(Float t) {
            if (waitBucket()) {

            }

            if (this.ctr == null) {
                this.ctr = System.nanoTime();
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

            this.locked = true;

            FloatVector.this.getSynchronizer().submit(() -> {
                this.indexPointer = 0;

                this.size = to - from;
                // float[] t = sycnhronizeFrom(from, to - 1);
                fp32Fetch(FloatVector.this.deallocator.address, this.rwWrapper, from, to - 1);

                this.locked = false;
            });
        }

        @Override
        void flush() {

            if (this.indexPointer == 0) {
                return;
            }

            this.locked = true;

            if (FloatVector.this.queue.getAndIncrement() > 0) {
                return;
            }

            getSynchronizer().submit(() -> {
                // Long ctr = System.nanoTime();
                Bucket b = this;
                Bucket c;

                while (true) {
                    b.locked = true;

                    int size = FloatVector.this.bucketSize * FloatVector.this.bufferSize;

                    boolean isFull = false;
                    try {
                        isFull = fp32Sync(FloatVector.this.deallocator.address, b.rwWrapper, b.indexPointer, size);

                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                    }

                    b.indexPointer = 0;

                    c = b;
                    b = (Bucket) b.next;

                    c.locked = false;

                    if (isFull) {
                        fp32AllocateMore(FloatVector.this.deallocator.address, FloatVector.this.bucketSize * FloatVector.this.bufferSize, 2f);
                    }

                    if (FloatVector.this.queue.decrementAndGet() == 0) {
                        return;
                    }

                }

                // System.out.println("sync: " + ((System.nanoTime() - ctr) / 1000000.0));
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
        return fp32GetSize(this.deallocator.address);
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
    protected Float convert(Number n) {
        return n.floatValue();
    }

    // -----======= Native Operation =======-----

    private static native long fp32Construct(int cacheSize);
    private static native void fp32Delete(long address, long[] caches);

    private static native int fp32GetSize(long address);

    private static native void fp32AllocateMore(long address, int cacheSize, float resizePolicy);
    private static native boolean fp32Sync(long address, long bufferAddress, int size, int cacheSize);
    private static native void fp32Fetch(long address, long bufferAddress, int begin, int to);

    private static native float fp32GetByIndex(long address, int index);
    private static native boolean fp32Contains(long address, float num);
    private static native boolean fp32Remove(long address, float num);
    private static native void fp32RemoveByIndex(long address, int index);

    private static native void fp32Clear(long address);

    @Override
    public boolean addAll(int index, Collection<? extends Float> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Float get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Float set(int index, Float element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void add(int index, Float element) {
        // TODO Auto-generated method stub

    }

    @Override
    public Float remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int indexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ListIterator<Float> listIterator() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ListIterator<Float> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public List<Float> subList(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }
}
