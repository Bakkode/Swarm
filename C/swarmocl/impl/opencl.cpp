#include "../header/jni.h"
#include <vector>
#include <string>
#include <CL/opencl.h>

#include <iomanip>
#include <sstream>

using namespace std;

JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclGetVersion
(JNIEnv* env, jclass clazz) {
    cl_uint numPlatforms;
    clGetPlatformIDs(0, NULL, &numPlatforms);

    if (numPlatforms == 0) {
        return env->NewStringUTF("Error");
    }

    std::vector<cl_platform_id> platforms(numPlatforms);
    clGetPlatformIDs(numPlatforms, platforms.data(), NULL);

    double minVersion = 999.9; // Arbitrary high value to find the lowest

    for (cl_platform_id platform : platforms) {
        cl_uint numDevices;
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, NULL, &numDevices);

        if (numDevices == 0) continue; // No devices on this platform

        std::vector<cl_device_id> devices(numDevices);
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevices, devices.data(), NULL);

        for (cl_device_id device : devices) {
            char versionString[128];
            clGetDeviceInfo(device, CL_DEVICE_VERSION, sizeof(versionString), versionString, NULL);

            // Extract major.minor version
            double version = std::strtod(versionString + 7, NULL); // Skip "OpenCL " prefix

            if (version < minVersion) {
                minVersion = version;
            }
        }
    }

    std::ostringstream stream;
    stream << std::fixed << std::setprecision(1) << minVersion;

    return  env->NewStringUTF(stream.str().c_str());
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclEnumerateDevices
(JNIEnv*, jclass) {

    // Get number of OpenCL platforms
    cl_uint numPlatforms;
    cl_int err = clGetPlatformIDs(0, NULL, &numPlatforms);
    if (err != CL_SUCCESS) {
        jint* ret = new jint[1];
        ret[0] = err;

        return reinterpret_cast<jlong>(ret);
    }

    // Allocate memory for platforms
    cl_platform_id* platforms = new cl_platform_id[numPlatforms];
    err = clGetPlatformIDs(numPlatforms, platforms, NULL);
    if (err != CL_SUCCESS) {
        delete[] platforms;

        jint* ret = new jint[1];
        ret[0] = err;

        return reinterpret_cast<jlong>(ret);
    }

    // Cout device for each platform
    cl_uint count = 0;
    for (cl_uint i = 0; i < numPlatforms; i++) {
        cl_uint numDevices;
        err = clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL, 0, NULL, &numDevices);

        if (err == CL_SUCCESS) {
            count += numDevices;
            continue;
        }

        delete[] platforms;

        jint* ret = new jint[1];
        ret[0] = err;

        return reinterpret_cast<jlong>(ret);
    }

    jlong* ret = new jlong[2 + count];
    ret[1] = count;

    // Iterate over each platform
    cl_ulong offset = 0;
    for (cl_uint i = 0; i < numPlatforms; i++) {

        // Get number of devices for this platform
        cl_uint numDevices;
        ret[0] = clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL, 0, NULL, &numDevices);
        if (ret[0] != CL_SUCCESS) {
            break;
        }

        // Fill the device pointer
        ret[0] = clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL, numDevices, reinterpret_cast<cl_device_id*>(&ret[2 + offset]), NULL);

        if (ret[0] == CL_SUCCESS) {
            offset += numDevices;
            continue;
        }

        break;
    }

    delete[] platforms;
    return reinterpret_cast<jlong>(ret);
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclGetDeviceInfo
(JNIEnv*, jclass, jlong device) {
    jlong* properties = new jlong[9];

    // 0 Compute unit
    clGetDeviceInfo(reinterpret_cast<cl_device_id>(device), CL_DEVICE_MAX_COMPUTE_UNITS, 4, reinterpret_cast<cl_int*>(&properties[0]), NULL);

    // 1 Global Memory
    clGetDeviceInfo(reinterpret_cast<cl_device_id>(device), CL_DEVICE_GLOBAL_MEM_SIZE, 8, reinterpret_cast<cl_ulong*>(&properties[1]), NULL);

    // 2 ND Range -X, Y, Z
    clGetDeviceInfo(reinterpret_cast<cl_device_id>(device), CL_DEVICE_MAX_WORK_ITEM_SIZES, 24, reinterpret_cast<cl_long*>(&properties[2]), NULL);
    
    // 5 Thread per block
    clGetDeviceInfo(reinterpret_cast<cl_device_id>(device), CL_DEVICE_MAX_WORK_GROUP_SIZE, 8, reinterpret_cast<cl_ulong*>(&properties[5]), NULL);

    // 6 TFLOPS
    cl_uint clockRate;
    clGetDeviceInfo(reinterpret_cast<cl_device_id>(device), CL_DEVICE_MAX_CLOCK_FREQUENCY, sizeof(clockRate), &clockRate, NULL);

    char vendor[64];
    clGetDeviceInfo(reinterpret_cast<cl_device_id>(device), CL_DEVICE_VENDOR, sizeof(vendor), vendor, NULL);
    if (strstr(vendor, "NVIDIA")) {
        properties[6] = static_cast<jlong>((256 * (cl_int)properties[0] * ((float)clockRate / 1000.0))); // GFLOPS estimate
    }
    else if (strstr(vendor, "AMD")) {
        properties[6] = static_cast<jlong>((128 * (cl_int)properties[0] * ((float)clockRate / 1000.0))); // GFLOPS estimate
    }
    else {
        properties[6] = static_cast<jlong>((2 * (cl_int)properties[0] * ((float)clockRate / 1000.0))); // GFLOPS estimate
    }
    

    clGetDeviceInfo(reinterpret_cast<cl_device_id>(device), CL_DEVICE_UUID_KHR, 16, reinterpret_cast<cl_ulong*>(&properties[7]), NULL);
    
    return reinterpret_cast<jlong>(properties);
}

JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclGetDeviceName
(JNIEnv* env, jclass clazz, jlong deviceId) {

    char deviceName[256];
    if (clGetDeviceInfo(reinterpret_cast<cl_device_id>(deviceId), CL_DEVICE_NAME, sizeof(deviceName), deviceName, NULL) == CL_SUCCESS) {
        return env->NewStringUTF(deviceName);
    }

    return env->NewStringUTF("Error");
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclCreateContext
(JNIEnv*, jclass, jlong device) {
    // 0   -> Error code
    // 1   -> Context
    // 2-5 -> Queue (stream)
    jlong* ret = new jlong[6];

    // Create device context
    ret[1] = reinterpret_cast<jlong>(clCreateContext(NULL, 1, reinterpret_cast<cl_device_id*>(&device), NULL, NULL, reinterpret_cast<cl_int*>(&ret[0])));

    if (ret[0] != CL_SUCCESS) {
        clReleaseContext(reinterpret_cast<cl_context>(ret[1]));
        return reinterpret_cast<jlong>(ret);
    }

    // Create stream queue
    for (int i = 0; i < 4; i++) {
        ret[2 + i] = reinterpret_cast<jlong>(clCreateCommandQueue(reinterpret_cast<cl_context>(ret[1]), reinterpret_cast<cl_device_id>(device), 0, reinterpret_cast<cl_int*>(&ret[0])));

        if (ret[0] == CL_SUCCESS) {
            continue;
        }
        
        for (int l = 0; l < i; l++) {
            clReleaseCommandQueue(reinterpret_cast<cl_command_queue>(ret[2 + l]));
        }

        clReleaseContext(reinterpret_cast<cl_context>(ret[1]));
        break;
    }

    return reinterpret_cast<jlong>(ret);
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclDeleteContext
(JNIEnv*, jclass, jlong context) {
    return clReleaseContext(reinterpret_cast<cl_context>(context));
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclAddQueue
(JNIEnv*, jclass, jlong context, jlong device, jint count) {
    // 0 Error
    // 1 ~ Stream ptr
    jlong* ret = new jlong[count + 1];

    for (int i = 0; i < count; i++) {
        ret[1 + i] = reinterpret_cast<jlong>(clCreateCommandQueue(reinterpret_cast<cl_context>(context), reinterpret_cast<cl_device_id>(device), 0, reinterpret_cast<cl_int*>(&ret[0])));
       
        if (ret[0] == CL_SUCCESS) {            
            continue;
        }

        for (int l = 0; l < i; l++) {
            clReleaseCommandQueue(reinterpret_cast<cl_command_queue>(ret[1 + l]));
        }

        break;
    }

    return reinterpret_cast<jlong>(ret);
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclDeleteQueue
(JNIEnv* env, jclass clazz, jlongArray jQueue, jint count) {
    jlong* elements = env->GetLongArrayElements(jQueue, nullptr);

    cl_uint err = 0;
    for (int i = 0; i < count; i++) {
        cl_uint lerr = clReleaseCommandQueue(reinterpret_cast<cl_command_queue>(elements[i]));

        if (lerr != CL_SUCCESS) {
            err = lerr;;
        }
    }

    env->ReleaseLongArrayElements(jQueue, elements, JNI_ABORT);
    return err;
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclCreateProgram
(JNIEnv* env, jclass clazz, jlong device, jlong context, jstring jsrc) {
    const char* src = env->GetStringUTFChars(jsrc, nullptr);

    jlong* ret = new jlong[2];
   
    ret[1] = reinterpret_cast<jlong>(clCreateProgramWithSource(reinterpret_cast<cl_context>(context), 1, &src, NULL, reinterpret_cast<cl_int*>(&ret[0])));

    if ((cl_int)ret[0] != CL_SUCCESS) {
        return reinterpret_cast<jlong>(ret);
    }

    ret[0] = clBuildProgram(reinterpret_cast<cl_program>(ret[1]), 1, reinterpret_cast<cl_device_id*>(&device), NULL, NULL, NULL);
    if (ret[0] != CL_SUCCESS) {

        size_t logSize;
        clGetProgramBuildInfo(reinterpret_cast<cl_program>(ret[1]), reinterpret_cast<cl_device_id>(device), CL_PROGRAM_BUILD_LOG, 0, NULL, &logSize);
        char* log = (char*)malloc(logSize);
        clGetProgramBuildInfo(reinterpret_cast<cl_program>(ret[1]), reinterpret_cast<cl_device_id>(device), CL_PROGRAM_BUILD_LOG, logSize, log, NULL);

        clReleaseProgram(reinterpret_cast<cl_program>(ret[1]));
    }

    return reinterpret_cast<jlong>(ret);
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclDeleteProgram
(JNIEnv*, jclass, jlong program) {
    return clReleaseProgram(reinterpret_cast<cl_program>(program));
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclGetKernel
(JNIEnv* env, jclass, jlong program, jstring jkName) {
    jlong* ret = new jlong[2];

    const char* kernelName = env->GetStringUTFChars(jkName, nullptr);

    ret[1] = reinterpret_cast<jlong>(clCreateKernel(reinterpret_cast<cl_program>(program), kernelName, reinterpret_cast<cl_int*>(&ret[0])));

    env->ReleaseStringUTFChars(jkName, kernelName);

    return reinterpret_cast<jlong>(ret);
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclDeleteKernel
(JNIEnv* env, jclass, jlongArray jKernel, jint count) {
    jlong* elements = env->GetLongArrayElements(jKernel, nullptr);

    cl_uint err = 0;
    for (int i = 0; i < count; i++) {
        cl_uint lerr = clReleaseKernel(reinterpret_cast<cl_kernel>(elements[i]));

        if (lerr != CL_SUCCESS) {
            err = lerr;;
        }
    }

    return err;
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclLaunch
(JNIEnv* env, jclass,
    jlong kernel, jlong queue,
    jint x, jint y, jint z,
    jint lx, jint ly, jint lz,
    jlongArray arguments,jintArray argRef, jint count) {

    jlong* elements = env->GetLongArrayElements(arguments, nullptr);
    jint*  sizes    = env->GetIntArrayElements(argRef, nullptr);

    for (unsigned int i = 0; i < count; i++) {
        cl_int err = clSetKernelArg(reinterpret_cast<cl_kernel>(kernel), i, static_cast<size_t>(sizes[i]), reinterpret_cast<void*>(&elements[i]));

        if (err != CL_SUCCESS) { 
            env->ReleaseLongArrayElements(arguments, elements, JNI_ABORT);
            env->ReleaseIntArrayElements(argRef, sizes, JNI_ABORT);
            
            return;
        }
    }

    size_t globalSize[3] = { static_cast<size_t>(x), static_cast<size_t>(y), static_cast<size_t>(z) };
    size_t localSize[3]  = { static_cast<size_t>(lx), static_cast<size_t>(ly), static_cast<size_t>(lz) };
    
    cl_uint err = clEnqueueNDRangeKernel(reinterpret_cast<cl_command_queue>(queue), reinterpret_cast<cl_kernel>(kernel), 3, NULL, globalSize, localSize, 0, NULL, NULL);
 
    env->ReleaseLongArrayElements(arguments, elements, JNI_ABORT);
    env->ReleaseIntArrayElements(argRef, sizes, JNI_ABORT);

    return;
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclHook
(JNIEnv*, jclass, jlong context, jlong size) {

    jlong* ret = new jlong[2];
    ret[1] = reinterpret_cast<jlong>(clCreateBuffer(reinterpret_cast<cl_context>(context), CL_MEM_READ_WRITE, static_cast<size_t>(size), NULL, reinterpret_cast<cl_int*>(&ret[0])));

    return reinterpret_cast<jlong>(ret);
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclSyncDataTo
(JNIEnv*, jclass, jlong queue, jlong hostMemory, jlong deviceMemory, jlong size) {
    return clEnqueueWriteBuffer(reinterpret_cast<cl_command_queue>(queue), reinterpret_cast<cl_mem>(deviceMemory), CL_FALSE, 0, static_cast<size_t>(size), reinterpret_cast<void*>(hostMemory), 0, NULL, NULL);
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclSyncDataFrom
(JNIEnv*, jclass, jlong queue, jlong hostMemory, jlong deviceMemory, jlong size) {
    return clEnqueueReadBuffer(reinterpret_cast<cl_command_queue>(queue), reinterpret_cast<cl_mem>(deviceMemory), CL_FALSE, 0, static_cast<size_t>(size), reinterpret_cast<void*>(hostMemory), 0, NULL, NULL);
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclUnhook
(JNIEnv*, jclass, jlong deviceMemory) {
    return clReleaseMemObject(reinterpret_cast<cl_mem>(deviceMemory));
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_ocl_OclDriver_oclWaitAll    
(JNIEnv* env, jclass clazz, jlongArray jQueue, jint count) {
    jlong* elements = env->GetLongArrayElements(jQueue, nullptr);

    cl_uint err = 0;
    for (int i = 0; i < count; i++) {
        cl_uint lerr = clFinish(reinterpret_cast<cl_command_queue>(elements[i]));

        if (lerr != CL_SUCCESS) {
            err = lerr;;
        }
    }

    env->ReleaseLongArrayElements(jQueue, elements, JNI_ABORT);
    return err;
}