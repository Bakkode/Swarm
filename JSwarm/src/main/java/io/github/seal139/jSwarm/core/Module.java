package io.github.seal139.jSwarm.core;

import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;

/**
 * Represent GPU module or programs that holds {@link Kernel}
 */
public interface Module extends NativeResources {

    /**
     * Get kernel function.
     *
     * @param kernelName Name of the {@link Kernel}.
     * @return {@link Kernel}.
     */
    Kernel getKernel(String kernelName) throws SwarmException;
}
