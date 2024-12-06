package io.github.seal139.jSwarm.backend.jvm;

import io.github.seal139.jSwarm.core.Context;
import io.github.seal139.jSwarm.core.DeviceType;
import io.github.seal139.jSwarm.core.Executor;

public class JvmDevice implements Executor {

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Context getDefaultContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context newContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeviceType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getComputeUnit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalMemory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxNDRange() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long[] getMaxIndexPerDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] getMaxThreadPerDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxThread() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getFlops() {
		// TODO Auto-generated method stub
		return 0;
	}

}
