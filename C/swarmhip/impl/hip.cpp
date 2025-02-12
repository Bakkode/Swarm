#define __HIP_PLATFORM_AMD__

#include "../header/jni.h"
#include <vector>
#include <string>

#include <hip/hip_runtime.h>
#include <hip/hiprtc.h>



using namespace std;

    JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipGetVersion
    (JNIEnv* env, jclass clazz) {
        hipInit(0);
        
        int version = 0;
        hipError_t err = hipDriverGetVersion(&version);

        if (err != HIP_SUCCESS) {
            return env->NewStringUTF("Error");
        }

        // Convert version to string (e.g., "12.3")
        int major = version / 1000;
        int minor = (version % 1000) / 10;
        std::string versionStr = std::to_string(major) + "." + std::to_string(minor);

        return env->NewStringUTF(versionStr.c_str());
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipEnumerateDevices
    (JNIEnv*, jclass) {
        // 0 -> Error code
        // 1 -> Device count
        // 2 ~ -> Pointer
        jint* ret = new jint[2];       
        ret[0] = hipGetDeviceCount(reinterpret_cast<int*>(&ret[1]));
        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipGetDeviceInfo
    (JNIEnv*, jclass, jint device) {
        jlong* properties = new jlong[7];

        hipDeviceProp_t prop;
        hipGetDeviceProperties(&prop, device);

        // 0 Compute unit
        properties[0] = prop.multiProcessorCount;

        // 1 Global Memory
        properties[1] = prop.totalGlobalMem;

        // 2 ND Range -X
        properties[2] = prop.maxGridSize[0];

        // 3 ND Range -Y
        properties[3] = prop.maxGridSize[1];

        // 4 ND Range -Z
        properties[4] = prop.maxGridSize[2];

        // 5 Thread per block
        properties[5] = prop.maxThreadsPerBlock;

        // 6 TFLOPS
        properties[6] = static_cast<jlong>((2 * properties[0] * prop.memoryClockRate * 1000)); // TFLOPS estimate

        return reinterpret_cast<jlong>(properties);
    }

    JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipGetDeviceName
    (JNIEnv* env, jclass clazz, jint deviceId) {
        hipDeviceProp_t prop;
        char name[256]; // Buffer for device name

        // Get the device handle
        if (hipGetDeviceProperties(&prop, deviceId) != HIP_SUCCESS) {
            return env->NewStringUTF(name);
        }

        return env->NewStringUTF("Error");
    }    

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipAddQueue
    (JNIEnv*, jclass, jint count) {
        // 0 Error
        // 1 ~ Stream ptr
        jlong* ret = new jlong[count + 1];
        ret[0] = 0;

        for (int i = 0; i < count; i++) {
            ret[0] = hipStreamCreate(reinterpret_cast<hipStream_t*>(&ret[1 + i]));

            if (ret[0] == HIP_SUCCESS) {
                continue;
            }

            for (int l = 0; l < i; l++) {
                hipStreamDestroy(reinterpret_cast<hipStream_t>(ret[1 + l]));
            }

            break;
        }

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipDeleteQueue
    (JNIEnv* env, jclass clazz, jlongArray jQueue, jint count) {
        jlong* elements = env->GetLongArrayElements(jQueue, nullptr);

        hipError_t err = hipError_t::hipSuccess;
        for (int i = 0; i < count; i++) {
            hipError_t lerr = hipStreamDestroy(reinterpret_cast<hipStream_t>(elements[i]));

            if (lerr != HIP_SUCCESS) {
                err = lerr;;
            }
        }

        env->ReleaseLongArrayElements(jQueue, elements, JNI_ABORT);
        return err;
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipCreateProgram
    (JNIEnv* env, jclass clazz, jstring jsrc) {
        const char* src = env->GetStringUTFChars(jsrc, nullptr);

        jlong* ret = new jlong[2];
        ret[0] = 0;

        hiprtcProgram prog;
        ret[0] = hiprtcCreateProgram(&prog, src, "kernel.hip", 0, nullptr, nullptr);
        env->ReleaseStringUTFChars(jsrc, src);

        if (ret[0] != HIP_SUCCESS) {
            hiprtcDestroyProgram(&prog);
            return reinterpret_cast<jlong>(ret);
        }

        ret[0] = hiprtcCompileProgram(prog, 0, nullptr);
        if (ret[0] != HIP_SUCCESS) {
            hiprtcDestroyProgram(&prog);
            return reinterpret_cast<jlong>(ret);
        }

        size_t codeSize;
        ret[0] = hiprtcGetCodeSize(prog, &codeSize);
        if (ret[0] != HIP_SUCCESS) {
            hiprtcDestroyProgram(&prog);
            return reinterpret_cast<jlong>(ret);
        }

        std::vector<char> binary(codeSize);
        ret[0] = hiprtcGetCode(prog, binary.data());
        hiprtcDestroyProgram(&prog);

        if (ret[0] == HIP_SUCCESS) {
            ret[0] = hipModuleLoadData(reinterpret_cast<hipModule_t*>(&ret[1]), binary.data());
        }        

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipDeleteProgram
    (JNIEnv*, jclass, jlong program) {
        return hipModuleUnload(reinterpret_cast<hipModule_t>(program));
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipGetKernel
    (JNIEnv* env, jclass, jlong program, jstring jkName) {
        jlong* ret = new jlong[2];
        ret[0] = 0;

        const char* kernelName = env->GetStringUTFChars(jkName, nullptr);
        ret[0] = hipModuleGetFunction(reinterpret_cast<hipFunction_t*>(&ret[1]), reinterpret_cast<hipModule_t>(program), kernelName);
        env->ReleaseStringUTFChars(jkName, kernelName);

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipLaunch
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

        hipModuleLaunchKernel(
            reinterpret_cast<hipFunction_t>(kernel),
            x, y, z,       // Grid dimensions
            lx, ly, lz,    // Block dimensions
            0,             // Shared Memory - Currently unavailable
            reinterpret_cast<hipStream_t>(queue),
            args,
            0);

        delete[] args;
        env->ReleaseLongArrayElements(arguments, elements, JNI_ABORT);
    }

    JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipHook
    (JNIEnv*, jclass, jlong size) {

        jlong* ret = new jlong[2];
        ret[0] = hipMalloc(reinterpret_cast<void**>(&ret[1]), size);

        return reinterpret_cast<jlong>(ret);
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipSyncDataTo
    (JNIEnv*, jclass, jlong queue, jlong hostMemory, jlong deviceMemory, jlong size) {
        return hipMemcpyAsync(reinterpret_cast<void*>(deviceMemory), reinterpret_cast<void*>(hostMemory), size, hipMemcpyHostToDevice, reinterpret_cast<hipStream_t>(queue));
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipSyncDataFrom
    (JNIEnv*, jclass, jlong queue, jlong hostMemory, jlong deviceMemory, jlong size) {
        return hipMemcpyAsync(reinterpret_cast<void*>(deviceMemory), reinterpret_cast<void*>(hostMemory), size, hipMemcpyDeviceToHost, reinterpret_cast<hipStream_t>(queue));
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipUnhook
    (JNIEnv*, jclass, jlong deviceMemory) {
        return hipFree(reinterpret_cast<void*>(deviceMemory));
    }

    JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_hip_HipDriver_hipWaitAll
    (JNIEnv* env, jclass) {
        return hipDeviceSynchronize();
    }