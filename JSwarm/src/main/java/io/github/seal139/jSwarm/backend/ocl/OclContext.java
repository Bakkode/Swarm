package io.github.seal139.jSwarm.backend.ocl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.seal139.jSwarm.core.Context;
import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.NdRange;
import io.github.seal139.jSwarm.core.Program;
import io.github.seal139.jSwarm.core.SwarmException;
import io.github.seal139.jSwarm.core.SyncDirection;
import io.github.seal139.jSwarm.datatype.Vector;
import io.github.seal139.jSwarm.misc.Common;
import io.github.seal139.jSwarm.misc.Log;
import io.github.seal139.jSwarm.misc.NativeCleaner;
import io.github.seal139.jSwarm.misc.NativeCleaner.DeallocatedException;
import sun.misc.Unsafe;

public class OclContext implements Context {

    static final class DeallocatorImpl implements Deallocator {
        private final long              address;
        private final List<Deallocator> moduleDec    = new ArrayList<>();
        private final List<Long>        queueAddress = new ArrayList<>();

        private int     queueSize;
        private boolean isClosed = false;

        private DeallocatorImpl(long address) {
            this.address = address;
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

            int r1 = OclDriver.oclDeleteQueue(addr, this.queueSize);
            if (r1 != 0L) {
                Log.error(new OclException(r1));
            }

            // Delete this object (context)
            int r2 = OclDriver.oclDeleteContext(this.address);
            if (r2 != 0L) {
                Log.error(new OclException(r2));
            }

            this.isClosed = true;
        }
    }

    // ============ Allocator - Deallocator ==================

    private final DeallocatorImpl deallocator;
    private final Ocl             ocl = Ocl.getInstance();
    private final OclDevice       device;

    @Override
    public Deallocator getDeallocator() { return this.deallocator; }

    OclContext(OclDevice device) throws OclException {
        final Unsafe mem = Common.getMemoryManagement();

        final long intptr = OclDriver.oclCreateContext(device.getDeviceId());

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new OclException(errorCode);
        }

        this.deallocator = new DeallocatorImpl(mem.getLong(intptr + 8));
        this.device      = device;

        // By default, creating context will include 4 stream queue. this is useful for
        // concurrent data transfer
        this.deallocator.queueAddress.add(mem.getLong(intptr + 16));
        this.deallocator.queueAddress.add(mem.getLong(intptr + 24));
        this.deallocator.queueAddress.add(mem.getLong(intptr + 32));
        this.deallocator.queueAddress.add(mem.getLong(intptr + 40));
        this.deallocator.queueSize = 4;
        this.queueIdx              = 0;

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);
        NativeCleaner.register(this);
    }

    @Override
    public void close() throws Exception {
        this.deallocator.clean();
    }

    @Override
    public boolean isClosed() { return this.deallocator.isClosed; }

    // ============ Functionality Operation ==================

    @Override
    public OclDevice getDevice() { return this.device; }

    @Override
    public void activate() throws SwarmException {
        this.ocl.setContext(this.getAddress());
    }

    private int queueIdx;

    @Override
    public int getParallelismLevel() { return this.deallocator.queueSize; }

    @Override
    public void addParallelismLevel(int additionalNumber) throws OclException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        final Unsafe mem    = Common.getMemoryManagement();
        final long   intptr = OclDriver.oclAddQueue(this.device.getDeviceId(), this.ocl.getContextValue(), additionalNumber);

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new OclException(errorCode);
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

        return new OclModule(this, program);
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
        if (this.device.getMaxLocalThread() < (ndRange.getXLocal() * ndRange.getYLocal() * ndRange.getZLocal())) {
            throw new SwarmException("Local Worksize excedeed maximum thread");
        }

        long[] args = new long[arguments.length];

        {
            final int size = arguments.length;
            for (int i = 0; i < size; i++) {
                args[i] = arguments[i].getBufferAddress(this);
            }

        }

        OclDriver.oclLaunch(((OclKernel) kernel).getAddress(), hitQueueIndex(), //
                ndRange.getXGlobal() * ndRange.getXLocal(), ndRange.getYGlobal() * ndRange.getYLocal(), ndRange.getZGlobal() * ndRange.getZLocal(), //
                ndRange.getXLocal(), ndRange.getYLocal(), ndRange.getZLocal(), //
                args, args.length);
    }

    // ==== buffer memory management ====

    @Override
    public void hook(Vector<? extends Number> vector) throws OclException {
        final Unsafe mem = Common.getMemoryManagement();

        long intptr = OclDriver.oclHook(this.ocl.getContextValue(), vector.longSize() * vector.getValueSize());

        int errorCode = (int) mem.getLong(intptr);
        if (errorCode != 0) {
            // Don't forget to deallocate memory
            mem.freeMemory(intptr);
            throw new OclException(errorCode);
        }

        vector.setBuffer(this, mem.getLong(intptr + 8));

        // Don't forget to deallocate memory
        mem.freeMemory(intptr);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sync(SyncDirection direction, Vector<? extends Number>... dataCollection) throws OclException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        if (SyncDirection.TO_DEVICE.equals(direction)) {
            for (Vector<?> vec : dataCollection) {
                int errorCode = OclDriver.oclSyncDataTo(hitQueueIndex(), //
                        vec.getNativeAddress(), vec.getBufferAddress(this), vec.longSize() * vec.getValueSize());

                if (errorCode != 0) {
                    throw new OclException(errorCode);
                }
            }
        }

        for (Vector<?> vec : dataCollection) {
            int errorCode = OclDriver.oclSyncDataFrom(hitQueueIndex(), //
                    vec.getNativeAddress(), vec.getBufferAddress(this), vec.longSize() * vec.getValueSize());

            if (errorCode != 0) {
                throw new OclException(errorCode);
            }
        }
    }

    @Override
    public void unhook(Vector<? extends Number> vector) throws OclException {
        int errorCode = OclDriver.oclUnhook(vector.getBufferAddress(this));
        if (errorCode != 0) {
            throw new OclException(errorCode);
        }

        vector.removeBuffer(this);
    }

    @Override
    public void reHook(Vector<? extends Number> vector) throws OclException {
        int errorCode = OclDriver.oclUnhook(vector.getBufferAddress(this));
        if (errorCode != 0) {
            throw new OclException(errorCode);
        }

        hook(vector);
    }
    // ==== Fence ====

    @Override
    public void waitOperation() throws OclException, DeallocatedException {
        if (isClosed()) {
            throw new DeallocatedException();
        }

        long[] addr = new long[this.deallocator.queueSize];
        for (int i = 0; i < this.deallocator.queueSize; i++) {
            addr[i] = this.deallocator.queueAddress.get(i).longValue();
        }

        int errorCode = OclDriver.oclWaitAll(addr, this.deallocator.queueSize);
        if (errorCode != 0) {
            throw new OclException(errorCode);
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

    long getAddress() { return this.deallocator.address; }

    @Override
    public int hashCode() {
        // Use native memory address instead
        return (int) getAddress();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj.hashCode() == hashCode() //
        ) && (obj instanceof OclContext //
        ) && (((OclModule) obj).getAddress() == getAddress());
    }
}
