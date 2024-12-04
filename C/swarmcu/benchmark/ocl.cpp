#include <iostream>
#include <vector>

#include "../header/info.h"
#include <CL/cl.h>


using namespace std;

intptr_t* getDevice(int platformIndex, int deviceIndex) {
    intptr_t* ret = new intptr_t[2];

    cl_uint*      errorCode = new cl_uint;
    cl_device_id* devices   = new cl_device_id;

    // Store the pointer addresses in ret
    ret[0] = (intptr_t)errorCode;
    ret[1] = (intptr_t)devices;

    cl_platform_id* platform = new cl_platform_id[platformIndex + 1];
    clGetPlatformIDs(platformIndex + 1, platform, nullptr); // Get platforms
    
    cl_device_id* dvcs = new cl_device_id[deviceIndex + 1];
    clGetDeviceIDs(platform[platformIndex], CL_DEVICE_TYPE_ALL, deviceIndex + 1, dvcs, nullptr); // Get devices

    *devices = dvcs[deviceIndex];
    delete[] dvcs;
    delete[] platform;

    char deviceName[128];
    clGetDeviceInfo(*devices, CL_DEVICE_NAME, sizeof(deviceName), deviceName, nullptr);
    std::cout << "Device Name\t\t: " << deviceName << std::endl;

    

    return ret;

    // Don't forget to delete
    //delete (cl_uint*)ret[0];
    //delete (cl_device_id*)ret[1];
    //delete[] ret;
}

intptr_t* createContext(intptr_t device) {
    intptr_t* ret = new intptr_t[2];

    cl_int* errorCode = new cl_int;
    cl_context* ctx   = new cl_context;

    // Store the pointer addresses in ret
    ret[0] = (intptr_t)errorCode;
    ret[1] = (intptr_t)ctx;

    *ctx = clCreateContext(NULL, 1, (cl_device_id*) device, NULL, NULL, errorCode);

    return ret;
}

intptr_t* createProgram(intptr_t context, intptr_t device, const char* source) {
    intptr_t* ret = new intptr_t[2];

    cl_int* errorCode = new cl_int;
    cl_program* prog = new cl_program;

    // Store the pointer addresses in ret
    ret[0] = (intptr_t)errorCode;
    ret[1] = (intptr_t)prog;

    *prog = clCreateProgramWithSource( *((cl_context *)context), 1, &source, NULL, errorCode);
    
    clBuildProgram(*prog, 1, (cl_device_id*)device, NULL, NULL, NULL);

    return ret;
}

intptr_t* createKernel(intptr_t program, const char* kernelName) {
    intptr_t* ret = new intptr_t[2];

    cl_int* errorCode = new cl_int;
    cl_kernel* kernel = new cl_kernel;

    // Store the pointer addresses in ret
    ret[0] = (intptr_t)errorCode;
    ret[1] = (intptr_t)kernel;

    *kernel = clCreateKernel(*((cl_program*)program), kernelName, NULL);

    return ret;
}

intptr_t* createQueue(intptr_t context, intptr_t device) {
    intptr_t* ret = new intptr_t[2];

    cl_int* errorCode = new cl_int;
    cl_command_queue* queues = new cl_command_queue[4];

    ret[0] = (intptr_t)errorCode;
    ret[1] = (intptr_t)queues;

    queues[0] = clCreateCommandQueue(*((cl_context*)context), *((cl_device_id*)device), 0, errorCode);
    queues[1] = clCreateCommandQueue(*((cl_context*)context), *((cl_device_id*)device), 0, errorCode);
    queues[2] = clCreateCommandQueue(*((cl_context*)context), *((cl_device_id*)device), 0, errorCode);
    queues[3] = clCreateCommandQueue(*((cl_context*)context), *((cl_device_id*)device), 0, errorCode);

    return ret;
}

void wait(intptr_t queues) {
    cl_command_queue* queue = (cl_command_queue*)queues;

    clFinish(queue[0]);
    clFinish(queue[1]);
    clFinish(queue[2]);
    clFinish(queue[3]);
}

intptr_t copyData(  intptr_t context, 
                    intptr_t dataSource, size_t dataSize, size_t dataCount,
                    intptr_t queue, int queueIndex) {

    cl_mem* buffer = new cl_mem;
    size_t memSize = dataSize * dataCount;

    *buffer = clCreateBuffer(*((cl_context*)context), CL_MEM_READ_WRITE, memSize, NULL, NULL);
    clEnqueueWriteBuffer(((cl_command_queue*)queue)[queueIndex], *buffer, CL_FALSE, 0, memSize, (const void*)dataSource, 0, NULL, NULL);

    return (intptr_t)buffer;
}

void execute(
                intptr_t kernel,
                intptr_t queue, int queueIndex, 
                intptr_t* args, size_t argCount,
                const size_t globX, const size_t globY, const size_t globZ,
                const size_t localX, const size_t localY, const size_t localZ) {

    for (int i = 0; i < argCount; i++) {
        clSetKernelArg(*((cl_kernel*)kernel), i, sizeof(cl_mem), (cl_mem*)args[i]);
    }

    size_t global_work_size[3] = { globX, globY, globZ };
    size_t local_work_size[3] = { localX, localY, localZ };

    wait(queue);
    clEnqueueNDRangeKernel(
            ((cl_command_queue*)queue)[queueIndex], 
            *((cl_kernel*)kernel), 
            3, 
            NULL, 
            global_work_size, 
            local_work_size,
            0, NULL, NULL);
    wait(queue);
}


int _main() {
    printCLDevices();

    const char* kernelSource = R"(
    __kernel void vecAdd(__global const float* A, 
                         __global const float* B, 
                         __global float* C, 
                         __global int* N) {
        int id = get_global_id(0);
        if (id < N[0]) {
            C[id] = A[id] * B[id] + 2;
        }
    }
    )";
    
    intptr_t device = getDevice(0, 0)[1];
    intptr_t context = createContext(device)[1];

    intptr_t program = createProgram(context, device, kernelSource)[1];
    intptr_t kernel = createKernel(program, "vecAdd")[1];

    intptr_t queues = createQueue(context, device)[1];
    
    const long long N = 500000000;
    std::vector<float> A(N, 1.0f); // Initialize A with 1.0
    std::vector<float> B(N, 2.0f); // Initialize B with 2.0
    std::vector<float> C(N);       // Output vector C
    std::vector<float> D(1, N);

    intptr_t dA = copyData(context, (intptr_t)A.data(), sizeof(float), (size_t)N, queues, 0);
    intptr_t dB = copyData(context, (intptr_t)B.data(), sizeof(float), (size_t)N, queues, 1);
    intptr_t dC = copyData(context, (intptr_t)C.data(), sizeof(float), (size_t)N, queues, 2);
    intptr_t dD = copyData(context, (intptr_t)D.data(), sizeof(int), (size_t)1, queues, 3);

    intptr_t arguments[] = { dA, dB, dC, dD };
    execute(kernel, queues, 0, arguments, 4, 50000000, 10, 1, 100, 10, 1);

    // Step 10: Read back the result
    clEnqueueReadBuffer(((cl_command_queue*)queues)[0], *((cl_mem*) dC), CL_TRUE, 0, sizeof(float) * N, C.data(), 0, NULL, NULL);

    // Step 11: Cleanup
    //clReleaseCommandQueue(queue);

    //clReleaseMemObject(bufferA);
    //clReleaseMemObject(bufferB);
    //clReleaseMemObject(bufferC);

    //clReleaseKernel(*kernel);
    //clReleaseProgram(*program);    
    //clReleaseContext(*context);

    // Output the result
    for (int i = 0; i < 10; i++) {
        std::cout << "mbu" << C[i] << " "; // Print the first 10 results
    }
    std::cout << std::endl;



    //cl_int err = clBuildProgram(program, 1, &device, "-cl-std=CL2.0 -cl-spirv-output", NULL, NULL);
    //if (err != CL_SUCCESS) {
    //    printf("Error building program: %d\n", err);
    //    return 1;
    //}

    // Get SPIR-V binary
    //size_t binarySize;
    //clGetProgramInfo(program, CL_PROGRAM_BINARY_SIZES, sizeof(size_t), &binarySize, NULL);

    //unsigned char* binary = (unsigned char*)malloc(binarySize);
    //clGetProgramInfo(program, CL_PROGRAM_BINARIES, sizeof(unsigned char*), &binary, NULL);

    // Save SPIR-V to a file
    //saveToFile("d:/output.spv", binary, binarySize);
}
