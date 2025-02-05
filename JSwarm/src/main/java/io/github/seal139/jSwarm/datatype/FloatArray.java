package io.github.seal139.jSwarm.datatype;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * High performance array of float.
 */
public class FloatArray extends Vector<Float> {

    protected static final long WORD_SIZE  = 4;
    protected static final long SHIFT_SIZE = 2;

    // ============ Allocator - Deallocator ==================

    public FloatArray() {
        super(16, WORD_SIZE, true);
    }

    public FloatArray(long initial) {
        super(initial, WORD_SIZE, true);
    }

    public FloatArray(long initial, boolean aligned) {
        super(initial, WORD_SIZE, aligned);
    }

    public FloatArray(long address, long size) {
        super(address, address + (size * WORD_SIZE), WORD_SIZE);
    }

    // ============ Functionality Operation ==================

    @Override
    public List<Float> subList(long fromIndex, long toIndex) {
        // Safety check
        fromIndex = this.lBound + (fromIndex << SHIFT_SIZE);
        if ((fromIndex < this.lBound) || (fromIndex >= this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(fromIndex));
        }

        // Safety check
        toIndex = this.lBound + (toIndex << SHIFT_SIZE);
        if ((toIndex < this.lBound) || (toIndex > this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(toIndex));
        }

        // Safety check
        if (toIndex < fromIndex) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(toIndex));
        }

        return new FloatArray(fromIndex, toIndex - fromIndex);
    }

    @Override
    public boolean addAll(Collection<? extends Float> c) {
        long additional = c.size() - capacity();

        if (additional > 0) {
            reinit(capacity() + additional, WORD_SIZE);
        }

        c.forEach(e -> {
            memAlloc.putFloat(this.indexPtr, e);
            this.indexPtr += WORD_SIZE;
        });

        return true;
    }

    @Override
    public boolean add(Float e) {
        if (this.indexPtr >= this.uBound) {
            reinit(capacity() * 2L, WORD_SIZE);
        }

        memAlloc.putFloat(this.indexPtr, e);
        this.indexPtr += WORD_SIZE;

        return true;
    }

    @Override
    public Float get(long index) {
        // Safety check
        if ((index < 0) || ((this.lBound + (index << SHIFT_SIZE)) >= this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(index));
        }

        return directGet(index);
    }

    @Override
    public Float set(long index, Float element) {
        // Safety check
        if ((index < 0) || ((this.lBound + (index << SHIFT_SIZE)) >= this.indexPtr)) {
            throw new ArrayIndexOutOfBoundsException("Out of bound: " + String.valueOf(index));
        }

        return directSet(index, element.floatValue());
    }

    @Override
    public Float remove(long index) {
        if ((index < 0) || ((this.lBound + (index << SHIFT_SIZE)) >= this.indexPtr)) {
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

        for (long i = this.lBound; i < this.indexPtr; i += WORD_SIZE) {
            memAlloc.putFloat(i, 0.0f);
        }
    }

    @Override
    public long longIndexOf(Float element) {
        final float ref = element.floatValue();
        final long  s   = (longSize() << SHIFT_SIZE) + this.lBound;

        for (long i = this.lBound; i < s; i += WORD_SIZE) {
            if (memAlloc.getFloat(i) == ref) {
                return (i - this.lBound) >> SHIFT_SIZE;
            }
        }

        return -1;
    }

    @Override
    public long longLastIndexOf(Float o) {
        final float ref = o.floatValue();
        final long  s   = (longSize() << SHIFT_SIZE) + (this.lBound - WORD_SIZE);

        for (long i = s; i >= this.lBound; i -= WORD_SIZE) {
            if (memAlloc.getFloat(i) == ref) {
                return (i - this.lBound) >> SHIFT_SIZE;
            }
        }

        return -1;
    }

    @Override
    public Iterator<Float> iterator() {
        return new Iterator<>() {
            private long memPtr = FloatArray.this.lBound;

            @Override
            public Float next() {
                float f = memAlloc.getFloat(this.memPtr);
                this.memPtr += WORD_SIZE;

                return f;
            }

            @Override
            public boolean hasNext() {
                return this.memPtr < FloatArray.this.indexPtr;
            }
        };
    }

    @Override
    public long longSize() {
        return (this.indexPtr - this.lBound) >> SHIFT_SIZE;
    }

    @Override
    public long capacity() {
        return (this.uBound - this.lBound) >> SHIFT_SIZE;
    }

    // -----=======~~ Direct Functionality ~~~=======-----
    public float directGet(long index) {
        return memAlloc.getFloat(this.lBound + (index << SHIFT_SIZE));
    }

    public float directSet(long index, float element) {
        final long  memPtr = this.lBound + (index << SHIFT_SIZE);
        final float value  = memAlloc.getFloat(memPtr);

        memAlloc.putFloat(memPtr, element);
        return value;
    }

    public float directRemove(long index) {
        final long idx = this.lBound + (index << SHIFT_SIZE);

        final float prevValue = memAlloc.getFloat(idx);

        if (this.dynamic) {
            // Shift left
            for (long i = idx; i < this.indexPtr; i += WORD_SIZE) {
                memAlloc.putFloat(i, memAlloc.getFloat(i + WORD_SIZE));
            }

            this.indexPtr -= WORD_SIZE;
            return prevValue;
        }

        memAlloc.putFloat(idx, 0.0f);
        return prevValue;
    }
}
