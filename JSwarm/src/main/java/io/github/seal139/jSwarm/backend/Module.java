package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.misc.NativeCleaner.DeallocatedException;
import io.github.seal139.jSwarm.misc.NativeCleaner.NativeResources;

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
    Kernel getKernel(String kernelName) throws BackendException, DeallocatedException;
}
