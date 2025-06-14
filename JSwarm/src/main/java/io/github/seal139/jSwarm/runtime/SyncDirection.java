package io.github.seal139.jSwarm.runtime;

/**
 * Control synchronization direction
 */
public enum SyncDirection {
    /**
     * Synchronize data from device to host.
     */
    TO_DEVICE,

    /**
     * Synchronize data from host to device.
     */
    TO_HOST
}
