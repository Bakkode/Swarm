package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.core.Platform;

public class Cuda implements Platform{

	@Override
	public String getName() {
		return "CUDA";
	}
	
	@Override
	public String getFullName() {
		return "NVIDIA CUDA";
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public String getVersion() {
		return "12.6";
	}

	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Executor[] getDevices() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// Return std
	// 0 error code
	// 1 value count
	// 2 ... value pointer
	
	
	/*
	 * 0   error code
	 * 1 ~ device pointer
	 */
	static native long[] enumerateCuDevice();
	
	/*
	 * 0   error code
	 * 1   context pointer
	 * 2-5 queues
	 */
	static native long[] initCuDefault(long device);
	
	/*
	 * 0   error code
	 * 1   context pointer
	 * 2-5 queues
	 */
	static native long[] createCuContext(long device);
	
	/*
	 * 0 error code
	 * 1 program pointer
	 */
	static native long[] createCuProgram(long context, String source);
	
	/*
	 * 0 error code
	 * 1 kernel pointer
	 */
	static native long[] createKernel(long program, String name);
	
	/*
	 * 0   error code
	 * 1 ~ queues pointer
	 */
	static native long[] createQueue(int count);

}
