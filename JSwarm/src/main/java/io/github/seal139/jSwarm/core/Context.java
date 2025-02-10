package io.github.seal139.jSwarm.core;

import java.util.Collection;

import io.github.seal139.jSwarm.core.NativeCleaner.DeallocatedException;
import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;
import io.github.seal139.jSwarm.datatype.Vector;

/**
 * Represent execution context. In terms of GPGPU, Context plays a crucial parts
 * to manage resource.
 *
 */
public interface Context extends NativeResources {

    /**
     * Load a {@link Class} of {@link Program} into a {@link Module}.
     *
     * @param program A {@link Class} of {@link Program}.
     * @return A {@link Module}.
     */
    Module loadProgram(Class<? extends Program> program) throws SwarmException, DeallocatedException;

    /**
     * Load a {@link Class} of {@link Program} from a variadic (varargs) array. This
     * will combine each {@link Program} into a {@link Module}.
     *
     * @param programs A variadic (varargs) array that holds {@link Class}
     *                 definition of {@link Program}.
     * @return A {@link Module}.
     */
    Module loadProgram(Class<? extends Program>... programs) throws SwarmException, DeallocatedException;

    /**
     * Load a {@link Class} of {@link Program} from {@link Collection}. This will
     * combine each {@link Program} into a {@link Module}.
     *
     * @param programs A {@link Collection} that holds {@link Class} definition of
     *                 {@link Program}.
     * @return A {@link Module}.
     */
    Module loadProgram(Collection<Class<? extends Program>> programs) throws SwarmException, DeallocatedException;

    /**
     * Load program by source.
     *
     * @param program Source code.
     * @return {@link Module}.
     */
    Module loadProgram(String program) throws SwarmException, DeallocatedException;

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
    void addParallelismLevel(int additionalNumber) throws SwarmException, DeallocatedException;

    // ==== Launcher ====
    /**
     * Launch kernel.
     *
     * @param kernel {@link Kernel} to launch.
     */
    void launch(Kernel kernel, NdRange ndRange, Vector<? extends Number>... arguments) throws SwarmException, DeallocatedException;

    void launchAsync(Kernel kernel, NdRange ndRange, Vector<? extends Number>... arguments) throws SwarmException, DeallocatedException;

    // ==== buffer memory management ====
    void hook(Vector<? extends Number> vector) throws SwarmException;
    void sync(SyncDirection direction, Vector<? extends Number>... dataCollection) throws SwarmException, DeallocatedException;
    void unhook(Vector<? extends Number> vector) throws SwarmException;
    void reHook(Vector<? extends Number> vector) throws SwarmException;

    // ==== Fence ====
    void waitOperation() throws SwarmException, DeallocatedException;
}
