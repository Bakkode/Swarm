extern "C" {
#include <stdio.h>

#include <time.h>
    
#include "../header/vector.h"
#include "../header/avx/avx.h"
#include <immintrin.h>

int main(void)
{

    SimdVector* vectorPtr = createVectorFp32256(4, 40, 0.4);

    clock_t start = clock();
    for (int i = 0; i < 1000000; i++) {
        int ii = i * 10;

        appendFp32256(vectorPtr, ii + 0, ii + 1, ii + 2, ii + 3, ii + 4, ii + 5, ii + 6, ii + 7);
    }

    SimdVector* scalar1Ptr = createScalarFp32256(8);
    SimdVector* scalar2Ptr = createScalarFp32256(2);

    // Print the result
    double time = (double)(clock() - start) / CLOCKS_PER_SEC;
    printf("Elapsed time: %f seconds for %i insertion. Showing first 45 data", time, 7000000);


   /* for (int i = 0; i < 45; i++) {
        if (i % 8 == 0) {
            printf("\nValue is:");
        }

        printf("%f, ", getFp32256(vectorPtr, i));
    }

    printf("\n-----------\n");*/



    clock_t start2 = clock();



    SimdVector* scalar3 = deriveVectorFp32256(scalar1Ptr);
    addScalarFp32256(scalar3, scalar1Ptr, scalar2Ptr);

    SimdVector* vector5Ptr = deriveVectorFp32256(vectorPtr);
    for (int i = 0; i <= vectorPtr->threadIndex.totalThreads; i++) {
        addVectorFp32256(vector5Ptr, i, vectorPtr, vectorPtr);
    }

    SimdVector* result = deriveVectorFp32256(vector5Ptr);
    for (int i = 0; i <= vector5Ptr->threadIndex.totalThreads; i++) {
        addMixedFp32256(result, i, vector5Ptr, scalar3);
    }

    printf("\nElapsed time: %f seconds for addition. Showing first 45 data", (double)(clock() - start2) / CLOCKS_PER_SEC);

    float values[8];
    __m256 simd = ((__m256*)(scalar3->threadList[0]))[0];
    _mm256_storeu_ps(values, simd);

    /*for (int i = 0; i < 45; i++) {
        if (i % 8 == 0) {
            printf("\nValue is:");
        }

        printf("%f, ", getFp32256(result, i));
    }*/

    destroyFp32256(vectorPtr);

    int num;
    scanf("%d", &num);
    return 0;
}

}