package io.github.seal139.jSwarm.core;

/**
 * Represent GPU module or programs that holds {@link Kernel}
 */
public interface Module extends AutoCloseable {

	/**
	 * Get kernel function.
	 * @param kernelName Name of the {@link Kernel}.
	 * @return {@link Kernel}.
	 */
	Kernel getKernel(String kernelName);
}
