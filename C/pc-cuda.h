#ifdef USE_CUDA

#include <stdint.h>
#include <cuda_fp16.h>

// ---===== Container Size in 32 bit =====---
#define cSize 1

// ---===== data type =====---
#define int8 int8_t
#define int16 int16_t
#define int32 int32_t
#define int64 int64_t

#define f16 __half
#define f32 float
#define f64 double

#define vf32 float
#define vf64 double
#define vi16 int16_t 
#define vi32 int32_t 
#define vi64 int64_t 
// ---===== ===== =====---

// ---===== Vector Initializer =====---

// ---===== ===== =====---

// ---===== Indexer =====---
#define index(axis, length) \
    for (int axis = threadIdx.axis + blockIdx.axis * blockDim.axis; \
         axis < length; \
         axis += blockDim.axis * gridDim.axis)

// ---===== Kernel =====---
#define __main__ __host__
#define __kernel__ __global__
#define __any__ __device__ __host__


// ---===== Function =====---
#define power powf

// ---===== Statement =====---
#define pfor(expression) for(exprssion)

#define pif(expression) \
    

#endif