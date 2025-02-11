package io.github.seal139.jSwarm.core;

/**
 * Represent device hardware.
 */
public interface Executor {

    /**
     * Get default execution context.
     *
     * @return Default managed {@link Context}.
     */
    Context getDefaultContext() throws SwarmException;

    /**
     * Create new execution context. additional context is not needed in most
     * situation and only cause additional overhead that can reduce performance
     * significantly. we strongly discourage you to call this method unless for
     * specific case that need multiple context.
     *
     * @return Newly created {@link Context}.
     */
    Context newContext() throws SwarmException;

    /**
     * Get the type of the hardware device used by this Executor.
     *
     * @return The {@link DeviceType}.
     */
    DeviceType getType();

    /**
     * Get the name of the device.
     *
     * @return Name of the device. This value is not unique and cannot be used to
     *         identify physical hardware.
     */
    String getName();

    /**
     * Get logical processor core in the device.
     *
     * @return The number of logical processor core.
     */
    long getComputeUnit();

    /**
     * Get total memory that can be used by the device.
     *
     * @return Total memory in bytes.
     */
    long getTotalMemory();

    /**
     * Return maximum ND-Range supported by the device. ND-Range represent total
     * dimension count that can be used to process data. <br/>
     * <br/>
     * For example, this function will return 1 if the device only accept single
     * dimension in X axis. return 2 for two-dimensional of X and Y axis. And 3 for
     * three-dimensional of X, Y, and Z axis. <br/>
     * <br/>
     * In most device, this function will return 3 that represent three-dimensional
     * of X, Y, and Z axis.
     *
     * @return Max dimension supported.
     */
    int getMaxNDRange();

    /**
     * Get maximum upper-bound value for each dimension used for work-node.
     *
     * @return Array of max index per dimension.
     */
    long[] getMaxIndexPerDimension();

    /**
     * Get maximum local thread that can run per work-node based on specific
     * dimension.
     *
     * @return Maximum local thread. This value is maximum possible per dimension
     *         not a product.
     */
    long getMaxThreadPerDimension();

    /**
     * Get device maximum theoretical performance.
     *
     * @return FLOPS number in Gigaflop.
     */
    long getFlops();
}
