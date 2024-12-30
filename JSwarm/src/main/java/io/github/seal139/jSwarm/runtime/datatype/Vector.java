package io.github.seal139.jSwarm.runtime.datatype;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
public abstract class Vector<T extends Number> implements List<T> {
    // Implemented using ring buffer

    protected final ExecutorService synchronizer = Executors.newSingleThreadExecutor();

    protected final class Bucket {
        private Bucket         next;
        private final int      maxIndex;
        private final Number[] storage;

        Bucket(int size) {
            this.maxIndex = size;
            this.storage  = new Number[size];
        }

        Bucket setNext(Bucket next) {
            this.next = next;
            return next;
        }

        private int indexPointer = 0;

        Bucket addIncr(T t) {
            // Prevent race condition when insertion is extremely fast that outrun
            // synchronization process
            synchronized (this.storage) {
                this.storage[this.indexPointer++] = t;
            }

            if (this.indexPointer < this.maxIndex) {
                return this;
            }

            Vector.this.synchronizer.submit(() -> flush());

            return this.next;
        }

        private int size;

        void fetch(int from, int to) {
            synchronized (this.storage) {
                // System.out.println("fetching " + this.storage.hashCode());
                this.size = to-- - from;
                Number[] t = sycnhronizeFrom(from, to);
                for (int i = 0; i < this.size; i++) {
                    this.storage[i] = t[i];
                }
            }

        }

        void flush() {
            // Prevent race condition when insertion is extremely fast that outrun
            // synchronization process
            synchronized (this.storage) {
                // System.out.println("flushing " + this.storage.hashCode());
                sycnhronizeTo(this.storage, this.indexPointer);
                this.indexPointer = 0;
            }
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

    @Override
    public Iterator<T> iterator() {
        Future<?> ret = Vector.this.synchronizer.submit(() -> {
            // Submit uncommited data first before iterating
            this.bufferBucket.flush();

            // Fetch initial data first
            int size     = size();
            int offset[] = {
                    0 };

            Bucket first = new Bucket(this.bufferSize);

            if (size > this.bufferSize) {
                offset[0] = this.bufferSize;
                first.fetch(0, offset[0]);

                // For the next bucket, do it in separate thread for performance
                Vector.this.synchronizer.submit(() -> {
                    Bucket bucket = first;
                    for (int i = 1; i < this.bucketSize; i++) {
                        bucket = bucket.setNext(new Bucket(this.bufferSize));

                        // Default
                        bucket.setNext(first);

                        int begin = offset[0];
                        offset[0] += this.bufferSize;

                        if (offset[0] < size) {
                            bucket.fetch(begin, offset[0]);
                            continue;
                        }

                        bucket.fetch(begin, size);
                        offset[0] = size;

                        break;
                    }
                });
            }
            else {
                offset[0] = size;
                first.fetch(0, offset[0]);
            }

            return new Iterator<T>() {
                Bucket bucket = first;

                @Override
                public T next() {
                    Number value = this.bucket.storage[this.bucket.indexPointer++];

                    if ((this.bucket.indexPointer >= this.bucket.size) && (this.bucket.size >= Vector.this.bufferSize)) {
                        Bucket prevBucket = this.bucket;
                        this.bucket = this.bucket.next;

                        Vector.this.synchronizer.submit(() -> {
                            int begin = offset[0];
                            offset[0] += Vector.this.bufferSize;

                            if (offset[0] < size) {
                                prevBucket.fetch(begin, offset[0]);
                            }
                            else {
                                prevBucket.fetch(begin, size);
                            }
                        });
                    }

                    return convert(value);
                }

                @Override
                public boolean hasNext() {
                    return (this.bucket.indexPointer < this.bucket.size) || //
                           ((this.bucket.size >= Vector.this.bufferSize) && (this.bucket.next.size > 0));
                }
            };
        });

        try {
            return (Iterator<T>) ret.get();
        }
        catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    abstract T convert(Number n);

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

    // -----=======~~ Abstract ~~~=======-----

    protected abstract void sycnhronizeTo(Number[] t, int size);
    protected abstract Number[] sycnhronizeFrom(int beginIndex, int endIndex);

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
    public void add(int index, T element) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Not supported due to performance issue
     */
    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Not supported due to performance issue
     */
    @Override
    @Deprecated
    public final ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Not supported due to performance issue
     */
    @Override
    @Deprecated
    public final ListIterator<T> listIterator() {
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

    /**
     * Not supported due to performance issue
     */
    @Override
    @Deprecated
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }
}
