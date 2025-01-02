package io.github.seal139.jSwarm.runtime.datatype;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;
import io.github.seal139.jSwarm.misc.Common;

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
public abstract class Vector<T extends Number> implements NativeResources, Collection<T> {
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

    static {
        try {
            System.load(Common.getDriverByOs("Vector", "Vector"));
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected abstract class AbstractBucket {
        protected int indexPointer = 0;
        protected int size;

        volatile int lockCount = 0;

        protected AbstractBucket next;

        abstract AbstractBucket setNext(AbstractBucket bucket);

        abstract AbstractBucket addIncr(T t);

        abstract T getStorageValue(int index);

        abstract void fetch(final int from, final int to);

        abstract void flush();

        abstract boolean waitBucket();

        protected AtomicBoolean writting = new AtomicBoolean(false);
    }

    protected AtomicBoolean synchronizing = new AtomicBoolean(false);

    private AbstractBucket bufferBucket;

    protected final int bufferSize;
    protected final int bucketSize;

    protected Vector() {
        this(512, 3);
    }

    protected Vector(int bufferSize, int bucketSize) {
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
        this.bufferBucket.flush();

        waitAll();

        // Fetch initial data first
        int size     = size();
        int offset[] = {
                0 };

        AbstractBucket first = newBucket(this.bufferSize);

        offset[0] = size > this.bufferSize ? this.bufferSize : size;
        first.fetch(0, offset[0]);

        AbstractBucket bucket = first;
        for (int i = 1; i < this.bucketSize; i++) {
            bucket = bucket.setNext(newBucket(this.bufferSize));
        }
        bucket.setNext(first);

        first.waitBucket();

        return new Iterator<>() {
            AbstractBucket buck = first;

            AbstractBucket syncBucket = first;

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

                if (this.buck.indexPointer < this.buck.size) {
                    return true;
                }

                return false;
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
