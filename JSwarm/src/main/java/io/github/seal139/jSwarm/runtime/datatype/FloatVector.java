package io.github.seal139.jSwarm.runtime.datatype;

import java.util.Collection;

import io.github.seal139.jSwarm.core.NativeCleaner;
import io.github.seal139.jSwarm.core.NativeException;

public final class FloatVector extends Vector<Float> {

    @Override
    protected int size_t() {
        return 4;
    }

    private static class FloatCacheDeallocator extends CacheDeallocator {
        boolean cleaned = false;

        protected FloatCacheDeallocator(long address) {
            super(address);
        }

        @Override
        public void clean() {
            if (this.cleaned) {
                return;
            }

            this.cleaned = true;
            unhook(this.address);
        }
    }

    @Override
    protected CacheDeallocator getCDealloc(long addr) {
        return new FloatCacheDeallocator(addr);
    }

    private final class FloatDeallocator extends VectorDeallocator {
        private FloatDeallocator(long vAddress, long cAddress) {
            super(vAddress, cAddress);
        }

        @Override
        public void clean() {
            super.clean();

            fp32Delete(this.vectorAddress);
            unhook(this.cacheAddress);
        }
    }

    // ============ Allocator - Deallocator ==================

    private final VectorDeallocator deallocator;

    private final long vectorAddress;
    private final long cacheAddress;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    private static final int DEF_CACHE_SIZE = 128;
    private static final int DEF_INIT_SIZE  = 256;

    private static final int SMALL_OPTIMIZED = /* x */16;

    public FloatVector() throws NativeException {
        this(DEF_INIT_SIZE, DEF_CACHE_SIZE);
    }

    public FloatVector(int initial) throws NativeException {
        this(initial, (initial < DEF_CACHE_SIZE) ? initial : DEF_CACHE_SIZE);
    }

    private final long ubnd;

    public FloatVector(int initial, int cacheSize) throws NativeException {
        super(cacheSize);

        this.vectorAddress = fp32Construct(initial);
        this.cacheAddress  = hook(this.cacheSize);

        this.unsafePtr = this.cacheAddress;
        this.ubnd      = this.cacheAddress + this.cacheSize;

        this.deallocator = new FloatDeallocator(this.vectorAddress, this.cacheAddress);
        NativeCleaner.register(this);
    }

    @Override
    public void flush() {
        if (this.unsafePtr > this.cacheAddress) {
            sync();
        }
    }

    @Override
    public void close() throws Exception {
        this.deallocator.clean();
    }

    @Override
    public boolean isClosed() { return this.deallocator.isClosed; }

    // ============ Functionality Operation ==================

    @Override
    public boolean add(Float e) {
        UNSAFE.putFloat(this.unsafePtr, e.floatValue());
        this.unsafePtr += 4;

        if (this.unsafePtr == this.ubnd) {
            sync();
        }

        return true;
    }

    @Override
    protected Float convert(Number num) {
        return num.floatValue();
    }

    @Override
    public void sync() {
        fp32Sync(this.vectorAddress, this.cacheAddress, this.unsafePtr, 2f);
        this.unsafePtr = this.cacheAddress;
    }

    @Override
    protected void fetch(long cacheAddress, int from, int to) {
        fp32Fetch(this.vectorAddress, cacheAddress, from, to);
    }

    @Override
    public int size() {
        return fp32GetSize(this.vectorAddress);
    }

    @Override
    public boolean remove(Object o) {
        return fp32Remove(this.vectorAddress, ((Float) o).floatValue());
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) > -1;
    }

    @Override
    public void clear() {
        fp32Clear(this.vectorAddress);
    }

    @Override
    public Float get(int index) {
        return fp32GetByIndex(this.vectorAddress, index);
    }

    @Override
    public Float set(int index, Float element) {
        return fp32Set(this.vectorAddress, index, element.floatValue());
    }

    @Override
    public Float remove(int index) {
        return fp32RemoveByIndex(this.vectorAddress, index);
    }

    @Override
    public int indexOf(Object o) {
        return fp32GetIndex(this.vectorAddress, ((Float) o).floatValue());
    }

    @Override
    public int lastIndexOf(Object o) {
        return fp32GetLastIndex(this.vectorAddress, ((Float) o).floatValue());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        float[] ref = new float[c.size()];

        int index = -1;
        for (Object f : c) {
            ref[++index] = ((Float) f).floatValue();
        }

        return fp32ContainsAll(this.vectorAddress, ref);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        float[] ref = new float[c.size()];

        int index = -1;
        for (Object f : c) {
            ref[++index] = ((Float) f).floatValue();
        }

        return fp32RemoveAll(this.vectorAddress, ref);
    }

    // -----======= Native Operation =======-----

    private static native long fp32Hook_(float[] src); // Hook cache-array to native side
    private static native void fp32Unhook_(long address); // Unhook cache-array

    private static native long fp32Construct(int cacheSize); // Create Vector
    private static native void fp32Delete(long address); // Deallocate Vector

    private static native int fp32GetSize(long address); // Get the size of Vector

    private static native void fp32Sync(long address, long bufferAddress, long size, float resizePolicy); // [i, x) Synchronize data from cache to
                                                                                                          // Vector
    private static native void fp32Fetch(long address, long bufferAddress, int begin, int to); // [i, x) Synchronize data from Vector to cache

    private static native boolean fp32ContainsAll(long address, float[] nums); // Is this Vector contains all the specified value?
    private static native float fp32GetByIndex(long address, int index); // Get value from Vector at specified index
    private static native int fp32GetIndex(long address, float num); // Get the first Vector index of specified value
    private static native int fp32GetLastIndex(long address, float num); // Get the last Vector index of specified value

    private static native boolean fp32Remove(long address, float num); // Remove value from Vector
    private static native boolean fp32RemoveAll(long address, float[] nums); // Remove all contained value from Vector
    private static native float fp32RemoveByIndex(long address, int index); // Remove value at specified index

    private static native float fp32Set(long address, int index, float value); // Set value at specified index

    private static native void fp32Clear(long address); // Clear vector entirely

}
