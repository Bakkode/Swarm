

// Main algorithm is written here
// This source code should be compiled into C Shared Library (.so) and / or Nvidia-CUDA PTX 

// Author: Septian P.R.
/**
    -----======= Guidelines =======-----

    Ref: https://www.nvidia.com/docs/io/116711/sc11-cuda-c-basics.pdf

    Not to be confused:
    1. block: Primary execution node
        gridDim.[x | y | z]  : Represent total block in x, y, or z axes
        blockIdx.[x | y | z] : Represent current execution index in range 0 to gridDim - 1 (inclusive)

    2. thread: Secondary execution node. Thread is based on block
        blockDim.[x | y | z]  : Represent total thread within block in x, y, or z axes
        threadIdx.[x | y | z] : Represent current execution index in range 0 to blockDim - 1(inclusive)

    3. __global__ or __gpu__ will execute all calculation in total of gridDim * blockDim 
        all at once with the difference in blockIdx and threadIdx. They will use exactly the
        same data parameter (usually an array). We can get it's data by index.
        'index = threadIdx.[x | y | z] + blockIdx.[x | y | z] * blockDim.[x | y | z]'

    4. __global__ or __gpu__ has no return types, if you need return data, you can utilize array and set
        it as an parameter

    5. Entry point:
        GPU entry point defined as __global__
        CPU entry point defined as __host__
        GPU-Callable defined as __device__

    6. CUDA support memory sharing between thread within the same block. But to reduce complexity,
        we don't utilize that here
*/


// The aim of this source code is to allow multiple compilation to CUDA, C, or C++
// if not compiled to CUDA, this wrapper is to prevent compilation error.
#ifdef USE_CUDA
#define power powf
#define __cpu__ __host__
#define __gpu__ __global__
#define __cpu_gpu__ __device__ __host__
#define execIdxX threadIdx.x + blockIdx.x * blockDim.x
#define execIdxY threadIdx.y + blockIdx.y * blockDim.y
#define execIdxZ threadIdx.z + blockIdx.z * blockDim.z
#define stridesX blockDim.x * gridDim.x

#define index(axis, length) \
    for (int axis = threadIdx.axis + blockIdx.axis * blockDim.axis; \
         axis < length; \
         axis += blockDim.axis * gridDim.axis)


#else
#include <math.h>
#define power pow
#define __cpu__
#define __gpu__ 
#define __cpu_gpu__ 
#define execIdxX 0
#define execIdxY 0
#define execIdxZ 0
#define stridesX 0
#define index(axis, length) for(int axis = 0; axis < length; axis++)

#endif

#define ptr *
#define valueOf *
#define addressOf &

// ========================================================================

#ifdef USE_CUDA
extern "C" {
#endif


__gpu__
void expr(int length, float* source, float* out) {
    index(x, length) {
         out[x] = threadIdx.x; //source[x] * 4;
    }
}

__gpu__
void distance(float* result, int length, float* fromPoint, float* toPoint) {
    index(x, length) {
        int xIndex = x * 2;
        int yIndex = xIndex + 1; 

        float tmp = power(fromPoint[xIndex] - toPoint[yIndex], 2) +
                    power(fromPoint[xIndex] - toPoint[yIndex], 2);

        result[x] = power(tmp, 0.5);  
    }
}

__gpu__
void intersect(float* result, int length, float* fromPoint1, float* toPoint1, float* fromPoint2, float* toPoint2) {
    index(x, length) {
 
        int xIndex = x * 2;
        int yIndex = xIndex + 1;

        float x1 = toPoint1[xIndex];
        float x2 = toPoint2[xIndex];
        float y1 = toPoint1[yIndex];
        float y2 = toPoint2[yIndex];

        float dx1 = fromPoint1[xIndex] - x1;
        float dx2 = fromPoint2[xIndex] - x2;
        float dy1 = fromPoint1[yIndex] - y1;
        float dy2 = fromPoint2[yIndex] - y2;

        float zeroing1 = (dx1 != 0.0);
        float zeroing2 = (dx2 != 0.0);

        float gr1 = dy1 / dx1;
        float gr2 = dy2 / dx2;      

        float c1  = zeroing1 * (y1 - (x1 * gr1));
        float c2  = zeroing2 * (y2 - (x2 * gr2));

        if (dx1 * dx2 == 0.0) {
            result[xIndex] = (x1 * zeroing1) + (x2 * zeroing2);
            result[yIndex] = result[xIndex] * (gr1 + gr2) + c1 + c2;
            return;
        }

        // +8
        float dv = gr2 - gr1;
        result[xIndex] = (c1 - c2) / dv;
        result[yIndex] = ((c1 * gr2) - (c2 * gr1)) / dv;
    }
}

__gpu__
void normalize(float* result, int length, float* source) {
    index(x, length) {
        int biggestNum = 1;

        for (int i = 0; i < length; i++) {
            if (source[i] > biggestNum) {
                biggestNum = source[i];
            }
        }

        result[x] = source[x] / biggestNum;
    }
}

#ifdef USE_CUDA
}
#endif