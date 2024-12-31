package io.github.seal139.jSwarm.runtime.datatype;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;

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

    protected final class Bucket {
        volatile int lockCount = 0;

        private Bucket next;

        private final int      maxIndex;
        private final Number[] storage;

        Bucket(int size) {
            this.maxIndex = size;
            this.storage  = new Number[size];
        }

        Bucket setNext(Bucket bucket) {
            this.next = bucket;

            return bucket;
        }

        private int indexPointer = 0;

        Bucket addIncr(T t) {
            waitBucket(this);

            this.storage[this.indexPointer++] = t;

            if (this.indexPointer < this.maxIndex) {
                return this;
            }

            flush();

            return this.next;
        }

        private int size;

        Future<?> fetch(int from, int to) {
            waitBucket(this);

            this.lockCount += 1;
            return Vector.this.getSynchronizer().submit(() -> {
                this.indexPointer = 0;

                this.size = to - from;
                Number[] t = sycnhronizeFrom(from, to - 1);
                for (int i = 0; i < this.size; i++) {
                    this.storage[i] = t[i];
                }

                this.lockCount -= 1;
            });
        }

        void flush() {
            if (this.indexPointer == 0) {
                return;
            }

            waitBucket(this);

            this.lockCount += 1;
            Vector.this.getSynchronizer().submit(() -> {
                sycnhronizeTo(this.storage, this.indexPointer);
                this.indexPointer = 0;

                this.lockCount -= 1;
            });
        }
    }

    private Bucket bufferBucket;

    private final int bufferSize;
    private final int bucketSize;

    protected Vector() {
        this(512, 3);
    }

    protected Vector(int bufferSize, int bucketSize) {
        this.bufferSize = bufferSize;
        this.bucketSize = bucketSize;

        Bucket first = new Bucket(bufferSize);

        Bucket bucket = first;
        for (int i = 1; i < bucketSize; i++) {
            bucket = bucket.setNext(new Bucket(bufferSize));
        }

        this.bufferBucket = bucket.setNext(first);
    }

    protected void waitBucket(Bucket bucket) {
        int i = 0;
        while (bucket.lockCount > 0) {
            i += 1;
            // NoOp. but can be used to detect wait cycle
        }

        if (i != 0) {
            try {
                throw new Exception("hit : i");
            }
            catch (Exception e) {
                System.out.println(">>" + bucket.hashCode() + ":" + i);
                e.printStackTrace();
            }
        }
    }

    protected void waitAll() {
        waitBucket(this.bufferBucket);

        Bucket bucket = this.bufferBucket.next;
        while (!this.bufferBucket.equals(bucket)) {
            waitBucket(bucket);
            bucket = bucket.next;
        }
    }

    @Override
    public Iterator<T> iterator() {
        waitAll();

        this.bufferBucket.flush();

        waitBucket(this.bufferBucket);

        // Fetch initial data first
        int size     = size();
        int offset[] = {
                0 };

        Bucket first  = new Bucket(this.bufferSize);
        Bucket bucket = first;
        for (int i = 1; i < this.bucketSize; i++) {
            bucket = bucket.setNext(new Bucket(this.bufferSize));
        }
        bucket.setNext(first);

        try {
            if (size > this.bufferSize) {
                offset[0] = this.bufferSize;
                first.fetch(0, offset[0]).get();
            }
            else {
                offset[0] = size;
                first.fetch(0, offset[0]).get();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new Iterator<>() {
            Bucket bucket = first;

            Future<?> f;

            private void fetchNext() {
                Bucket nBucket = this.bucket.next;

                if (offset[0] < size) {

                    int begin = offset[0];
                    offset[0] += Vector.this.bufferSize;

                    if (offset[0] < size) {
                        this.f = nBucket.fetch(begin, offset[0]);
                    }
                    else {
                        this.f = nBucket.fetch(begin, size);
                    }

                }
                else {
                    nBucket.size = 0;
                }
            }

            @Override
            public T next() {
                try {
                    Number value = this.bucket.storage[this.bucket.indexPointer];
                    if (this.bucket.indexPointer == 0) {
                        fetchNext();
                    }

                    if (++this.bucket.indexPointer >= this.bucket.size) {
                        try {
                            if (this.f != null) {
                                this.f.get();
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        this.bucket = this.bucket.next;
                    }

                    return convert(value);
                }
                catch (Exception e) {
                    return null;
                }
            }

            @Override
            public boolean hasNext() {
                waitBucket(this.bucket);

                if (this.bucket.indexPointer < this.bucket.size) {
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
    protected abstract void sycnhronizeTo(Number[] t, int size);
    protected abstract Number[] sycnhronizeFrom(int beginIndex, int endIndex);
    protected abstract ExecutorService getSynchronizer();

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
