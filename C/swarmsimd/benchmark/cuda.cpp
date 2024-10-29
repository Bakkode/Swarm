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



void printCudaDevices() {
    int deviceCount;
    cudaError_t error = cudaGetDeviceCount(&deviceCount);

    if (error != cudaSuccess) {
        std::cerr << "Error getting device count: " << cudaGetErrorString(error) << std::endl;
        return;
    }

    std::cout << "Number of CUDA devices: " << deviceCount << std::endl;

    for (int i = 0; i < deviceCount; ++i) {
        cudaDeviceProp deviceProp;
        cudaGetDeviceProperties(&deviceProp, i);

        cout << "Device Name" << i << ": " << deviceProp.name << std::endl;
        std::cout << "  Compute Units: " << deviceProp.multiProcessorCount << std::endl;
        std::cout << "  Global Memory Size: " << deviceProp.totalGlobalMem << std::endl;
        
        std::cout << "  Max Work Group Sizes: " << deviceProp.maxThreadsPerBlock << std::endl;
        std::cout << "  Max Work Item Size: ["
            << deviceProp.maxGridSize[0] << ", "
            << deviceProp.maxGridSize[1] << ", "
            << deviceProp.maxGridSize[2] << "]" << std::endl;

        deviceProp.warpSize;

        std::cout << "  Compute capability: " << deviceProp.major << "." << deviceProp.minor << std::endl;
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

int main() {
    printCudaDevices();

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

    // Load PTX code and get kernel function
   
    const char* cudaSource = R"(
        extern "C" __global__ void addKernel(int *a, int *b, int *c, int *n) {
            int i = blockIdx.x * blockDim.x + threadIdx.x;
            if (i < *n) {
                c[i] = a[i] + b[i] + 5;
            }
        }
        )";

    cout << "bububaba" << endl;
    CUmodule module = loadFromPtx(readFromFile("C:\\wsl\\Programming\\swarmcu.ptx").c_str());
    //CUmodule module = loadFromCuSrc(cudaSource);

    CUfunction addKernel;
    cuModuleGetFunction(&addKernel, module, "addKernel");

    // Set up kernel parameters
    void* args[] = { &d_a, &d_b, &d_c, &d_n };

    // Launch the kernel
    int threadsPerBlock = 256;
    int blocksPerGrid = (n + threadsPerBlock - 1) / threadsPerBlock;
    cuLaunchKernel(addKernel,
        blocksPerGrid, 1, 1,       // Grid dimensions
        threadsPerBlock, 1, 1,     // Block dimensions
        0, 0, args, 0);            // Arguments

    // Copy result back to host
    cuMemcpyDtoH(c, d_c, n * sizeof(int));

    // Print the result
    std::cout << "Result: ";
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