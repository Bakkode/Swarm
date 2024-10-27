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

int main() {
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
