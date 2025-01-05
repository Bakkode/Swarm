package io.github.seal139.jSwarm.runtime.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;
import io.github.seal139.jSwarm.core.NativeException;

/**
 * Provide as huge data chunk for parallel process. The purpose of this class it
 * to provides a way to synchronize data from / to device memory for further
 * processing. Data stored here is stored in native side and unmanaged. However,
 * automatic garbage collection should be work as usual by performing memory
 * deletion upon GC call.
 *
 * <br/>
 *
 * As information, Java memory and native memory are two separated things. this
 * will be a problem because the device memory can only work with native memory,
 * not Java memory and can cause data duplication that can easily exhaust
 * memory. As workaround, right after status is {@link CollectionStatus
 * SYNCHRONIZED}, we will move data from native into Java. Rendering this vector
 * is in read only state
 *
 *
 * @param <T>
 */
public abstract class Vector<T extends Number> implements NativeResources, List<T> {
    // Implemented using ring buffer
    // Ring buffer is simply linked list where the last node connected to first node
    // Making it no head and no tail

    // The target performance of this implementation is to make sure synchronization
    // if faster than operation. This is to make sure no bottleneck
    // We should experiment with the best bucket size and buffer size to handle this
    // problem

//     We need at least 4 bucket to handle this situation
//    ------------v---------------v----
//    |   0   |   1   |   2   |   3   |
//    ----^---------------^------------
//

    // Logic:
    // For write mode:
    // - as long as bucket is full, lock it and commit in separate thread whilst
    // operation perform in the next bucket

    // For read mode
    // - Load for n-1 bucket
    // - if operation on bucket is at index 0, load the previous bucket with the
    // next value in separate thread

    // - In the worst case, when operation outrun synchronization process, wait for
    // that bucket become available

    // To prevent conflict and race condition, MAIN thread is responsible to LOCK
    // the bucket and SYNCHRONIZER is responsible to UNLOCK the bucket. If bucket is
    // locked, use busy-waiting on that. Since operation and synchronization did not
    // overlap, no race condition is guaranteed

    private static final NativeException e;
    static {
        Throwable le;
        try {
            System.load("C:\\wsl\\Programming\\Swarm\\C\\vector\\out\\build\\x64-release\\Vector.dll");
            le = null;
        }
        catch (Throwable e) {
            le = e;
        }

        if (le != null) {
            e = new NativeException(le);
        }
        else {
            e = null;
        }
    }

    protected abstract class AbstractBucket {
        protected int indexPointer = 0;
        protected int size;

        volatile boolean locked = false;

        protected AbstractBucket next;

        protected AbstractBucket prev;

        abstract AbstractBucket setNext(AbstractBucket bucket);

        abstract AbstractBucket addIncr(T t);

        abstract T getStorageValue(int index);

        abstract void fetch(final int from, final int to);

        abstract void flush();

        abstract void waitBucket();
    }

    protected volatile AtomicInteger queue = new AtomicInteger(0);

    private AbstractBucket bufferBucket;

    protected final int        bufferSize;
    protected int              bucketSize;
    protected final List<Long> bucketAddress = new ArrayList<>();

    protected Vector(int bufferSize, int bucketSize) throws NativeException {
        if (e != null) {
            throw e;
        }

        this.bufferSize = bufferSize;
        this.bucketSize = bucketSize;

        AbstractBucket first = newBucket(bufferSize);

        AbstractBucket bucket = first;
        for (int i = 1; i < bucketSize; i++) {
            bucket = bucket.setNext(newBucket(bufferSize));
        }

        this.bufferBucket = bucket.setNext(first);
    }

    public void flush() {
        this.bufferBucket.flush();
        waitAll();
    }

    public void waitAll() {
        this.bufferBucket.waitBucket();

        AbstractBucket bucket = this.bufferBucket.next;
        while (!this.bufferBucket.equals(bucket)) {
            bucket.waitBucket();
            bucket = bucket.next;
        }
    }

    @Override
    public Iterator<T> iterator() {
        waitAll();

        // Fetch initial data first
        int size     = size();
        int offset[] = {
                0 };

//        int iterSize;
//
//        if (size <= 1_000_000) {
//            iterSize = 4;
//        }
//        else if (size <= 2_000_000) {
//            iterSize = 8;
//        }
//        else if (size <= 32_000_000) {
//            iterSize = 16;
//        }
//        else if (size <= 128_000_000) {
//            iterSize = 32;
//        }
//        else if (size <= 512_000_000) {
//            iterSize = 64;
//        }
//        else {
//            iterSize = 128;
//        }

//        AbstractBucket first = newBucket(this.bufferSize);
//
//        offset[0] = size > this.bufferSize ? this.bufferSize : size;
//        first.fetch(0, offset[0]);
//
//        AbstractBucket bucket = first;
//        for (int i = 1; i < this.bucketSize; i++) {
//            bucket = bucket.setNext(newBucket(this.bufferSize));
//        }
//        bucket.setNext(first);
//
//        first.waitBucket();

        offset[0] = size > this.bufferSize ? this.bufferSize : size;
        this.bufferBucket.fetch(0, offset[0]);
        this.bufferBucket.waitBucket();

        return new Iterator<>() {
            AbstractBucket buck = Vector.this.bufferBucket;

            AbstractBucket syncBucket = Vector.this.bufferBucket;

            private void fetchNext() {
                AbstractBucket nBucket = this.syncBucket.next;

                while (true) {
                    if (offset[0] >= size) {
                        nBucket.size = 0;
                        break;
                    }

                    int begin = offset[0];
                    offset[0] += Vector.this.bufferSize;

                    if (offset[0] >= size) {
                        nBucket.fetch(begin, size);
                        break;
                    }

                    nBucket.fetch(begin, offset[0]);

                    if (nBucket.next.equals(this.syncBucket)) {
                        this.syncBucket = nBucket;
                        break;
                    }

                    nBucket = nBucket.next;
                }

            }

            @Override
            public T next() {
                try {
                    this.buck.waitBucket();

                    Number value = this.buck.getStorageValue(this.buck.indexPointer);

                    if (++this.buck.indexPointer >= this.buck.size) {
                        this.buck = this.buck.next;
                        fetchNext();
                    }

                    return convert(value);
                }
                catch (Exception e) {
                    return null;
                }
            }

            @Override
            public boolean hasNext() {
                this.buck.waitBucket();
                return this.buck.indexPointer < this.buck.size;
            }
        };
    }

    @Override
    public boolean add(T e) {
        this.bufferBucket = this.bufferBucket.addIncr(e);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(e -> add(e));
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty() { return size() == 0; }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            if (!remove(o)) {
                return false;
            }
        }

        return true;
    }

    // -----=======~~ Abstract ~~~=======-----
    protected abstract T convert(Number n);
    protected abstract ExecutorService getSynchronizer();
    protected abstract AbstractBucket newBucket(int size);

    // -----======= Not Supported =======-----

    /**
     * Not supported due to performance issue
     */
    @Override
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Not supported due to performance issue
     */
    @Override
    @Deprecated
    public final Object[] toArray() {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Not supported due to performance issue
     */
    @Override
    @Deprecated
    @SuppressWarnings("hiding")
    public final <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported");
    }
}
