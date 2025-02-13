package io.github.seal139.jSwarm.backend.hip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.seal139.jSwarm.core.Context;
import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.NativeCleaner;
import io.github.seal139.jSwarm.core.NativeCleaner.DeallocatedException;
import io.github.seal139.jSwarm.core.NdRange;
import io.github.seal139.jSwarm.core.Program;
import io.github.seal139.jSwarm.core.SwarmException;
import io.github.seal139.jSwarm.core.SyncDirection;
import io.github.seal139.jSwarm.datatype.Vector;
import io.github.seal139.jSwarm.misc.Common;
import io.github.seal139.jSwarm.misc.Log;
import sun.misc.Unsafe;

public class HipContext implements Context {

    static final class DeallocatorImpl implements Deallocator {
        private final int               deviceId;
        private final List<Deallocator> moduleDec    = new ArrayList<>();
        private final List<Long>        queueAddress = new ArrayList<>();

        private int     queueSize;
        private boolean isClosed = false;

        private DeallocatorImpl(int address) {
            this.deviceId = address;
        }

        @Override
        public void clean() {
            // Delete module and it's corresponding kernel
            this.moduleDec.forEach(Deallocator::clean);

            // Delete queue (stream)
            long[] addr = new long[this.queueSize];
            for (int i = 0; i < this.queueSize; i++) {
                addr[i] = this.queueAddress.get(i).longValue();
            }

            int r1 = HipDriver.hipDeleteQueue(addr, this.queueSize);
            if (r1 != 0L) {
                Log.error(new HipException(r1));
            }

            // Delete this object (context)
            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    HipContext(HipDevice device) throws HipException {
        this.deallocator = new DeallocatorImpl(device.getDeviceId());

        this.deallocator.queueSize = 0;
        this.queueIdx              = 0;

        // Don't forget to deallocate memory
        NativeCleaner.register(this);

        activate();

        try {
            addParallelismLevel(4);
        }
        catch (DeallocatedException de) {

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
    public void activate() throws HipException {
        int err = HipDriver.hipSetDevice((int) getAddress());

        if (err != 0) {
            throw new HipException(err);
        }
    }

    private int queueIdx;

    @Override
    public int getParallelismLevel() { return this.deallocator.queueSize; }

    @Override
    public void addParallelismLevel(int additionalNumber) throws HipException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        final Unsafe mem    = Common.getMemoryManagement();
        final long   intptr = HipDriver.hipAddQueue(additionalNumber);

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new HipException(errorCode);
        }

        this.deallocator.queueSize += additionalNumber;

        long lBound = intptr + 8;
        long uBound = lBound + (8 * additionalNumber);

        for (long queuePtr = lBound; queuePtr < uBound; queuePtr += 8) {
            this.deallocator.queueAddress.add(mem.getLong(queuePtr));
        }

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);
    }

    @Override
    public Module loadProgram(Class<? extends Program> program) throws SwarmException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        String src = "";
        return loadProgram(src);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Module loadProgram(Class<? extends Program>... programs) throws SwarmException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        String src = "";
        return loadProgram(src);
    }

    @Override
    public Module loadProgram(Collection<Class<? extends Program>> programs) throws SwarmException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        String src = "";
        return loadProgram(src);
    }

    @Override
    public Module loadProgram(String program) throws SwarmException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        return new HipModule(this, program);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void launch(Kernel kernel, NdRange ndRange, Vector<? extends Number>... arguments) throws SwarmException, DeallocatedException {
        launchAsync(kernel, ndRange, arguments);
        waitOperation();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void launchAsync(Kernel kernel, NdRange ndRange, Vector<? extends Number>... arguments) throws SwarmException, DeallocatedException {
        long[] args = new long[arguments.length];

        {
            final int size = arguments.length;
            for (int i = 0; i < size; i++) {
                args[i] = arguments[i].getBufferAddress(this);
            }

        }

        HipDriver.hipLaunch(((HipKernel) kernel).getAddress(), hitQueueIndex(), //
                ndRange.getXGlobal(), ndRange.getYGlobal(), ndRange.getZGlobal(), //
                ndRange.getXlocal(), ndRange.getYLocal(), ndRange.getZLocal(), //
                args, args.length);
    }

    // ==== buffer memory management ====

    @Override
    public void hook(Vector<? extends Number> vector) throws HipException {
        final Unsafe mem = Common.getMemoryManagement();

        long intptr = HipDriver.hipHook(vector.longSize() * vector.getValueSize());

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new HipException(errorCode);
        }

        vector.setBuffer(this, mem.getLong(intptr + 8));

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sync(SyncDirection direction, Vector<? extends Number>... dataCollection) throws HipException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        if (SyncDirection.TO_DEVICE.equals(direction)) {
            for (Vector<?> vec : dataCollection) {
                int errorCode = HipDriver.hipSyncDataTo(hitQueueIndex(), //
                        vec.getNativeAddress(), vec.getBufferAddress(this), vec.longSize() * vec.getValueSize());

                if (errorCode != 0) {
                    throw new HipException(errorCode);
                }
            }
        }

        for (Vector<?> vec : dataCollection) {
            int errorCode = HipDriver.hipSyncDataFrom(hitQueueIndex(), //
                    vec.getNativeAddress(), vec.getBufferAddress(this), vec.longSize() * vec.getValueSize());

            if (errorCode != 0) {
                throw new HipException(errorCode);
            }
        }
    }

    @Override
    public void unhook(Vector<? extends Number> vector) throws HipException {
        int errorCode = HipDriver.hipUnhook(vector.getBufferAddress(this));
        if (errorCode != 0) {
            throw new HipException(errorCode);
        }

        vector.removeBuffer(this);
    }

    @Override
    public void reHook(Vector<? extends Number> vector) throws HipException {
        int errorCode = HipDriver.hipUnhook(vector.getBufferAddress(this));
        if (errorCode != 0) {
            throw new HipException(errorCode);
        }

        hook(vector);
    }
    // ==== Fence ====

    @Override
    public void waitOperation() throws HipException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        int errorCode = HipDriver.hipWaitAll();
        if (errorCode != 0) {
            throw new HipException(errorCode);
        }
    }

    // ==== ====

    protected long hitQueueIndex() {
        if (this.queueIdx == this.deallocator.queueSize) {
            this.queueIdx = 0;
        }

        return this.deallocator.queueAddress.get(this.queueIdx++);
    }

    // =============== Object Operation ======================

    long getAddress() { return this.deallocator.deviceId; }

    @Override
    public int hashCode() {
        // Use native memory address instead
        return (int) getAddress();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj.hashCode() == hashCode() //
        ) && (obj instanceof HipContext //
        ) && (((HipModule) obj).getAddress() == getAddress());
    }
}
