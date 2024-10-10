extern "C" {
#include "../../header/simd.h"
#include "../../header/avx/arthmavx.h"
#include "../../header/thread.h"

    static void __addV(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2) {
        long long int indexMax = vector1->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_add_ps(
                ((__m256*)vector1->threadList[threadIndex])[ii],
                ((__m256*)vector2->threadList[threadIndex])[ii]
            );
        }
    }

    static void __addM(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar) {
        long long int indexMax = vector->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_add_ps(
                ((__m256*)vector->threadList[threadIndex])[ii],
                ((__m256*)scalar->threadList[0])[0]
            );
        }
    }

    void addScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2) {
        waitAll();

        ((__m256*)(result->threadList[0]))[0] = _mm256_add_ps(
            ((__m256*)scalar1->threadList[0])[0],
            ((__m256*)scalar2->threadList[0])[0]
        );
    }

    void addVectorFp32256(SimdVector* result, SimdVector* vector1, SimdVector* vector2) {
        enqueue(__addV, result, vector1, vector2, NULL);
    }

    void addMixedFp32256(SimdVector* result, SimdVector* vector, SimdVector* scalar) {
        enqueue(__addM, result, vector, scalar, NULL);
    }


    static void __subV(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2) {
        long long int indexMax = vector1->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_sub_ps(
                ((__m256*)vector1->threadList[threadIndex])[ii],
                ((__m256*)vector2->threadList[threadIndex])[ii]
            );
        }
    }

    static void __subM(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar) {
        long long int indexMax = vector->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_sub_ps(
                ((__m256*)vector->threadList[threadIndex])[ii],
                ((__m256*)scalar->threadList[0])[0]
            );
        }
    }
        
    void subScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2) {
        waitAll();

        ((__m256*)(result->threadList[0]))[0] = _mm256_sub_ps(
            ((__m256*)scalar1->threadList[0])[0],
            ((__m256*)scalar2->threadList[0])[0]
        );
    }

    void subVectorFp32256(SimdVector* result, SimdVector* vector1, SimdVector* vector2) {
        enqueue(__subV, result, vector1, vector2, NULL);
    }

    void subMixedFp32256(SimdVector* result, SimdVector* vector, SimdVector* scalar) {
        enqueue(__subM, result, vector, scalar, NULL);
    }


    static void __mulV(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2) {
        long long int indexMax = vector1->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_mul_ps(
                ((__m256*)vector1->threadList[threadIndex])[ii],
                ((__m256*)vector2->threadList[threadIndex])[ii]
            );
        }
    }

    static void __mulM(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar) {
        long long int indexMax = vector->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_mul_ps(
                ((__m256*)vector->threadList[threadIndex])[ii],
                ((__m256*)scalar->threadList[0])[0]
            );
        }
    }

    void mulScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2) {
        waitAll();

        ((__m256*)(result->threadList[0]))[0] = _mm256_mul_ps(
            ((__m256*)scalar1->threadList[0])[0],
            ((__m256*)scalar2->threadList[0])[0]
        );
    }

    void mulVectorFp32256(SimdVector* result, SimdVector* vector1, SimdVector* vector2) {
        enqueue(__mulV, result, vector1, vector2, NULL);
    }

    void mulMixedFp32256(SimdVector* result, SimdVector* vector, SimdVector* scalar) {
        enqueue(__mulM, result, vector, scalar, NULL);
    }


    static void __divV(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2) {

        long long int indexMax = vector1->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_div_ps(
                ((__m256*)vector1->threadList[threadIndex])[ii],
                ((__m256*)vector2->threadList[threadIndex])[ii]
            );
        }
    }

    static void __divM(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar) {

        long long int indexMax = vector->indexCtr[threadIndex];
        for (long long int ii = 0; ii <= indexMax; ii++) {
            ((__m256*)result->threadList[threadIndex])[ii] = _mm256_div_ps(
                ((__m256*)vector->threadList[threadIndex])[ii],
                ((__m256*)scalar->threadList[0])[0]
            );
        }
    }

    void divScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2) {
        waitAll();

        ((__m256*)(result->threadList[0]))[0] = _mm256_div_ps(
            ((__m256*)scalar1->threadList[0])[0],
            ((__m256*)scalar2->threadList[0])[0]
        );
    }

    void divVectorFp32256(SimdVector* result, SimdVector* vector1, SimdVector* vector2) {
        enqueue(__divV, result, vector1, vector2, NULL);
    } 

    void divMixedFp32256(SimdVector* result, SimdVector* vector, SimdVector* scalar) {
        enqueue(__divM, result, vector, scalar, NULL);
    }

}