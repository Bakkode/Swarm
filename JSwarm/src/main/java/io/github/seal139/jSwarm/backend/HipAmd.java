package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.core.BackendIntf;

public class HipAmd implements BackendIntf{
	private static final HipAmd instance = new HipAmd();

	public static HipAmd getInstance() {
		return instance;
	}
	
	private HipAmd() {
		
	}
}
