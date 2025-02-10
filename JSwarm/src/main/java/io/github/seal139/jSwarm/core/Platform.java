package io.github.seal139.jSwarm.core;

/**
 * Represent platform driver. This is the low level implementation that
 * communicate directly with runtime environment
 */
public interface Platform {

    /**
     * Get short name as identifier. This name should be unique.
     *
     * @return Identifier name.
     */
    String getName();

    /**
     * Get full driver name.
     *
     * @return Driver name.
     */
    String getFullName();

    /**
     * Mark vendor, device, or case -specific platform that can offer higher
     * performance. This method is used to differ preferable and fallback platform.
     * In case of Device is appear in multiple platform, an {@link Executor} object
     * for the same device from primary platform will showed up first.
     *
     * @return True when this driver is marked as 'preferable' platform
     */
    boolean isPrimary();

    /**
     * Get platform version
     *
     * @return Platform version
     */
    String getVersion();

    /**
     * Get devices {@link Executor} associated with this Platform
     *
     * @return
     */
    Executor[] getDevices();
}
