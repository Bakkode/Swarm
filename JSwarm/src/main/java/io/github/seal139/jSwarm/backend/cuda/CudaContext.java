package io.github.seal139.jSwarm.backend.cuda;

import java.util.Collection;

import io.github.seal139.jSwarm.core.Context;
import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.Program;
import io.github.seal139.jSwarm.core.SyncDirection;

public class CudaContext implements Context {

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

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
	public int getParallelismLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addParallelismLevel(int additionalNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void launch(Kernel kernel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void launchAsync(Kernel kernel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void launch(Kernel kernel, int allocatedSharedMemory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void launchAsync(Kernel kernel, int allocatedSharedMemory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sync(SyncDirection direction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void waitOperation() {
		// TODO Auto-generated method stub

	}

}
