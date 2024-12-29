package io.github.seal139.jSwarm.backend.cuda;

import java.util.Collection;

import io.github.seal139.jSwarm.core.Context;
import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.Program;
import io.github.seal139.jSwarm.core.SyncDirection;
import io.github.seal139.jSwarm.runtime.datatype.Vector;

public class CudaContext implements Context {

    private final long address;
    private long       queueAddress;
    private int        queueSize;
    private int        queueIdx;

    private boolean closed = false;

    public long getAddress() { return address; }

    CudaContext(long device) throws CudaException {
        long[] r = CudaDriver.cudaInitDefault(device);

        if (r[0] != 0l) {
            throw new CudaException(r[0]);
        }

        address      = r[1];
        queueAddress = r[2];
        queueSize    = 4;
        queueIdx     = 0;
    }

    boolean isClosed() { return closed; }

    @Override
    public void close() throws Exception {
        if (closed) {
            return;
        }

        CudaDriver.cudaDeleteQueue(queueAddress, queueSize);
        CudaDriver.cudaDeleteContext(address);

        closed = true;
    }

    @Override
    public int getParallelismLevel() { return queueSize; }

    @Override
    public void addParallelismLevel(int additionalNumber) throws CudaException {
        long[] r = CudaDriver.cudaCreateQueue(queueAddress, queueSize, additionalNumber);

        if (r[0] != 0l) {
            throw new CudaException(r[0]);
        }

        queueAddress  = r[1];
        queueSize    += additionalNumber;
    }

    @Override
    public Module loadProgram(Class<? extends Program> program) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Module loadProgram(Class<? extends Program>... programs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Module loadProgram(Collection<Class<? extends Program>> programs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Module loadProgram(String program) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void launch(CudaKernel kernel) {

        // TODO Auto-generated method stub

        // waitOperation();
    }

    @Override
	public void launchAsync(Kernel kernel) {
		CudaDriver.cudaLaunch(address, address, queueAddress, queueIdx, address, address, address, address, queueSize, queueIdx, queueAddress, address)
	}

    @Override
    public void launch(Kernel kernel, int allocatedSharedMemory) {
        // TODO Auto-generated method stub

        // waitOperation();
    }

    @Override
    public void launchAsync(Kernel kernel, int allocatedSharedMemory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sync(SyncDirection direction, Vector<?>... dataCollection) {
        if (direction.equals(SyncDirection.TO_DEVICE)) {
            for (Vector<?> vec : dataCollection) {
                CudaDriver.cudaSyncDataTo(this.address, this.queueAddress, getQueueIndex(), vec.getAddress(), vec.getNativeSize());
            }
        }
        else {
            for (Vector<?> vec : dataCollection) {
                CudaDriver.cudaSyncDataFrom(this.queueAddress, getQueueIndex(), vec.getAddress(), vec.getContextAddress(this), vec.getNativeSize());
            }
        }
    }

    @Override
    public void waitOperation() throws CudaException {
        long r = CudaDriver.cudaWaitAll(queueAddress, queueSize);
        if (r != 0l) {
            throw new CudaException(r);
        }

    }

    protected int getQueueIndex() {
        int i = queueIdx++;
        if (queueIdx == queueSize) {
            queueIdx = 0;
        }

        return i;
    }

}
