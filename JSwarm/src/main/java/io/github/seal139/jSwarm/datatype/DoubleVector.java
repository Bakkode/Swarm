package io.github.seal139.jSwarm.datatype;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * High performance array of float.
 */
public final class DoubleVector extends Vector<Double> {

    private static final long WORD_SIZE  = 8;
    private static final long SHIFT_SIZE = 3;

    // ============ Allocator - Deallocator ==================

    public DoubleVector() {
        super(16, false, WORD_SIZE, SHIFT_SIZE);
    }

    public DoubleVector(long initial) {
        super(initial, false, WORD_SIZE, SHIFT_SIZE);
    }

    public DoubleVector(long initial, boolean aligned) {
        super(initial, aligned, WORD_SIZE, SHIFT_SIZE);
    }

    public DoubleVector(long address, long size) {
        super(address, address + (size * WORD_SIZE), WORD_SIZE, SHIFT_SIZE);
    }

    // ============ Functionality Operation ==================

    @Override
    public List<Double> subList(long fromIndex, long toIndex) {
        // Safety check
        fromIndex = this.lBound + (fromIndex << this.bit_t);
        if ((fromIndex < this.lBound) || (fromIndex >= this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(fromIndex));
        }

        // Safety check
        toIndex = this.lBound + (toIndex << this.bit_t);
        if ((toIndex < this.lBound) || (toIndex > this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(toIndex));
        }

        // Safety check
        if (toIndex < fromIndex) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(toIndex));
        }

        return new DoubleVector(fromIndex, toIndex - fromIndex);
    }

    @Override
    public boolean addAll(Collection<? extends Double> c) {
        long additional = c.size() - capacity();

        if (additional > 0) {
            reinit(capacity() + additional, this.size_t);
        }

        c.forEach(e -> {
            memAlloc.putDouble(this.indexPtr, e);
            this.indexPtr += this.size_t;
        });

        return true;
    }

    @Override
    public boolean add(Double e) {
        if (this.indexPtr >= this.uBound) {
            reinit(capacity() * 2L, this.size_t);
        }

        memAlloc.putDouble(this.indexPtr, e);
        this.indexPtr += this.size_t;

        return true;
    }

    @Override
    public Double get(long index) {
        // Safety check
        if ((index < 0) || ((this.lBound + (index << this.bit_t)) >= this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(index));
        }

        return directGet(index);
    }

    @Override
    public Double set(long index, Double element) {
        // Safety check
        if ((index < 0) || ((this.lBound + (index << this.bit_t)) >= this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(index));
        }

        return directSet(index, element.doubleValue());
    }

    @Override
    public Double remove(long index) {
        if ((index < 0) || ((this.lBound + (index << this.bit_t)) >= this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(index));
        }

        return directRemove(index);
    }

    @Override
    public void clear() {
        if (this.dynamic) {
            this.indexPtr = this.lBound;
            return;
        }

        for (long i = this.lBound; i < this.indexPtr; i += this.size_t) {
            memAlloc.putDouble(i, 0.0);
        }
    }

    @Override
    public long longIndexOf(Double element) {
        final double ref = element.doubleValue();
        final long   s   = (longSize() << this.bit_t) + this.lBound;

        for (long i = this.lBound; i < s; i += this.size_t) {
            if (memAlloc.getDouble(i) == ref) {
                return (i - this.lBound) >> this.bit_t;
            }
        }

        return -1;
    }

    @Override
    public long longLastIndexOf(Double o) {
        final double ref = o.doubleValue();
        final long   s   = (longSize() << this.bit_t) + (this.lBound - this.size_t);

        for (long i = s; i >= this.lBound; i -= this.size_t) {
            if (memAlloc.getDouble(i) == ref) {
                return (i - this.lBound) >> this.bit_t;
            }
        }

        return -1;
    }

    @Override
    public ListIterator<Double> listIterator(int index) {
        return new ListIterator<>() {
            private final long st = DoubleVector.this.size_t;
            private final long bt = DoubleVector.this.bit_t;
            private final long ub = DoubleVector.this.indexPtr - this.st;

            private long memPtr = DoubleVector.this.lBound + ((index - 1) << this.bt);

            @Override
            public boolean hasNext() {
                return this.memPtr < this.ub;
            }

            @Override
            public int nextIndex() {
                return (int) ((this.memPtr + this.st) >> this.bt);
            }

            @Override
            public Double next() {
                this.memPtr += this.st;
                return memAlloc.getDouble(this.memPtr);
            }

            @Override
            public boolean hasPrevious() {
                return this.memPtr >= DoubleVector.this.lBound;
            }

            @Override
            public int previousIndex() {
                return (int) ((this.memPtr - this.st) >> this.bt);
            }

            @Override
            public Double previous() {
                this.memPtr -= this.st;
                return memAlloc.getDouble(this.memPtr);
            }

            @Override
            public void set(Double e) {
                memAlloc.putDouble(this.memPtr, e);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported due to performance issue");
            }

            @Override
            public void add(Double e) {
                throw new UnsupportedOperationException("Not supported due to performance issue");
            }
        };
    }

    // -----=======~~ Direct Functionality ~~~=======-----
    public double directGet(long index) {
        return memAlloc.getDouble(this.lBound + (index << this.bit_t));
    }

    public double directSet(long index, Double element) {
        final long   memPtr = this.lBound + (index << this.bit_t);
        final double value  = memAlloc.getDouble(memPtr);

        memAlloc.putDouble(memPtr, element);
        return value;
    }

    public double directRemove(long index) {
        final long idx = this.lBound + (index << this.bit_t);

        final double prevValue = memAlloc.getDouble(idx);

        if (this.dynamic) {
            // Shift left
            for (long i = idx; i < this.indexPtr; i += this.size_t) {
                memAlloc.putDouble(i, memAlloc.getDouble(i + this.size_t));
            }

            this.indexPtr -= this.size_t;
            return prevValue;
        }

        memAlloc.putDouble(idx, 0.0);
        return prevValue;
    }
}
