package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.core.BackendIntf;

public class OpenCL implements BackendIntf{
	private static final OpenCL instance = new OpenCL();
	
	public static OpenCL getInstance() {
		return instance;
	}
	
	private OpenCL() {
		
	}
}
