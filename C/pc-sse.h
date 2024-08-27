#ifdef USE_SSE

#include <stdint.h>
#include <immintrin.h>

// ---===== Container Size in 32 bit =====---
#define cSize 4

// ---===== data type =====---
#define int8 int8_t
#define int16 int16_t
#define int32 int32_t
#define int64 int64_t

#define f16 float
#define f32 float
#define f64 double

#define vf32 __m256
#define vf64 __m256d
#define vi16 __m256i 
#define vi32 __m256i 
#define vi64 __m256i 
// ---===== ===== =====---

// ---===== Vector Initializer =====---
ofInt8
ofInt16
ofInt32
ofInt64

off16
off32
off64
// ---===== ===== =====---

// ---===== Indexer =====---
#define index(axis, length) \
    for (int axis = threadIdx.axis + blockIdx.axis * blockDim.axis; \
         axis < length; \
         axis += blockDim.axis * gridDim.axis)

// ---===== Kernel =====---
#define __main__ 
#define __kernel__ 
#define __any__ 


// ---===== Function =====---
#define power powf

// ---===== Statement =====---
#define pfor(expression) for(exprssion)

#endif