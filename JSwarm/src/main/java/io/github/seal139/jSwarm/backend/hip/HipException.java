package io.github.seal139.jSwarm.backend.hip;

import io.github.seal139.jSwarm.core.SwarmException;

/**
 * HIP-related exception
 */
public final class HipException extends SwarmException {

    private static final long serialVersionUID = -3271554093353725232L;

    HipException(int i) {
        super(constructMessage(i));
    }

    private static String constructMessage(int i) {
        String hex = "0x" + String.format("%016X", i);

        // see cuda.h files
        return switch (i) {
        case 1 -> hex + " HIP_ERROR_INVALID_VALUE : ";

        case 2 -> hex + " HIP_ERROR_OUT_OF_MEMORY : ";

        case 3 -> hex + " HIP_ERROR_NOT_INITIALIZED";

        case 4 -> hex + " HIP_ERROR_DEINITIALIZED";

        case 5 -> hex + " HIP_ERROR_PROFILER_DISABLED";

        case 6 -> hex + " HIP_ERROR_PROFILER_NOT_INITIALIZED";

        case 7 -> hex + " HIP_ERROR_PROFILER_ALREADY_STARTED";

        case 8 -> hex + " HIP_ERROR_PROFILER_ALREADY_STOPPED";

        case 34 -> hex + " HIP_ERROR_STUB_LIBRARY";

        case 46 -> hex + " HIP_ERROR_DEVICE_UNAVAILABLE";

        case 100 -> hex + " HIP_ERROR_NO_DEVICE";

        case 101 -> hex + " HIP_ERROR_INVALID_DEVICE";

        case 102 -> hex + " HIP_ERROR_DEVICE_NOT_LICENSED";

        case 200 -> hex + " HIP_ERROR_INVALID_IMAGE";

        case 201 -> hex + " HIP_ERROR_INVALID_CONTEXT";

        case 202 -> hex + " HIP_ERROR_CONTEXT_ALREADY_CURRENT";

        case 205 -> hex + " HIP_ERROR_MAP_FAILED";

        case 206 -> hex + " HIP_ERROR_UNMAP_FAILED";

        case 207 -> hex + " HIP_ERROR_ARRAY_IS_MAPPED";

        case 208 -> hex + " HIP_ERROR_ALREADY_MAPPED";

        case 209 -> hex + " HIP_ERROR_NO_BINARY_FOR_GPU";

        case 210 -> hex + " HIP_ERROR_ALREADY_ACQUIRED";

        case 211 -> hex + " HIP_ERROR_NOT_MAPPED";

        case 212 -> hex + " HIP_ERROR_NOT_MAPPED_AS_ARRAY";

        case 213 -> hex + " HIP_ERROR_NOT_MAPPED_AS_POINTER";

        case 214 -> hex + " HIP_ERROR_ECC_UNCORRECTABLE";

        case 215 -> hex + " HIP_ERROR_UNSUPPORTED_LIMIT";

        case 216 -> hex + " HIP_ERROR_CONTEXT_ALREADY_IN_USE";

        case 217 -> hex + " HIP_ERROR_PEER_ACCESS_UNSUPPORTED";

        case 218 -> hex + " HIP_ERROR_INVALID_PTX";

        case 219 -> hex + " HIP_ERROR_INVALID_GRAPHICS_CONTEXT";

        case 220 -> hex + " HIP_ERROR_NVLINK_UNCORRECTABLE";

        case 221 -> hex + " HIP_ERROR_JIT_COMPILER_NOT_FOUND";

        case 222 -> hex + " HIP_ERROR_UNSUPPORTED_PTX_VERSION";

        case 223 -> hex + " HIP_ERROR_JIT_COMPILATION_DISABLED";

        case 224 -> hex + " HIP_ERROR_UNSUPPORTED_EXEC_AFFINITY";

        case 225 -> hex + " HIP_ERROR_UNSUPPORTED_DEVSIDE_SYNC";

        case 300 -> hex + " HIP_ERROR_INVALID_SOURCE";

        case 301 -> hex + " HIP_ERROR_FILE_NOT_FOUND";

        case 302 -> hex + " HIP_ERROR_SHARED_OBJECT_SYMBOL_NOT_FOUND";

        case 303 -> hex + " HIP_ERROR_SHARED_OBJECT_INIT_FAILED";

        case 304 -> hex + " HIP_ERROR_OPERATING_SYSTEM";

        case 400 -> hex + " HIP_ERROR_INVALID_HANDLE";

        case 401 -> hex + " HIP_ERROR_ILLEGAL_STATE";

        case 402 -> hex + " HIP_ERROR_LOSSY_QUERY";

        case 500 -> hex + " HIP_ERROR_NOT_FOUND";

        case 600 -> hex + " HIP_ERROR_NOT_READY";

        case 700 -> hex + " HIP_ERROR_ILLEGAL_ADDRESS";

        case 701 -> hex + " HIP_ERROR_LAUNCH_OUT_OF_RESOURCES";

        case 702 -> hex + " HIP_ERROR_LAUNCH_TIMEOUT";

        case 703 -> hex + " HIP_ERROR_LAUNCH_INCOMPATIBLE_TEXTURING";

        case 704 -> hex + " HIP_ERROR_PEER_ACCESS_ALREADY_ENABLED";

        case 705 -> hex + " HIP_ERROR_PEER_ACCESS_NOT_ENABLED";

        case 708 -> hex + " HIP_ERROR_PRIMARY_CONTEXT_ACTIVE";

        case 709 -> hex + " HIP_ERROR_CONTEXT_IS_DESTROYED";

        case 710 -> hex + " HIP_ERROR_ASSERT";

        case 711 -> hex + " HIP_ERROR_TOO_MANY_PEERS";

        case 712 -> hex + " HIP_ERROR_HOST_MEMORY_ALREADY_REGISTERED";

        case 713 -> hex + " HIP_ERROR_HOST_MEMORY_NOT_REGISTERED";

        case 714 -> hex + " HIP_ERROR_HARDWARE_STACK_ERROR";

        case 715 -> hex + " HIP_ERROR_ILLEGAL_INSTRUCTION";

        case 716 -> hex + " HIP_ERROR_MISALIGNED_ADDRESS";

        case 717 -> hex + " HIP_ERROR_INVALID_ADDRESS_SPACE";

        case 718 -> hex + " HIP_ERROR_INVALID_PC";

        case 719 -> hex + " HIP_ERROR_LAUNCH_FAILED";

        case 720 -> hex + " HIP_ERROR_COOPERATIVE_LAUNCH_TOO_LARGE";

        case 800 -> hex + " HIP_ERROR_NOT_PERMITTED";

        case 801 -> hex + " HIP_ERROR_NOT_SUPPORTED";

        case 802 -> hex + " HIP_ERROR_SYSTEM_NOT_READY";

        case 803 -> hex + " HIP_ERROR_SYSTEM_DRIVER_MISMATCH";

        case 804 -> hex + " HIP_ERROR_COMPAT_NOT_SUPPORTED_ON_DEVICE";

        case 805 -> hex + " HIP_ERROR_MPS_CONNECTION_FAILED";

        case 806 -> hex + " HIP_ERROR_MPS_RPC_FAILURE";

        case 807 -> hex + " HIP_ERROR_MPS_SERVER_NOT_READY";

        case 808 -> hex + " HIP_ERROR_MPS_MAX_CLIENTS_REACHED";

        case 809 -> hex + " HIP_ERROR_MPS_MAX_CONNECTIONS_REACHED";

        case 810 -> hex + " HIP_ERROR_MPS_CLIENT_TERMINATED";

        case 811 -> hex + " HIP_ERROR_CDP_NOT_SUPPORTED";

        case 812 -> hex + " HIP_ERROR_CDP_VERSION_MISMATCH";

        case 900 -> hex + " HIP_ERROR_STREAM_CAPTURE_UNSUPPORTED";

        case 901 -> hex + " HIP_ERROR_STREAM_CAPTURE_INVALIDATED";

        case 902 -> hex + " HIP_ERROR_STREAM_CAPTURE_MERGE";

        case 903 -> hex + " HIP_ERROR_STREAM_CAPTURE_UNMATCHED";

        case 904 -> hex + " HIP_ERROR_STREAM_CAPTURE_UNJOINED";

        case 905 -> hex + " HIP_ERROR_STREAM_CAPTURE_ISOLATION";

        case 906 -> hex + " HIP_ERROR_STREAM_CAPTURE_IMPLICIT";

        case 907 -> hex + " HIP_ERROR_CAPTURED_EVENT";

        case 908 -> hex + " HIP_ERROR_STREAM_CAPTURE_WRONG_THREAD";

        case 909 -> hex + " HIP_ERROR_TIMEOUT";

        case 910 -> hex + " HIP_ERROR_GRAPH_EXEC_UPDATE_FAILURE";

        case 911 -> hex + " HIP_ERROR_EXTERNAL_DEVICE";

        case 912 -> hex + " CUDA_ERROR_INVALID_CLUSTER_SIZE";

        case 913 -> hex + " HIP_ERROR_FUNCTION_NOT_LOADED";

        case 914 -> hex + " HIP_ERROR_INVALID_RESOURCE_TYPE";

        case 915 -> hex + " HIP_ERROR_INVALID_RESOURCE_CONFIGURATION";

        default -> hex + " HIP_ERROR_UNKNOWN";
        };
    }

}
