#include <cuda_runtime.h>
#include <cuda.h>
#include <iostream>
#include <fstream>
#include <string>

using namespace std;

// Function to read PTX code from a file
std::string readPTXFile(const std::string& filename) {
    std::ifstream file(filename);
    if (!file.is_open()) {
        std::cerr << "Failed to open PTX file: " << filename << std::endl;
        exit(EXIT_FAILURE);
    }
    std::string ptxCode((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
    file.close();
    return ptxCode;
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

        cout << "Device " << i << ": " << deviceProp.name << std::endl;
        std::cout << "  Total global memory: " << deviceProp.totalGlobalMem << " bytes" << std::endl;
        std::cout << "  Multiprocessor count: " << deviceProp.multiProcessorCount << std::endl;
        std::cout << "  Max threads per block: " << deviceProp.maxThreadsPerBlock << std::endl;
        std::cout << "  Max grid size: ["
            << deviceProp.maxGridSize[0] << ", "
            << deviceProp.maxGridSize[1] << ", "
            << deviceProp.maxGridSize[2] << "]" << std::endl;
        std::cout << "  Compute capability: " << deviceProp.major << "." << deviceProp.minor << std::endl;
        std::cout << std::endl;
    }
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

    // Read PTX code from file
    std::string ptxCode = readPTXFile("C:\\wsl\\Programming\\swarmcu.ptx");

    // Load PTX code and get kernel function
    CUmodule module;
    cuModuleLoadData(&module, ptxCode.c_str());
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