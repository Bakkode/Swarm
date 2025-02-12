#include <vector>
#include <string>

#include <cuda.h>
#include <nvrtc.h>

#include "../header/jni.h"

using namespace std;

    JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetVersion
    (JNIEnv* env, jclass clazz) {
        cuInit(0);
        
        int version = 0;
        CUresult err = cuDriverGetVersion(&version);

        if (err != CUDA_SUCCESS) {
            return env->NewStringUTF("Error");
        }

        // Convert version to string (e.g., "12.3")
        int major = version / 1000;
        int minor = (version % 1000) / 10;
        std::string versionStr = std::to_string(major) + "." + std::to_string(minor);

        return env->NewStringUTF(versionStr.c_str());
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaEnumerateDevices
    (JNIEnv*, jclass) {

        int deviceCount = 0;
        CUresult err = cuDeviceGetCount(&deviceCount);
        if (err != CUDA_SUCCESS) {
            jint* ret = new jint[1];
            ret[0] = err;

            return reinterpret_cast<jlong>(ret);
        }

        // 0 -> Error code
        // 1 -> Device count
        // 2 ~ -> Pointer
        jint* ret = new jint[2 + deviceCount];
        //cudaDeviceProp* deviceProps = new cudaDeviceProp[deviceCount];

        ret[0] = 0;           // Store error code
        ret[1] = deviceCount; // Store device count

        for (int i = 0; i < deviceCount; ++i) {
            //cudaGetDeviceProperties(&deviceProps[i], i);
            CUdevice device;
            ret[0] = cuDeviceGet(&device, i);
            if (ret[0] == CUDA_SUCCESS) {
                ret[2 + i] = static_cast<jint>(device);
            }
            else {
                break;
            }
        }

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetDeviceInfo
    (JNIEnv*, jclass, jint device) {
        jlong* properties = new jlong[7];

        // 0 Compute unit
        cuDeviceGetAttribute(reinterpret_cast<int*>(&properties[0]), CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT, device);

        // 1 Global Memory
        cuDeviceTotalMem(reinterpret_cast<size_t*>(&properties[1]), device);

        // 2 ND Range -X
        cuDeviceGetAttribute(reinterpret_cast<int*>(&properties[2]), CU_DEVICE_ATTRIBUTE_MAX_GRID_DIM_X, device);

        // 3 ND Range -Y
        cuDeviceGetAttribute(reinterpret_cast<int*>(&properties[3]), CU_DEVICE_ATTRIBUTE_MAX_GRID_DIM_Y, device);

        // 4 ND Range -Z
        cuDeviceGetAttribute(reinterpret_cast<int*>(&properties[4]), CU_DEVICE_ATTRIBUTE_MAX_GRID_DIM_Z, device);

        // 5 Thread per block
        cuDeviceGetAttribute(reinterpret_cast<int*>(&properties[5]), CU_DEVICE_ATTRIBUTE_MAX_THREADS_PER_BLOCK, device);

        int clockRate, coresPerSM;
        cuDeviceGetAttribute(&clockRate, CU_DEVICE_ATTRIBUTE_CLOCK_RATE, device); // Clock rate in kHz
        cuDeviceGetAttribute(&coresPerSM, CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT, device); // Compute unit count

        int major, minor;
        cuDeviceGetAttribute(&major, CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MAJOR, device);
        cuDeviceGetAttribute(&minor, CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MINOR, device);

        if (major == 1) coresPerSM *= 8;   // Tesla
        else if (major == 2) coresPerSM *= 32;  // Fermi
        else if (major == 3) coresPerSM *= 192; // Kepler
        else if (major == 5) coresPerSM *= 128; // Maxwell
        else if (major == 6) coresPerSM *= (minor == 0) ? 64 : 128; // Pascal
        else if (major == 7) coresPerSM *= (minor == 0) ? 64 : 128; // Volta/Turing
        else if (major >= 8) coresPerSM *= 128; // Ampere
        //else if (major == 9) coresPerSM *= 128; // Hopper

        // 6 TFLOPS
        properties[6] = static_cast<jlong>((2 * coresPerSM * clockRate * 1000)); // TFLOPS estimate

        return reinterpret_cast<jlong>(properties);
    }

    JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetDeviceName
    (JNIEnv* env, jclass clazz, jint deviceId) {
        CUdevice device;
        char name[256]; // Buffer for device name

        // Get the device handle
        if (cuDeviceGet(&device, deviceId) != CUDA_SUCCESS) {
            return env->NewStringUTF("Error");
        }

        // Get the device name
        if (cuDeviceGetName(name, 256, device) != CUDA_SUCCESS) {
            return env->NewStringUTF("Error");
        }

        return env->NewStringUTF(name);
    }


    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaCreateContext
    (JNIEnv*, jclass, jint device) {
        // 0   -> Error code
        // 1   -> Context
        // 2-5 -> Queue (stream)
        jlong* ret = new jlong[6];;
        ret[0] = 0;

        CUcontext* context = reinterpret_cast<CUcontext*>(&ret[1]);
        ret[0] = cuCtxCreate(context, CU_CTX_SCHED_SPIN, static_cast<CUdevice>(device));
        if (ret[0] != CUDA_SUCCESS) {
            return reinterpret_cast<jlong>(ret);
        }

        cuCtxSetCurrent(*context);

        for (int i = 0; i < 4; i++) {
            ret[0] = cuStreamCreate(reinterpret_cast<CUstream*>(&ret[2 + i]), CU_STREAM_NON_BLOCKING);

            if (ret[0] == CUDA_SUCCESS) {
                continue;
            }

            for (int l = 0; l < i; l++) {
                cuStreamDestroy(reinterpret_cast<CUstream>(ret[2 + l]));
            }

            cuCtxDestroy(*context);
            break;
        }

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaDeleteContext
    (JNIEnv*, jclass, jlong context) {
        return cuCtxDestroy(reinterpret_cast<CUcontext>(context));
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaSetContext
    (JNIEnv*, jclass, jlong context) {
        return cuCtxSetCurrent(reinterpret_cast<CUcontext>(context));        
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaAddQueue
    (JNIEnv*, jclass, jint count) {
        // 0 Error
        // 1 ~ Stream ptr
        jlong* ret = new jlong[count + 1];
        ret[0] = 0;

        for (int i = 0; i < count; i++) {
            ret[0] = cuStreamCreate(reinterpret_cast<CUstream*>(&ret[1 + i]), CU_STREAM_NON_BLOCKING);

            if (ret[0] == CUDA_SUCCESS) {
                continue;
            }

            for (int l = 0; l < i; l++) {
                cuStreamDestroy(reinterpret_cast<CUstream>(ret[1 + l]));
            }

            break;
        }

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaDeleteQueue
    (JNIEnv* env, jclass clazz, jlongArray jQueue, jint count) {
        jlong* elements = env->GetLongArrayElements(jQueue, nullptr);

        CUresult err = CUDA_SUCCESS;
        for (int i = 0; i < count; i++) {
            CUresult lerr = cuStreamDestroy(reinterpret_cast<CUstream>(elements[i]));

            if (lerr != CUDA_SUCCESS) {
                err = lerr;;
            }
        }

        env->ReleaseLongArrayElements(jQueue, elements, JNI_ABORT);
        return err;
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaCreateProgram
    (JNIEnv* env, jclass clazz, jstring jsrc) {
        const char* src = env->GetStringUTFChars(jsrc, nullptr);

        jlong* ret = new jlong[2];
        ret[0] = 0;

        nvrtcProgram prog;
        nvrtcResult err = nvrtcCreateProgram(&prog, src, "kernel.cu", 0, nullptr, nullptr);
        env->ReleaseStringUTFChars(jsrc, src);

        if (err != NVRTC_SUCCESS) {
            ret[0] = CUDA_ERROR_INVALID_SOURCE;
            return reinterpret_cast<jlong>(ret);
        }

        err = nvrtcCompileProgram(prog, 0, nullptr);
        if (err != NVRTC_SUCCESS) {
            ret[0] = CUDA_ERROR_INVALID_SOURCE;

            nvrtcDestroyProgram(&prog);
            return reinterpret_cast<jlong>(ret);
        }

        size_t ptxSize;
        err = nvrtcGetPTXSize(prog, &ptxSize);
        if (err != NVRTC_SUCCESS) {
            ret[0] = CUDA_ERROR_INVALID_PTX;

            nvrtcDestroyProgram(&prog);
            return reinterpret_cast<jlong>(ret);
        }

        vector<char> ptx(ptxSize);
        err = nvrtcGetPTX(prog, ptx.data());
        if (err != NVRTC_SUCCESS) {
            ret[0] = CUDA_ERROR_INVALID_PTX;

            nvrtcDestroyProgram(&prog);
            return reinterpret_cast<jlong>(ret);
        }

        nvrtcDestroyProgram(&prog);

        ret[0] = cuModuleLoadData(reinterpret_cast<CUmodule*>(&ret[1]), ptx.data());

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaDeleteProgram
    (JNIEnv*, jclass, jlong program) {
        return cuModuleUnload(reinterpret_cast<CUmodule>(program));
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetKernel
    (JNIEnv* env, jclass, jlong program, jstring jkName) {
        jlong* ret = new jlong[2];
        ret[0] = 0;

        const char* kernelName = env->GetStringUTFChars(jkName, nullptr);
        ret[0] = cuModuleGetFunction(reinterpret_cast<CUfunction*>(&ret[1]), reinterpret_cast<CUmodule>(program), kernelName);
        env->ReleaseStringUTFChars(jkName, kernelName);

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaLaunch
    (JNIEnv* env, jclass,
        jlong kernel, jlong queue,
        jint x, jint y, jint z,
        jint lx, jint ly, jint lz,
        jlongArray arguments, jint count) {
       
        jlong* elements = env->GetLongArrayElements(arguments, nullptr);
        void** args = new void*[count];
        for (int i = 0; i < count; i++) {
            args[i] = reinterpret_cast<void*>(&elements[i]);
        }

        cuLaunchKernel(
            reinterpret_cast<CUfunction>(kernel),
            x, y, z,       // Grid dimensions
            lx, ly, lz,    // Block dimensions
            0,             // Shared Memory - Currently unavailable
            reinterpret_cast<CUstream>(queue),
            args,
            0);

        delete[] args;
        env->ReleaseLongArrayElements(arguments, elements, JNI_ABORT);
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaHook
    (JNIEnv*, jclass, jlong size) {

        jlong* ret = new jlong[2];
        ret[0] = cuMemAlloc(reinterpret_cast<CUdeviceptr*>(&ret[1]), size);

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaSyncDataTo
    (JNIEnv*, jclass, jlong queue, jlong hostMemory, jlong deviceMemory, jlong size) {
        return cuMemcpyHtoDAsync(static_cast<CUdeviceptr>(deviceMemory), reinterpret_cast<void*>(hostMemory), size, reinterpret_cast<CUstream>(queue));
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaSyncDataFrom
    (JNIEnv*, jclass, jlong queue, jlong hostMemory, jlong deviceMemory, jlong size) {
        return cuMemcpyDtoHAsync(reinterpret_cast<void*>(hostMemory), static_cast<CUdeviceptr>(deviceMemory), size, reinterpret_cast<CUstream>(queue));
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaUnhook
    (JNIEnv*, jclass, jlong deviceMemory) {
        return cuMemFree(static_cast<CUdeviceptr>(deviceMemory));
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaWaitAll
    (JNIEnv* env, jclass) {
        return cuCtxSynchronize();
    }
