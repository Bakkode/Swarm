package io.github.seal139.jSwarm.core;

/**
 * Represent GPGPU Program that contains kernel function
 */
public abstract class Program {
	long[] indexPtr;
	long[] localPtr;
	
	protected long getCurrentIndex(int dimension) {
		return indexPtr[dimension];
	}
	
	protected long getLocalIndex(int dimension) {
		return localPtr[dimension];
	}
	
	protected void synchronize() {
		
	}
}
