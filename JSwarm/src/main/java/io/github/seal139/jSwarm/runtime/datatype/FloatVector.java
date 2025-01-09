package io.github.seal139.jSwarm.runtime.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.seal139.jSwarm.core.NativeCleaner;
import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;
import io.github.seal139.jSwarm.core.NativeException;

public final class FloatVector extends Vector<Float> implements NativeResources {

    final class DeallocatorImpl implements Deallocator {
        private final ExecutorService synchronizer = Executors.newSingleThreadExecutor();

        private final long       address;
        private final List<Long> nativeStorage = new ArrayList<>(32);

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

    private final long address;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    public FloatVector() throws NativeException {
        this(1, 262136, 32);
    }

    public FloatVector(int initial, int bufferSize, int bucketSize) throws NativeException {
        super(bufferSize, bucketSize);

        this.address = fp32Construct(initial);

        this.deallocator = new DeallocatorImpl(this.address);
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

    private final class Bucket extends AbstractBucket implements Runnable {

        private int               maxIndex;
        private transient float[] storage;

        private transient long rwWrapper;

        private native long hook();

        Bucket(int size) {
            this.maxIndex = size;
            this.storage  = new float[size];

            this.rwWrapper = hook();
            FloatVector.this.bucketAddress.add(this.rwWrapper);
        }

        @Override
        AbstractBucket setNext(AbstractBucket bucket) {
            this.next   = bucket;
            bucket.prev = this;
            return bucket;
        }

        @Override
        protected void waitBucket() {
            while (this.locked) {
            }
        }

        @Override
        Vector<Float>.AbstractBucket addIncr(Float t) {
            waitBucket();
//            if (this.locked) {
//                this.storage[0] = t;
//                return this;
//            }

            this.storage[this.indexPointer++] = t;

            if (this.indexPointer < this.maxIndex) {
                return this;
            }

            flush();

            return this.next;
        }

        @Override
        void fetch(final int from, final int to) {
            this.indexPointer = 0;

            this.size = to - from;
            if (this.size > 0) {
                fp32Fetch(FloatVector.this.address, this.rwWrapper, from, to - 1);

//               this.locked = true;

                // getSynchronizer().submit(() -> {
                fp32Fetch(FloatVector.this.address, this.rwWrapper, from, to - 1);
                // this.locked = false;
                // });
            }
        }

        @Override
        public void run() {
            long          address    = FloatVector.this.address;
            int           bucketSize = FloatVector.this.bucketSize;
            AtomicInteger queue      = FloatVector.this.queue;

            Bucket b = this;
            while (true) {
                boolean isFull = fp32Sync(address, b.rwWrapper, b.indexPointer, b.maxIndex);

                b.indexPointer = 0;
                b.locked       = false;

                if (isFull) {
//                    AbstractBucket bucket = b;
                    b = (Bucket) b.next;
//
//                    for (int i = 0; i < bucketSize; i++) {
//                        bucket = bucket.setNext(newBucket(8192));
//                    }
//
//                    bucket.setNext(b);

                    fp32AllocateMore(address, b.maxIndex, 2f);
                }
                else {
                    b = (Bucket) b.next;
                }

                if (queue.decrementAndGet() == 0) {
                    return;
                }
            }

        }

        @Override
        void flush() {

            this.locked = (this.indexPointer != 0);
            if (!this.locked) {
                return;
            }

            if (FloatVector.this.queue.getAndIncrement() > 0) {
                return;
            }

            getSynchronizer().submit(this);
        }

        @Override
        Float getStorageValue() { return this.storage[this.indexPointer++]; }
    }

    @Override
    protected Vector<Float>.AbstractBucket newBucket(int size) {
        return new Bucket(size);
    }

    @Override
    protected ExecutorService getSynchronizer() { return this.deallocator.synchronizer; }

    @Override
    public int size() {
        return fp32GetSize(this.address);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Float) {
            return fp32Remove(this.address, ((Float) o).floatValue());
        }

        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Float) {
            return fp32Contains(this.address, ((Float) o).floatValue());
        }

        return false;
    }

    @Override
    public void clear() {
        fp32Clear(this.address);
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
