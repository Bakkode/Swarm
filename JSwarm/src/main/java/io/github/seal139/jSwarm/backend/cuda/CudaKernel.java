package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.runtime.datatype.Vector;

public class CudaKernel implements Kernel {

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setArgument(int index, Vector<? extends Number> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGlobalNDSize(long... dim) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocalNDSize(long... dim) {
		// TODO Auto-generated method stub

	}

}
