#include <cuda_runtime.h>
#include <cuda.h>
#include <nvrtc.h>

#include <iostream>
#include <fstream>
#include <string>
#include <vector>

using namespace std;

void checkCudaError(CUresult result, const char* msg) {
    if (result == CUDA_SUCCESS) {
        return;
    }

    const char* errorStr;
    cuGetErrorString(result, &errorStr);
    std::cerr << msg << " - CUDA error: " << errorStr << std::endl;
    exit(EXIT_FAILURE);
}

void checkNvrtcError(nvrtcResult result, const char* msg) {
    if (result == NVRTC_SUCCESS) {
        return;
    }

    std::cerr << msg << " - NVRTC error: " << nvrtcGetErrorString(result) << std::endl;
    exit(EXIT_FAILURE);
}

string readFromFile(const string& filename) {
    ifstream file(filename);

    if (!file.is_open()) {
        std::cerr << "Failed to open PTX file: " << filename << std::endl;
        exit(EXIT_FAILURE);
    }
    
    string content((istreambuf_iterator<char>(file)), istreambuf_iterator<char>());
    file.close();
    return content;
}

CUmodule loadFromPtx(const char* ptxSource) {
    CUmodule module;
    cuModuleLoadData(&module, ptxSource);

    return module;
}

CUmodule loadFromCuSrc(const char* cudaSource) {
    nvrtcProgram prog;
    checkNvrtcError(nvrtcCreateProgram(&prog, cudaSource, "cuda.cu", 0, nullptr, nullptr), "Program creation failed");

    nvrtcResult compileResult = nvrtcCompileProgram(prog, 0, nullptr);
    if (compileResult != NVRTC_SUCCESS) {
        size_t logSize;
        nvrtcGetProgramLogSize(prog, &logSize);
        vector<char> log(logSize);
        nvrtcGetProgramLog(prog, log.data());
        cerr << "Compilation failed with error log:\n" << log.data() << std::endl;
        exit(EXIT_FAILURE);
    }

    // Obtain PTX code
    size_t ptxSize;
    checkNvrtcError(nvrtcGetPTXSize(prog, &ptxSize), "Failed to get PTX size");

    vector<char> ptx(ptxSize);
    checkNvrtcError(nvrtcGetPTX(prog, ptx.data()), "Failed to get PTX");

    // Cleanup NVRTC program
    nvrtcDestroyProgram(&prog);

    return loadFromPtx(ptx.data());
}



void _printCudaDevices() {
    int deviceCount;
    cudaError_t error = cudaGetDeviceCount(&deviceCount);

    if (error != cudaSuccess) {
        std::cerr << "Error getting device count: " << cudaGetErrorString(error) << std::endl;
        return;
    }

    for (int i = 0; i < deviceCount; ++i) {
        cudaDeviceProp deviceProp;
        cudaGetDeviceProperties(&deviceProp, i);

        std::cout << "Device Type: GPU" << std::endl;
        std::cout << "Device Name: " << deviceProp.name << std::endl;

        std::cout << "Compute Units: "<< deviceProp.multiProcessorCount << std::endl;
        std::cout << "Global Memory Size: " << deviceProp.totalGlobalMem << std::endl;
        
        
        std::cout << "Max Work Item Sizes: ["
            << deviceProp.maxGridSize[0] << ", "
            << deviceProp.maxGridSize[1] << ", "
            << deviceProp.maxGridSize[2] << "]" << std::endl;

        std::cout << "Max Work Group Size: " << deviceProp.maxThreadsPerBlock << std::endl;

        deviceProp.warpSize;

        std::cout << std::endl;      

        int coresPerSM;
        int cudaCores;
        int instructionsPerCycle = 2; // Typically 2 FLOPS per cycle for FP32 on NVIDIA GPUs

        // Determine cores per Streaming Multiprocessor (SM) based on architecture
        if (deviceProp.major == 8) {  // Ampere
            coresPerSM = 128;
        }
        else if (deviceProp.major == 7) {  // Volta and Turing
            coresPerSM = (deviceProp.minor == 5) ? 64 : 128;
        }
        else if (deviceProp.major == 6) {  // Pascal
            coresPerSM = 64;
        }
        else if (deviceProp.major == 5) {  // Maxwell
            coresPerSM = 128;
        }
        else if (deviceProp.major == 3) {  // Kepler
            coresPerSM = 192;
        }
        else {
            coresPerSM = 0;
        }

        // Calculate total CUDA cores
        cudaCores = deviceProp.multiProcessorCount * coresPerSM;

        // Peak FLOPS = CUDA cores * Clock Frequency (in GHz) * Instructions per Cycle
        double peakFLOPS = cudaCores * (deviceProp.clockRate * 1e3) * instructionsPerCycle;

        std::cout << "Estimated Peak FLOPS: " << peakFLOPS / 1e12 << " TFLOPS" << std::endl;
    }
}


void launch() {

}

intptr_t cuGetDevice(int index) {
    CUdevice* device = new CUdevice;
    cuDeviceGet(device, index);

    return (intptr_t)device;
}

intptr_t cuCreateContext(intptr_t device) {
    CUcontext* context = new CUcontext;
    
    CUresult err = cuCtxCreate(context, CU_CTX_SCHED_SPIN, *((CUdevice *)device));
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    return (intptr_t)context;
}

intptr_t cuCreateProgram(intptr_t context, const char* source) {
    cuCtxSetCurrent(*((CUcontext*)context));

    CUmodule* module = new CUmodule;
    *module = loadFromCuSrc(source);

    return (intptr_t)module;
}

intptr_t cuCreateKernel(intptr_t program, const char* kernelName) {
    CUfunction* kernel = new CUfunction;
    CUresult err = cuModuleGetFunction(kernel, *((CUmodule *) program), kernelName);

    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    return (intptr_t)kernel;
}

intptr_t cuCreateQueue(intptr_t context) {
    cuCtxSetCurrent(*((CUcontext*)context));

    CUstream* stream = new CUstream[4];
    CUresult err = cuStreamCreate(&stream[0], CU_STREAM_DEFAULT);
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    err = cuStreamCreate(&stream[1], CU_STREAM_DEFAULT);
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    err = cuStreamCreate(&stream[2], CU_STREAM_DEFAULT);
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    err = cuStreamCreate(&stream[3], CU_STREAM_DEFAULT);
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    return (intptr_t)stream;
}

void cuWait(intptr_t queues) {
    CUstream* stream = (CUstream*)queues;

    cudaStreamSynchronize(stream[0]);
    cudaStreamSynchronize(stream[1]);
    cudaStreamSynchronize(stream[2]);
    cudaStreamSynchronize(stream[3]);
}

intptr_t cuCopyData(intptr_t context, 
                    intptr_t dataSource, size_t dataSize, size_t dataCount, 
                    intptr_t queue, int queueIndex) {

    cuCtxSetCurrent(*((CUcontext*)context));

    CUdeviceptr* buffer = new CUdeviceptr;
    CUresult err = cuMemAlloc(buffer, dataCount * dataSize);
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    err = cuMemcpyHtoDAsync(*buffer, (const void*)dataSource, dataCount * dataSize, ((CUstream*)queue)[queueIndex]);
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return NULL;
    }

    return (intptr_t)buffer;
}

void cuExecute(intptr_t kernel, intptr_t queue, int queueIndex, intptr_t* params,
                const size_t globX, const size_t globY, const size_t globZ,
                const size_t localX, const size_t localY, const size_t localZ) {

    cuWait(queue);
    CUresult err = cuLaunchKernel(
            *((CUfunction*)kernel),
            globX, globY, globZ,       // Grid dimensions
            localX, localY, localZ,     // Block dimensions
            0, 
            ((CUstream*)queue)[queueIndex],
            (void**) params,
            0);

    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
        return;
    }

    cuWait(queue);
}

int main() {
    _printCudaDevices();

    cuInit(0);

    const char* kernelsource = R"(
    extern "C" __global__ void vecAdd(float* a, 
                                      float* b,
                                      float* c, 
                                      int* N) {
        int i = blockIdx.x * blockDim.x + threadIdx.x;

        c[0] = a[0] * b[0] + 2 ;
        
        
    }
    )";

    intptr_t device = cuGetDevice(0);
    intptr_t context = cuCreateContext(device);

    intptr_t program = cuCreateProgram(context, kernelsource);
    intptr_t kernel = cuCreateKernel(program, "vecAdd");

    intptr_t queues = cuCreateQueue(context);

    const long long N = 500000000;
    std::vector<float> A(N, 1.0f); // Initialize A with 1.0
    std::vector<float> B(N, 2.0f); // Initialize B with 2.0
    std::vector<float> C(N);       // Output vector C
    std::vector<int> D(1, N);

    intptr_t dA = cuCopyData(context, (intptr_t)A.data(), sizeof(float), (size_t)N, queues, 0);
    intptr_t dB = cuCopyData(context, (intptr_t)B.data(), sizeof(float), (size_t)N, queues, 1);
    intptr_t dC = cuCopyData(context, (intptr_t)C.data(), sizeof(float), (size_t)N, queues, 2);
    intptr_t dD = cuCopyData(context, (intptr_t)D.data(), sizeof(int), (size_t)1, queues, 3);

    // don't forget to set context
    intptr_t arguments[] = {dA, dB, dC, dD};
    cuExecute(kernel, queues, 0, arguments, 50000000, 1, 1, 100, 10, 1);

 
    // Copy result back to host
    CUresult err = cuMemcpyDtoHAsync_v2(C.data(), *((CUdeviceptr*) dC), N * sizeof(float), ((CUstream*)queues)[0]);
    if (err != CUDA_SUCCESS) {
        const char* errStr;
        cuGetErrorString(err, &errStr);
        std::cerr << "Error: " << errStr << std::endl;
    }

    for (int i = 0; i < 10; i++) {
        std::cout << "x" << C[i] << " "; // Print the first 10 results
    }
    std::cout << std::endl;

    //cudaEventDestroy(event);
    //cudaStreamDestroy(stream);

    //cuMemFree(d_a);
    //cuMemFree(d_b);
    //cuMemFree(d_c);


    //cuModuleUnload(module);
    //cuCtxDestroy(context);

    return 0;
}

int __main() {
    _printCudaDevices();


    const char* cudaSource = R"(

        package boo

        extern "C" __global__ void vecAdd(float* a, 
                                          float* b,
                                          float* c, 
                                          int* n) {
            int i = blockIdx.x * blockDim.x + threadIdx.x;
            if (i < *n) {
                c[i] = a[i] + b[i] + 10;
            }
        }
        )";

    // Initialize CUDA


    cuInit(0);
    CUdevice device;
    cuDeviceGet(&device, 0);
    CUcontext context;
    cuCtxCreate(&context, 0, device);

    // Allocate memory
    const int n = 10;
    int a[n], b[n], c[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i * 2;
    }

    int d[1];
    d[0] = n;

    CUdeviceptr d_a, d_b, d_c, d_n;
    cuMemAlloc(&d_a, n * sizeof(int));
    cuMemAlloc(&d_b, n * sizeof(int));
    cuMemAlloc(&d_c, n * sizeof(int));
    cuMemAlloc(&d_n, sizeof(int));

    // Copy input data to device
    cuMemcpyHtoD(d_a, a, n * sizeof(int));
    cuMemcpyHtoD(d_b, b, n * sizeof(int));
    cuMemcpyHtoD(d_n, d, sizeof(int));

    // Set up kernel parameters
    //void* args[] = { &d_a, &d_b, &d_c, &d_n };

    void** args = (void**)malloc(4 * sizeof(void*));  // Allocate memory for 4 kernel arguments
    if (args == NULL) {
        fprintf(stderr, "Failed to allocate memory for args\n");
        return -1;  // Exit if memory allocation fails
    }

    // Set kernel arguments dynamically using a loop
    CUdeviceptr* devPtrs[] = { &d_a, &d_b, &d_c, &d_n };  // Array of device pointers
    for (int i = 0; i < 4; i++) {
        args[i] = devPtrs[i];  // Set each kernel argument
    }


    // Load PTX code and get kernel function
   
    

    cout << "bububababa" << endl;
    CUmodule module = loadFromPtx(readFromFile("C:\\wsl\\Programming\\swarmcu.ptx").c_str());
    //CUmodule module = loadFromCuSrc(cudaSource);

    CUfunction addKernel;
    cuModuleGetFunction(&addKernel, module, "vecAdd");

    
    // Launch the kernel
    int threadsPerBlock = 256;
    int blocksPerGrid = (n + threadsPerBlock - 1) / threadsPerBlock;

    CUstream stream;
    cuStreamCreate(&stream, CU_STREAM_DEFAULT);

    cudaSetDevice(0);
    cuLaunchKernel(addKernel,
        blocksPerGrid, 1, 1,       // Grid dimensions
        threadsPerBlock, 1, 1,     // Block dimensions
        0, stream, args, 0);            // Arguments

    cudaEvent_t event;
    cudaEventCreate(&event);
    cudaEventRecord(event, stream);

    // Wait for the kernel to complete
    cudaEventSynchronize(event);

    // Copy result back to host
    cuMemcpyDtoH(c, d_c, n * sizeof(int));

    cudaEventDestroy(event);
    cudaStreamDestroy(stream);
 


    // Print the result
    std::cout << "Resulted: ";
    for (int i = 0; i < n; i++) {
        std::cout << c[i] << " ";
    }
    std::cout << std::endl;

    // Cleanup


    cuMemFree(d_a);
    cuMemFree(d_b);
    cuMemFree(d_c);


    cuModuleUnload(module);
    cuCtxDestroy(context);

    return 0;
}

// OpenCL

 //// Vector sizes
    //const int N = 1024;
    //std::vector<float> A(N, 1.0f); // Initialize A with 1.0
    //std::vector<float> B(N, 2.0f); // Initialize B with 2.0
    //std::vector<float> C(N);       // Output vector C

    //// Step 1: Get platform and device information
    //cl_platform_id platform;
    //clGetPlatformIDs(1, &platform, NULL);

    //cl_device_id device;
    //clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 1, &device, NULL);

    //// Step 2: Create an OpenCL context
    //cl_context context = clCreateContext(NULL, 1, &device, NULL, NULL, NULL);

    //// Step 3: Create a command queue
    //cl_command_queue queue = clCreateCommandQueue(context, device, 0, NULL);

    //// Step 4: Create memory buffers
    //cl_mem bufferA = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
    //    sizeof(float) * N, A.data(), NULL);
    //cl_mem bufferB = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
    //    sizeof(float) * N, B.data(), NULL);
    //cl_mem bufferC = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
    //    sizeof(float) * N, NULL, NULL);

    //// Step 5: Create a program from the kernel source
    //cl_program program = clCreateProgramWithSource(context, 1,
    //    &kernelSource, NULL, NULL);

    //// Step 6: Build the program
    //clBuildProgram(program, 1, &device, NULL, NULL, NULL);

    //// Step 7: Create the OpenCL kernel
    //cl_kernel kernel = clCreateKernel(program, "vecAdd", NULL);

    //// Step 8: Set kernel arguments
    //clSetKernelArg(kernel, 0, sizeof(cl_mem), &bufferA);
    //clSetKernelArg(kernel, 1, sizeof(cl_mem), &bufferB);
    //clSetKernelArg(kernel, 2, sizeof(cl_mem), &bufferC);
    //clSetKernelArg(kernel, 3, sizeof(unsigned int), &N);

    //// Step 9: Execute the kernel
    //size_t globalSize = N; // Total number of threads
    //clEnqueueNDRangeKernel(queue, kernel, 1, NULL, &globalSize, NULL, 0, NULL, NULL);

    //// Step 10: Read back the result
    //clEnqueueReadBuffer(queue, bufferC, CL_TRUE, 0, sizeof(float) * N, C.data(), 0, NULL, NULL);

    //// Step 11: Cleanup
    //clReleaseMemObject(bufferA);
    //clReleaseMemObject(bufferB);
    //clReleaseMemObject(bufferC);
    //clReleaseProgram(program);
    //clReleaseKernel(kernel);
    //clReleaseCommandQueue(queue);
    //clReleaseContext(context);

    //// Output the result
    //for (int i = 0; i < 10; i++) {
    //    std::cout << C[i] << " "; // Print the first 10 results
    //}
    //std::cout << std::endl;