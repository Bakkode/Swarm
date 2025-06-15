package io.github.seal139.jSwarm.backend.jvm;

import java.util.HashSet;
import java.util.Set;

import io.github.seal139.jSwarm.backend.BackendException;
import io.github.seal139.jSwarm.backend.Context;
import io.github.seal139.jSwarm.backend.Executor;
import io.github.seal139.jSwarm.backend.Kernel;
import io.github.seal139.jSwarm.backend.Module;
import io.github.seal139.jSwarm.datatype.Vector;
import io.github.seal139.jSwarm.misc.NativeCleaner.DeallocatedException;
import io.github.seal139.jSwarm.runtime.NdRange;
import io.github.seal139.jSwarm.runtime.Program;
import io.github.seal139.jSwarm.runtime.SyncDirection;

public class JvmContext implements Context {

    @Override
    public Deallocator getDeallocator() { return null; }

    @Override
    public boolean isClosed() { // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close() throws Exception {
        // NoOp
    }

    @Override
    public void activate() throws BackendException {
        // NoOp
    }

    @Override
    public Executor getDevice() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Module loadProgram(Class<? extends Program> program) throws BackendException, DeallocatedException {
        return new JvmModule(program);
    }

    @Override
    public int getParallelismLevel() { // TODO Auto-generated method stub
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public void addParallelismLevel(int additionalNumber) throws BackendException, DeallocatedException {
        // NoOp
    }

    @Override
    public void launch(Kernel kernel, NdRange ndRange, Number... arguments) throws BackendException, DeallocatedException {
        JvmKernel jKernel = (JvmKernel) kernel;
        jKernel.run(ndRange, arguments);
        jKernel.await();
    }

    private Set<JvmKernel> processingKernel = new HashSet<>();

    @Override
    public void launchAsync(Kernel kernel, NdRange ndRange, Number... arguments) throws BackendException, DeallocatedException {
        JvmKernel jKernel = (JvmKernel) kernel;
        jKernel.run(ndRange, arguments);

        this.processingKernel.add(jKernel);
    }

    @Override
    public void hook(Vector<? extends Number> vector) throws BackendException {
        // NoOp
    }

    @Override
    public void sync(SyncDirection direction, Vector<? extends Number>... dataCollection) throws BackendException, DeallocatedException {
        // NoOp
    }

    @Override
    public void unhook(Vector<? extends Number> vector) throws BackendException {
        // NoOp
    }

    @Override
    public void reHook(Vector<? extends Number> vector) throws BackendException {
        // NoOp
    }

    @Override
    public void waitOperation() throws BackendException, DeallocatedException {
        this.processingKernel.forEach(JvmKernel::await);
        this.processingKernel.clear();
    }

}
