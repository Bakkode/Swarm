#ifdef USE_CUDA

#include <stdint.h>
#include <cuda_fp16.h>


// ---===== data type =====---
#define int8 int8_t
#define int16 int16_t
#define int32 int32_t
#define int64 int64_t

#define uint8 uint8_t
#define uint16 uint16_t
#define uint32 uint32_t
#define uint64 uint64_t

#define f16 __half
#define f32 float
#define f64 double
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

#endif