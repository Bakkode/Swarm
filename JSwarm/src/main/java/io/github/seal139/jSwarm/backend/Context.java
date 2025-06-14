package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.datatype.Vector;
import io.github.seal139.jSwarm.misc.NativeCleaner.DeallocatedException;
import io.github.seal139.jSwarm.misc.NativeCleaner.NativeResources;
import io.github.seal139.jSwarm.runtime.NdRange;
import io.github.seal139.jSwarm.runtime.Program;
import io.github.seal139.jSwarm.runtime.SyncDirection;

/**
 * Represent execution context. In terms of GPGPU, Context plays a crucial parts
 * to manage resource on the device side. Context is supposed to tightly coupled
 * with {@link Thread}.
 *
 */
public interface Context extends NativeResources {

    /**
     * Activate this {@link Context} before used. Attempting an action to inactive
     * {@link Context} may cause undefined behavior. The activated {@link Context}
     * is belong to the caller {@link Thread}. It's possible for multiple
     * {@link Thread} that uses the same {@link Context} but only one active
     * {@link Context} every {@link Thread} is permitted.
     *
     * @throws BackendException
     */
    void activate() throws BackendException;

    /**
     * Get the {@link Executor Device} associated with this context.
     *
     * @return Associated {@link Executor Device}
     */
    Executor getDevice();

    /**
     * Load a {@link Class} of {@link Program} into a {@link Module}.
     *
     * @param program A {@link Class} of {@link Program}.
     * @return A {@link Module}.
     */
    Module loadProgram(Class<? extends Program> program) throws BackendException, DeallocatedException;

    /**
     * Get the maximum number of concurrent operations at once. By default this
     * value is 4 or less (based on maximum supported by the platform).
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
    void addParallelismLevel(int additionalNumber) throws BackendException, DeallocatedException;

    // ==== Launcher ====
    /**
     * Launch kernel and wait for it to finish it execution.
     *
     * @param kernel    Valid {@link Kernel} that belongs to this {@link Context}
     * @param ndRange   Used working size per dimension
     * @param arguments Data to be processed and the output
     * @throws BackendException
     * @throws DeallocatedException
     */
    void launch(Kernel kernel, NdRange ndRange, Number... arguments) throws BackendException, DeallocatedException;

    /**
     * Launch kernel and return immediately. Don't forget to call
     * {@link #waitOperation()} before fetch the result
     *
     * @param kernel    Valid {@link Kernel} that belongs to this {@link Context}
     * @param ndRange   Used working size per dimension
     * @param arguments Data to be processed and the output
     * @throws BackendException
     * @throws DeallocatedException
     */
    void launchAsync(Kernel kernel, NdRange ndRange, Number... arguments) throws BackendException, DeallocatedException;

    // ==== buffer memory management ====

    /**
     * Hook {@link Vector} to VRAM for synchronization purpose. This works by
     * allocate memory on the device side based on the size of referenced
     * {@link Vector}
     *
     * @param vector Any valid {@link Vector} or it's derivatives.
     * @throws BackendException
     */
    void hook(Vector<? extends Number> vector) throws BackendException;

    /**
     * Synchronized {@link #hook hooked} {@link Vector} between host and device
     * memory.
     *
     * @param direction      Sync direction
     * @param dataCollection Any valid {@link Vector} or it's derivatives to be
     *                       synchronized.
     * @throws BackendException
     * @throws DeallocatedException
     */
    @SuppressWarnings("unchecked")
    void sync(SyncDirection direction, Vector<? extends Number>... dataCollection) throws BackendException, DeallocatedException;

    /**
     * Unhook {@link Vector} if no more synchronization is needed and release
     * resources used by device
     *
     * @param vector
     * @throws BackendException
     */
    void unhook(Vector<? extends Number> vector) throws BackendException;

    /**
     * Re-initialized device memory. This is should be called if referenced
     * {@link Vector} {@link Vector#size() size} is changed.
     *
     * @param vector referenced {@link Vector}
     * @throws BackendException
     */
    void reHook(Vector<? extends Number> vector) throws BackendException;

    /**
     * Wait for pending operation to be done.
     *
     * @throws BackendException
     * @throws DeallocatedException
     */
    void waitOperation() throws BackendException, DeallocatedException;
}
