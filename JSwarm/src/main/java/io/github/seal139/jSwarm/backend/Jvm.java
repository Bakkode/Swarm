package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.core.BackendIntf;

public class Jvm implements BackendIntf{
	private static final Jvm instance = new Jvm();
	
	public static Jvm getInstance() {
		return instance;
	}
	
	private Jvm() {
		
	}
}
