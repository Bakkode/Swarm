package io.github.seal139.jSwarm.core;

import io.github.seal139.jSwarm.runtime.datatype.Vector;

public interface BackendIntf {
	
	/**
	 * Backend platform name
	 * @return platform name
	 */
	String getPlatformName();
	
	/**
	 * Enumerate all device supported
	 * @return Hardware device list
	 */
	Device[] enumerate();
	
	/**
	 * Create new context (Responsible to manage resource used for kernel execution)
	 * @param hardware target hardware will be used
	 * @return Context
	 */
	Module loadProgram(Device Device, String program);
	
	/**
	 * Load GPGU kernel (Method) from Module
	 * 
	 * @param module
	 * @param kernelName method name
	 * @return
	 */
	Kernel getKernel(Module module, String kernelName);
	
	
	void launch(Kernel kernel, int x, int y, int z, int depth, Vector<?> ... parameter);
	
	/**
	 * Construct platform-specific kernel code from generic source
	 * @param raw
	 * @return
	 */
	String constructSourceAoT(String raw);
	
	
}
