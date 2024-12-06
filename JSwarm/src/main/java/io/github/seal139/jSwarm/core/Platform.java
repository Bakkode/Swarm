package io.github.seal139.jSwarm.core;

/**
 * Represent platofrm driver. This is the low level implementation that communicate directly with runtime environment
 */
public interface Platform {
	void init();
	
	String getName();
	
	String getFullName();
	
	boolean isPrimary();
	
	String getVersion();
	
	Executor[] getDevices();
}
