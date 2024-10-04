__kernel void vector_add(__global const float* A, __global const float* B, __global float* C, const int n) {
    int id = get_global_id(0);  // Get the index of the current element

    if (id < n) {
        C[id] = A[id] + B[id];  // Perform element-wise addition
    }
}
