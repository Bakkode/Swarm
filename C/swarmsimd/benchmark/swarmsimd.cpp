extern "C" {
#include <stdio.h>

#include <time.h>
    
#include "../header/vector.h"
#include "../header/avx/avx.h"
#include <immintrin.h>

int main(void)
{
    // Define X, Y, Z
    int max = 5000000;

    clock_t initStart = clock();
    SimdVector* constThree = createScalarFp32256(3);
    SimdVector* constFour = createScalarFp32256(4);
    SimdVector* constFive = createScalarFp32256(5);
    SimdVector* constSix = createScalarFp32256(6);
    SimdVector* constSeven = createScalarFp32256(7);
    SimdVector* constEight = createScalarFp32256(8);

    SimdVector* constPi = createScalarFp32256(3.14159);
    SimdVector* constE = createScalarFp32256(2.71828);

    SimdVector* vectorX = createVectorFp32256(4, 500, 0.3f);
    for (int i = 1; i <= max; i++) {
        float ii = (float)(1000.0 / (double)i);

        appendFp32256(vectorX, ii, ii + 1.0f, ii + 2.0f, ii + 3.0f, ii + 4.0f, ii + 5.0f, ii + 6.0f, ii + 7.0f);
    }

    printf("\n~Initialization~ of %i data. Elapsed time: %f seconds", max, (double)(clock() - initStart) / CLOCKS_PER_SEC);

    printf("\n\n==== === ====\n\n");

    clock_t initComputation = clock();

    // (((3 * x * y) - (5 * x) + (7 * y)) / ((4 * x) - (6 * y)) + (((e * x) - (pi * y) + 8) / 3)

    //(x * y)
    for (int loop = 0; loop < 100; loop++) {
        SimdVector* group3XY = deriveVectorFp32256(vectorX);
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulVectorFp32256(group3XY, 0, vectorX, vectorX); // *
        }

        // (3 * x * y)
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulMixedFp32256(group3XY, i, group3XY, constThree); // *
        }

        // (5 * x)
        SimdVector* group5x = deriveVectorFp32256(vectorX);
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulMixedFp32256(group5x, i, vectorX, constFive); // *
        }

        // (7 * y)
        SimdVector* group7y = deriveVectorFp32256(vectorX);
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulMixedFp32256(group7y, i, vectorX, constSeven); // *
        }

        // - (5 * x)
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            subVectorFp32256(group3XY, i, group3XY, group5x); // -
        }

        // + (7 * y)
        //This
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            addVectorFp32256(group3XY, i, group3XY, group7y); // +
        }

        // All Above is ok
        // 
        //((4 * x) - (6 * y)) + (((e * x) - (pi * y) + 8) / 3)

        // (6 * y)
        SimdVector* group6y = deriveVectorFp32256(vectorX);
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulMixedFp32256(group6y, i, vectorX, constSix);
        }

        // (4 * x)
        SimdVector* group4x = deriveVectorFp32256(vectorX);
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulMixedFp32256(group4x, i, vectorX, constFour);
        }

        // This
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            subVectorFp32256(group4x, i, group4x, group6y);
        }

        SimdVector* groupEx = deriveVectorFp32256(vectorX);
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulMixedFp32256(groupEx, i, vectorX, constE);
        }

        SimdVector* groupPIy = deriveVectorFp32256(vectorX);
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            mulMixedFp32256(groupPIy, i, vectorX, constPi);
        }

        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            subVectorFp32256(groupEx, i, groupEx, groupPIy);
        }

        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            addMixedFp32256(groupEx, i, groupEx, constEight);
        }

        // This
        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            divMixedFp32256(groupEx, i, groupEx, constThree);
        }

        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            addVectorFp32256(group4x, i, group4x, groupEx);
        }

        for (int i = 0; i <= vectorX->threadIndex.totalThreads; i++) {
            divVectorFp32256(vectorX, i, group3XY, group4x);
        }

        destroyFp32256(groupPIy);
        destroyFp32256(groupEx);
        destroyFp32256(group4x);
        destroyFp32256(group6y);
        destroyFp32256(group7y);
        destroyFp32256(group5x);
        destroyFp32256(group3XY);
    }

    printf("\n~SIMD Computation~ Elapsed time: %f seconds", (double)(clock() - initComputation) / CLOCKS_PER_SEC);
    
    printf("\nq: %f", getFp32256(vectorX, 0));
    printf("\nq: %f", getFp32256(vectorX, 1));
    printf("\nq: %f", getFp32256(vectorX, 2));
    printf("\nq: %f", getFp32256(vectorX, 3));
    
    printf("\n==== vs ====");

    clock_t linComputation = clock();

    for (int i = 1; i <= max; i++) {
        float ii = (float)(1000.0 / (double)i);

        for (float f = 0.0; f < 8.0; f += 1.0) {

            for (int loop = 0; loop < 10000; loop++) {
                float num = ii + f;

                float group3XY = num * num * 3;
                float group5x = num * 5;
                float group7y = num * 7;

                group3XY -= group5x;
                group3XY += group7y;

                float group6y = num * 6;
                float group4x = num * 4;

                group4x -= group6y;

                float groupEx = num * 2.71828;
                float groupPIy = num * 3.14159;

                groupEx -= groupPIy;
                groupEx += 8;

                groupEx /= 3;

                group4x += groupEx;

                num = group3XY / group4x;
            }            
        }
    }

    printf("\n~Linear Computation~ Elapsed time: %f seconds\n\n\n", (double)(clock() - linComputation) / CLOCKS_PER_SEC);

    return 0;
}

}