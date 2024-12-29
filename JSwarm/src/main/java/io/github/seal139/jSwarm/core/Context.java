package io.github.seal139.jSwarm.core;

import java.util.Collection;

import io.github.seal139.jSwarm.runtime.datatype.Vector;

/**
 * Represent execution context. In terms of GPGPU, Context plays a crucial parts
 * to manage resource.
 *
 */
public interface Context extends AutoCloseable {

    /**
     * Load a {@link Class} of {@link Program} into a {@link Module}.
     *
     * @param program A {@link Class} of {@link Program}.
     * @return A {@link Module}.
     */
    Module loadProgram(Class<? extends Program> program);

    /**
     * Load a {@link Class} of {@link Program} from a variadic (varargs) array. This
     * will combine each {@link Program} into a {@link Module}.
     *
     * @param programs A variadic (varargs) array that holds {@link Class}
     *                 definition of {@link Program}.
     * @return A {@link Module}.
     */
    Module loadProgram(Class<? extends Program>... programs);

    /**
     * Load a {@link Class} of {@link Program} from {@link Collection}. This will
     * combine each {@link Program} into a {@link Module}.
     *
     * @param programs A {@link Collection} that holds {@link Class} definition of
     *                 {@link Program}.
     * @return A {@link Module}.
     */
    Module loadProgram(Collection<Class<? extends Program>> programs);

    /**
     * Load program by source.
     * 
     * @param program Source code.
     * @return {@link Module}.
     */
    Module loadProgram(String program);

    /**
     * Get the maximum number of concurrent operations at once. By default this
     * value is 4.
     * 
     * @return the maximum number of concurrent operations in parallel.
     */
    int getParallelismLevel();

    /**
     * Add new parallelism capabilities to existing. Useful if working with multiple
     * kernel that can run independently. Also applicable in case to synchronize
     * many {@link Vector} that contains massive amount of data.
     * 
     * @param additionalNumber Number of added parallelism.
     */
    void addParallelismLevel(int additionalNumber) throws SwarmException;

    /**
     * Launch kernel.
     * 
     * @param kernel {@link Kernel} to launch.
     */
    void launch(Kernel kernel);

    /**
     * Launch kernel asynchronously
     * 
     * @param kernel {@link Kernel} to launch.
     */
    void launchAsync(Kernel kernel);

    /**
     * Launch kernel with shared local memory.
     * 
     * @param kernel                {@link Kernel} to launch.
     * @param allocatedSharedMemory Total memory allocated in bytes.
     */
    void launch(Kernel kernel, int allocatedSharedMemory);

    /**
     * Launch kernel asynchronously with shared local memory.
     * 
     * @param kernel                {@link Kernel} to launch.
     * @param allocatedSharedMemory Total memory allocated in bytes.
     */
    void launchAsync(Kernel kernel, int allocatedSharedMemory);

    /**
     * Synchronize data to device
     * 
     * @param direction Sync direction. To device or from device
     */
    void sync(SyncDirection direction, Vector<?>... dataCollection);

    /**
     * Wait operation for complete
     */
    void waitOperation() throws SwarmException;
}
