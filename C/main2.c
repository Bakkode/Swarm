
#include <stdio.h>
#include <stdlib.h>

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



typedef struct {
    int size;
    int16* container;
} vector_int16;

typedef struct {
    int size;
    int32* container;
} vector_int32;

typedef struct {
    int size;
    int64* container;
} vector_int64;

typedef struct {
    int size;
    f16* container;
} vector_f16;

typedef struct {
    int size;
    f32* container;
} vector_f32;

typedef struct {
    int size;
    f64* container;
} vector_f64;


#define v256_i8 int8[32]
// ---===== ===== =====---

// ---===== Vector Processor =====---

// From scalar
#define vec_extract_int8(ref, index) ref.container[index]
#define vec_assign_int8(target, val ...)                        \
    {                                                           \
        int8 array[]        = val;                              \
        int length          = sizeof(array) / sizeof(array[0]); \
        target.container    = array;                            \
        target.size         = length;                           \
    }


#define vec_extract_int16(ref, index) ref.container[index]
#define vec_assign_int16(target, val ...)                       \
    {                                                           \
        int16 array[]       = val;                              \
        int length          = sizeof(array) / sizeof(array[0]); \
        target.container    = array;                            \
        target.size         = length;                           \
    }

#define vec_extract_int32(ref, index) ref.container[index]
#define vec_assign_int32(target, val ...)                       \
    {                                                           \
        int32 array[]       = val;                              \
        int length          = sizeof(array) / sizeof(array[0]); \
        target.container    = array;                            \
        target.size         = length;                           \
    }

#define vec_extract_int64(ref, index) ref.container[index]
#define vec_assign_int64(target, val ...)                       \
    {                                                           \
        int64 array[]       = val;                              \
        int length          = sizeof(array) / sizeof(array[0]); \
        target.container    = array;                            \
        target.size         = length;                           \
    }

#define vec_extract_f16(ref, index) ref.container[index]
#define vec_assign_f16(target, val ...)                         \
    {                                                           \
        f16 array[]         = val;                              \
        int length          = sizeof(array) / sizeof(array[0]); \
        target.container    = array;                            \
        target.size         = length;                           \
    }

#define vec_extract_f32(ref, index) ref.container[index]
#define vec_assign_f32(target, val ...)                         \
    {                                                           \
        f32 array[]         = val;                              \
        int length          = sizeof(array) / sizeof(array[0]); \
        target.container    = array;                            \
        target.size         = length;                           \
    }

#define vec_extract_f64(ref, index) ref.container[index]
#define vec_assign_f64(target, val ...)                         \
    {                                                           \
        f64 array[]         = val;                              \
        int length          = sizeof(array) / sizeof(array[0]); \
        target.container    = array;                            \
        target.size         = length;                           \
    }

// From Array
#define of_int8_arr(x) x
#define of_int16_arr(x) x
#define of_int32_arr(x) x
#define of_int64_arr(x) x

#define of_f16(x) {x}
#define of_f32(x) {x}
#define of_f64(x) {x}

// To scalar
#define to_int8(v, i) v[i]
#define to_int16(v, i) v[i]
#define to_int32(v, i) v[i]
#define to_int64(v, i) v[i]

#define to_f16(v, i) v[i]
#define to_f32(v, i) v[i]
#define to_f64(v, i) v[i]

// To Array
#define to_int8_arr(v) v
#define to_int16_arr(v) v
#define to_int32_arr(v) v
#define to_int64_arr(v) v

#define to_f16_arr(v) v
#define to_f32_arr(v) v
#define to_f64_arr(v) v

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

#include <time.h>
#include <stdlib.h>






// Vector Data Type
#define vInt8
#define vInt16
#define vInt32
#define vInt64
#define vfp16
#define vfp32
#define vfp64

//Vector converter
#define of_int8
#define of_int16
#define of_int32
#define of_int64
#define of_vfp16
#define of_vfp32
#define of_vfp64

// Boolean operation
#define cmp_gt
#define cmp_gte
#define cmp_lt
#define cmp_lte
#define cmp_eq
#define cmp_neq

#define bop_not
#define bop_and
#define bop_xor
#define bop_or

// Conditional assignment. Branch divergence is not possible in SIMD
#define blend(condition, onTrue, onFalse) _mm256_blendv_ps(onTrue, onFalse, condition)



// Branching comparation


// Kernel parameter
/**
    // The main idea is to group data to calculate within the same index
    // Cross index is allowed with additional overhead.
    // Primitive scalar variable is not allowed

    //Multiply every mainOperand with all slaveOperand

*/




typedef struct{
    void* containerPtr;

    /*
    * 0 for boolean
    * 1 for int8
    * 2 for int16
    * 3 for int32
    * 4 for int64
    * 5 for fp16
    * 6 for fp32
    * 7 for fp64
    */
    unsigned int dataType: 3;

     /*
    * 0 for Array
    * 1 for SSE128
    * 2 for AVX256
    * 3 for AVX512
    */
    unsigned int vectorType: 2
} Val;

// this struct should accept Array, SSE128, AVX256, and AVX512



Val of_fp64(double value) {
    Val ret = {(__m256*) _aligned_malloc(256, 32), 6, 2};
    __m256d rr = _mm256_set1_pd(value);

    *((__m256d*)ret.containerPtr) = rr;
    return ret;
}

Val of_fp32_arr(float a, float b, float c, float d, float e, float f, float g, float h) {
    float* arr1 = (float*) _aligned_malloc(256, 32);
    arr1[0] = a;
    arr1[1] = b;
    arr1[2] = c;
    arr1[3] = d;
    arr1[4] = e;
    arr1[5] = f;
    arr1[6] = g;
    arr1[7] = h;

    Val v;
    return v;
//    return of_fp32_ptr(arr1);
}

Val of_fp32(float value) {
    Val ret = {(__m256*) _aligned_malloc(256, 32), 6, 2};
    *((__m256*)ret.containerPtr) = _mm256_set1_ps(value);
    return ret;
}

Val of_fp32_ptr(float* arr) {
    Val ret = {(__m256*) _aligned_malloc(256, 32), 6, 2};
    *((__m256*)ret.containerPtr) = _mm256_load_ps(arr);
    return ret;
}


/**
void sample (Val d1, Val d2, Val d3, Val* ret) {
    Val r1 = math_mul(d1, d2);
    Val r2 = math_add(r1, d3);

    Val r3 = blend(cmp_gt(r2, of_fp32(12.5)), of_fp32(10))

    *ret = math_div(r1, r2);
}
*/

static inline uint64_t rdtsc() {
    unsigned int lo, hi;
    __asm__ __volatile__ ("rdtsc" : "=a" (lo), "=d" (hi));
    return ((uint64_t)hi << 32) | lo;
}

#include <cpuid.h>
#ifdef _WIN32

#define cpuid(info, x) __cpuidex(info, x, 0)

#else

//  GCC Intrinsics
void cpuid(int info[4], int InfoType){
    __cpuid_count(InfoType, 0, info[0], info[1], info[2], info[3]);
}

#endif

#define bool int
void support() {
    //  Misc.
    bool HW_MMX;
    bool HW_x64;
    bool HW_ABM;      // Advanced Bit Manipulation
    bool HW_RDRAND;
    bool HW_BMI1;
    bool HW_BMI2;
    bool HW_ADX;
    bool HW_PREFETCHWT1;

    //  SIMD: 128-bit
    bool HW_SSE;
    bool HW_SSE2;
    bool HW_SSE3;
    bool HW_SSSE3;
    bool HW_SSE41;
    bool HW_SSE42;
    bool HW_SSE4a;
    bool HW_AES;
    bool HW_SHA;

    //  SIMD: 256-bit
    bool HW_AVX;
    bool HW_XOP;
    bool HW_FMA3;
    bool HW_FMA4;
    bool HW_AVX2;

    //  SIMD: 512-bit
    bool HW_AVX512F;    //  AVX512 Foundation
    bool HW_AVX512CD;   //  AVX512 Conflict Detection
    bool HW_AVX512PF;   //  AVX512 Prefetch
    bool HW_AVX512ER;   //  AVX512 Exponential + Reciprocal
    bool HW_AVX512VL;   //  AVX512 Vector Length Extensions
    bool HW_AVX512BW;   //  AVX512 Byte + Word
    bool HW_AVX512DQ;   //  AVX512 Doubleword + Quadword
    bool HW_AVX512IFMA; //  AVX512 Integer 52-bit Fused Multiply-Add
    bool HW_AVX512VBMI; //  AVX512 Vector Byte Manipulation Instructions

    int info[4];
    cpuid(info, 0);
    int nIds = info[0];

    cpuid(info, 0x80000000);
    unsigned nExIds = info[0];

    //  Detect Features
    if (nIds >= 0x00000001){
        cpuid(info,0x00000001);
        HW_MMX    = (info[3] & ((int)1 << 23)) != 0;
        HW_SSE    = (info[3] & ((int)1 << 25)) != 0;
        HW_SSE2   = (info[3] & ((int)1 << 26)) != 0;
        HW_SSE3   = (info[2] & ((int)1 <<  0)) != 0;

        HW_SSSE3  = (info[2] & ((int)1 <<  9)) != 0;
        HW_SSE41  = (info[2] & ((int)1 << 19)) != 0;
        HW_SSE42  = (info[2] & ((int)1 << 20)) != 0;
        HW_AES    = (info[2] & ((int)1 << 25)) != 0;

        HW_AVX    = (info[2] & ((int)1 << 28)) != 0;
        HW_FMA3   = (info[2] & ((int)1 << 12)) != 0;

        HW_RDRAND = (info[2] & ((int)1 << 30)) != 0;
    }
    if (nIds >= 0x00000007){
        cpuid(info,0x00000007);
        HW_AVX2   = (info[1] & ((int)1 <<  5)) != 0;

        HW_BMI1        = (info[1] & ((int)1 <<  3)) != 0;
        HW_BMI2        = (info[1] & ((int)1 <<  8)) != 0;
        HW_ADX         = (info[1] & ((int)1 << 19)) != 0;
        HW_SHA         = (info[1] & ((int)1 << 29)) != 0;
        HW_PREFETCHWT1 = (info[2] & ((int)1 <<  0)) != 0;

        HW_AVX512F     = (info[1] & ((int)1 << 16)) != 0;
        HW_AVX512CD    = (info[1] & ((int)1 << 28)) != 0;
        HW_AVX512PF    = (info[1] & ((int)1 << 26)) != 0;
        HW_AVX512ER    = (info[1] & ((int)1 << 27)) != 0;
        HW_AVX512VL    = (info[1] & ((int)1 << 31)) != 0;
        HW_AVX512BW    = (info[1] & ((int)1 << 30)) != 0;
        HW_AVX512DQ    = (info[1] & ((int)1 << 17)) != 0;
        HW_AVX512IFMA  = (info[1] & ((int)1 << 21)) != 0;
        HW_AVX512VBMI  = (info[2] & ((int)1 <<  1)) != 0;
    }
    if (nExIds >= 0x80000001){
        cpuid(info,0x80000001);
        HW_x64   = (info[3] & ((int)1 << 29)) != 0;
        HW_ABM   = (info[2] & ((int)1 <<  5)) != 0;
        HW_SSE4a = (info[2] & ((int)1 <<  6)) != 0;
        HW_FMA4  = (info[2] & ((int)1 << 16)) != 0;
        HW_XOP   = (info[2] & ((int)1 << 11)) != 0;
    }

    printf("%i \n %i \n %i \n %i \n %i \n", HW_AVX512F, HW_AVX2, HW_AVX, HW_SSE42,HW_SSE);
}

int supports_avx2() {
    unsigned int eax, ebx, ecx, edx;
    __get_cpuid(7, &eax, &ebx, &ecx, &edx);
    return (ebx & (1 << 5)) != 0;  // Check if AVX2 bit is set in EBX register
}

int supports_avx512() {
    unsigned int eax, ebx, ecx, edx;
    __get_cpuid(0x80000001, &eax, &ebx, &ecx, &edx);
    return (ecx & (1 << 16)) != 0;  // Check if AVX512F bit is set in ECX register
}

int main()
{
    int a = 5, b = 3;
    int result = 0;

    // Inline assembly for comparison and branching
        __asm__ (
            "cmp %[val1], %[val2] \n"   // Compare a and b
            "je equal \n"               // Jump to 'equal' if a == b
            "movl $0, %[res] \n"        // If not equal, set result = 0
            "jmp end \n"                // Jump to end

            "equal: \n"
            "movl $1, %[res] \n"        // If equal, set result = 1

            "end: \n"
            : [res] "=r" (result)       // Output operand
            : [val1] "r" (a), [val2] "r" (b)  // Input operands
            : "cc"                      // Clobbers (flags register)
        );

        if (result == 1) {
            printf("Numbers are equal!\n");
        } else {
            printf("Numbers are not equal!\n");
        }


    support();
    printf("AVX is: %i \n", supports_avx2());
    printf("AVX512 is: %i \n", supports_avx512());

    uint64_t start = rdtsc();
    float* arr1 = (float*) _aligned_malloc(8 * sizeof(float),32);
    arr1[0] = 5732.45f;
    arr1[1] = 8231.88f;
    arr1[2] = 19284.56f;
    arr1[3] = 47291.12f;
    arr1[4] = 29184.33f;
    arr1[5] = 10824.99f;
    arr1[6] = 5032.11f;
    arr1[7] = 9832.77f;

    Val v = of_fp32_ptr(arr1);

    Val v2= of_fp32(192.41);

    // Extract and print the contents
    float* f0 = (float*)(v.containerPtr);
    _mm256_store_ps(f0, *(__m256*)(v2.containerPtr));

    uint64_t end = rdtsc();

    printf("Cycles taken: %llu\n", end - start);
    printf("Value is: %f \n", f0[2]);

     __m256 vec = _mm256_set_ps(8.0f, 7.0f, 6.0f, 5.0f, 4.0f, 3.0f, 2.0f, 1.0f);

    // Scalar value to multiply with
    float scalar = 3.0f;

    // Broadcast the scalar value to all elements of an __m256 vector
    __m256 scalar_vec = _mm256_set1_ps(scalar);

    // Multiply each element of 'vec' by 'scalar_vec'
//    __m256 result = _mm256_mul_ps(vec, scalar_vec);

    float f1[8];
    //_mm256_storeu_ps(f1, result);

    printf("Value is: %f,%f,%f,%f,%f,%f,%f,%f", f1[0], f1[1], f1[2], f1[3], f1[4], f1[5], f1[6], f1[7]);


    return 0;
}
