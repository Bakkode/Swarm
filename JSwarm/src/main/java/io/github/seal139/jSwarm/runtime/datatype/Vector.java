package io.github.seal139.jSwarm.runtime.datatype;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    protected abstract class CacheDeallocator implements Deallocator {
        protected final long address;
        protected boolean    isClosed = false;

        protected CacheDeallocator(long address) {
            this.address = address;
        }

        @Override
        public void clean() {
            if (this.isClosed) {
                return;
            }

            this.isClosed = true;
        }
    }

    protected class VectorDeallocator implements Deallocator {
        protected final ExecutorService synchronizer = Executors.newSingleThreadExecutor();

        protected final long vectorAddress;
        protected final long cacheAddress;

        protected boolean isClosed = false;

        protected VectorDeallocator(long vAddress, long cAddress) {
            this.vectorAddress = vAddress;
            this.cacheAddress  = cAddress;
        }

        @Override
        public void clean() {
            if (this.isClosed) {
                return;
            }

            this.isClosed = true;
            this.synchronizer.shutdown();

        }
    }

    protected int indexPointer = 0;

    protected final int cacheSize;

    protected Vector(int cacheSize) throws NativeException {
        if (e != null) {
            throw e;
        }

        this.cacheSize = cacheSize;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean isEmpty() { return size() == 0; }

    public void flush() {
        // Only perform if data is dirty (modified, not synchronized)
        if (this.indexPointer > 0) {
            sync();
        }
    }

    // -----=======~~ Abstract ~~~=======-----
    protected abstract void fetch(long address, int from, int to);
    protected abstract void sync();

    // -----======= Not Supported =======-----

    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported due to performance issue");
    }

    @Override
    @Deprecated
    public void add(int index, T element) {
        throw new UnsupportedOperationException("Not supported due to performance issue");
    }

    @Override
    @Deprecated
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not supported due to memory efficiency and performance issue");
    }

    @Override
    @Deprecated
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported due to memory efficiency and performance issue");
    }

    @Override
    @Deprecated
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported due to memory efficiency");
    }

    @Override
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported due to performance issue");
    }

    @Override
    @Deprecated
    public final Object[] toArray() {
        throw new UnsupportedOperationException("Not supported due to memory efficiency");
    }

    @Override
    @Deprecated
    @SuppressWarnings("hiding")
    public final <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported due to memory efficiency");
    }
}
