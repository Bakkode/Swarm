package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.core.BackendIntf;

public class Cuda implements BackendIntf{
	private static final Cuda instance = new Cuda();
	
	public static Cuda getInstance() {
		return instance;
	}
	
	private Cuda() {
		
	}
}
