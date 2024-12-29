package io.github.seal139.jSwarm.backend.cuda;

import io.github.seal139.jSwarm.core.SwarmException;

/**
 * CUDA-related exception
 */
public final class CudaException extends SwarmException {

    private static final long serialVersionUID = -3271554093353725232L;

    CudaException(long l) {
        super(constructMessage(l));
    }

    private static String constructMessage(long l) {
        String hex        = "0x" + String.format("%016X", l);
        String errMessage = CudaDriver.getErrorMessage(l);

        return "Error " + hex + ": " + errMessage;
    }
}
