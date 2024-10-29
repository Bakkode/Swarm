#include <iostream>
#include <vector>
#include <CL/cl.h>

// OpenCL kernel as a string
const char* kernelSource = R"(
__kernel void vecAdd(__global const float* A, 
                     __global const float* B, 
                     __global float* C, 
                     const unsigned int N) {
    int id = get_global_id(0);
    if (id < N) {
        C[id] = A[id] + B[id];
    }
}
)";

void printDeviceInfo(cl_device_id device) {
    char deviceName[128];
    cl_uint computeUnits;
    size_t maxWorkGroupSize;
    cl_uint maxWorkItemDimensions;
    std::vector<size_t> maxWorkItemSizes;
    cl_ulong globalMemSize;
    cl_ulong maxMemAllocSize;

    cl_device_type deviceType;
    clGetDeviceInfo(device, CL_DEVICE_TYPE, sizeof(deviceType), &deviceType, nullptr);

    // Check device type and print it
    if (deviceType & CL_DEVICE_TYPE_CPU) {
        std::cout << "Device Type: CPU" << std::endl;
    }
    if (deviceType & CL_DEVICE_TYPE_GPU) {
        std::cout << "Device Type: GPU" << std::endl;
    }
    if (deviceType & CL_DEVICE_TYPE_ACCELERATOR) {
        std::cout << "Device Type: Accelerator" << std::endl;
    }
    if (deviceType & CL_DEVICE_TYPE_DEFAULT) {
        std::cout << "Device Type: Default" << std::endl;
    }

    // Get device name
    clGetDeviceInfo(device, CL_DEVICE_NAME, sizeof(deviceName), deviceName, nullptr);
    std::cout << "Device Name: " << deviceName << std::endl;

    // Get number of compute units
    clGetDeviceInfo(device, CL_DEVICE_MAX_COMPUTE_UNITS, sizeof(computeUnits), &computeUnits, nullptr);
    std::cout << "Compute Units: " << computeUnits << std::endl;

    // Get total memory size
    clGetDeviceInfo(device, CL_DEVICE_GLOBAL_MEM_SIZE, sizeof(globalMemSize), &globalMemSize, nullptr);
    std::cout << "Global Memory Size: " << globalMemSize << std::endl;  // Convert bytes to MB

    // Get maximum number of work item dimensions
    clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS, sizeof(maxWorkItemDimensions), &maxWorkItemDimensions, nullptr);

    // Get maximum work item sizes
    maxWorkItemSizes.resize(maxWorkItemDimensions);
    clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_ITEM_SIZES, sizeof(size_t) * maxWorkItemDimensions, maxWorkItemSizes.data(), nullptr);
    std::cout << "Max Work Item Sizes: ["
        << maxWorkItemSizes[0] << ", "
        << maxWorkItemSizes[1] << ", "
        << maxWorkItemSizes[2] << "]" << std::endl;
    
    // Get maximum thread per group
    clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_GROUP_SIZE, sizeof(maxWorkGroupSize), &maxWorkGroupSize, nullptr);
    std::cout << "Max Work Group Size: " << maxWorkGroupSize << std::endl;

    cl_uint clockFrequency;
    cl_device_fp_config fpConfig;

    // Get the maximum clock frequency (in MHz)
    clGetDeviceInfo(device, CL_DEVICE_MAX_CLOCK_FREQUENCY, sizeof(clockFrequency), &clockFrequency, nullptr);

    // Check if the device supports double precision floating-point
    clGetDeviceInfo(device, CL_DEVICE_DOUBLE_FP_CONFIG, sizeof(fpConfig), &fpConfig, nullptr);

    // Assume 2 FLOPS per clock cycle for FP32 or FP64 depending on support
    int flopsPerCycle = (fpConfig != 0) ? 2 : 1;  // Typically 2 for GPUs with FP32, varies with precision

    // Peak FLOPS = Compute Units * Clock Frequency * FLOPS per Cycle
    double peakFLOPS = static_cast<double>(computeUnits) * clockFrequency * 1e6 * flopsPerCycle;

    std::cout << "Estimated Peak FLOPS: " << peakFLOPS / 1e12 << " TFLOPS" << std::endl;

    std::cout << "-----------------------------" << std::endl;
}

void check() {
    // Initialize OpenCL
    cl_uint platformCount;
    clGetPlatformIDs(0, nullptr, &platformCount); // Get number of platforms
    std::vector<cl_platform_id> platforms(platformCount);
    clGetPlatformIDs(platformCount, platforms.data(), nullptr); // Get platforms

    // Iterate through each platform
    for (const auto& platform : platforms) {
        char platformName[128];
        clGetPlatformInfo(platform, CL_PLATFORM_NAME, sizeof(platformName), platformName, nullptr);
        std::cout << "Platform Name: " << platformName << std::endl;

        // Get devices for the platform
        cl_uint deviceCount;
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, nullptr, &deviceCount); // Get number of devices
        std::vector<cl_device_id> devices(deviceCount);
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, deviceCount, devices.data(), nullptr); // Get devices

        // Print device information
        for (const auto& device : devices) {
            printDeviceInfo(device);
        }
    }
}


int _main() {
    check();

    // Vector sizes
    const int N = 1024;
    std::vector<float> A(N, 1.0f); // Initialize A with 1.0
    std::vector<float> B(N, 2.0f); // Initialize B with 2.0
    std::vector<float> C(N);       // Output vector C

    // Step 1: Get platform and device information
    cl_platform_id platform;
    clGetPlatformIDs(1, &platform, NULL);

    cl_device_id device;
    clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 1, &device, NULL);

    // Step 2: Create an OpenCL context
    cl_context context = clCreateContext(NULL, 1, &device, NULL, NULL, NULL);

    // Step 3: Create a command queue
    cl_command_queue queue = clCreateCommandQueue(context, device, 0, NULL);

    // Step 4: Create memory buffers
    cl_mem bufferA = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
        sizeof(float) * N, A.data(), NULL);
    cl_mem bufferB = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
        sizeof(float) * N, B.data(), NULL);
    cl_mem bufferC = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
        sizeof(float) * N, NULL, NULL);

    // Step 5: Create a program from the kernel source
    cl_program program = clCreateProgramWithSource(context, 1,
        &kernelSource, NULL, NULL);

    // Step 6: Build the program
    clBuildProgram(program, 1, &device, NULL, NULL, NULL);

    // Step 7: Create the OpenCL kernel
    cl_kernel kernel = clCreateKernel(program, "vecAdd", NULL);

    // Step 8: Set kernel arguments
    clSetKernelArg(kernel, 0, sizeof(cl_mem), &bufferA);
    clSetKernelArg(kernel, 1, sizeof(cl_mem), &bufferB);
    clSetKernelArg(kernel, 2, sizeof(cl_mem), &bufferC);
    clSetKernelArg(kernel, 3, sizeof(unsigned int), &N);

    // Step 9: Execute the kernel
    size_t globalSize = N; // Total number of threads
    clEnqueueNDRangeKernel(queue, kernel, 1, NULL, &globalSize, NULL, 0, NULL, NULL);

    // Step 10: Read back the result
    clEnqueueReadBuffer(queue, bufferC, CL_TRUE, 0, sizeof(float) * N, C.data(), 0, NULL, NULL);

    // Step 11: Cleanup
    clReleaseMemObject(bufferA);
    clReleaseMemObject(bufferB);
    clReleaseMemObject(bufferC);
    clReleaseProgram(program);
    clReleaseKernel(kernel);
    clReleaseCommandQueue(queue);
    clReleaseContext(context);

    // Output the result
    for (int i = 0; i < 10; i++) {
        std::cout << C[i] << " "; // Print the first 10 results
    }
    std::cout << std::endl;

    return 0;
}
