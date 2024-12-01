package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.core.BackendIntf;

public class HipNv implements BackendIntf{
	private static final HipNv instance = new HipNv();

	public static HipNv getInstance() {
		return instance;
	}
	
	private HipNv() {
		
	}
}
