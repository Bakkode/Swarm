extern "C" {
#include <stdio.h>

#include <time.h>


#include "../header/thread.h"
#include "../header/vector.h"
#include "../header/avx/avx.h"
#include <immintrin.h>

    int mainaa(void)
    {
        // Define X, Y, Z
        int max = 5000000;
        int thread = 8;

        initExecutor(thread);

        clock_t initStart = clock();
        SimdVector* constThree = createScalarFp32256(3);
        SimdVector* constFour = createScalarFp32256(4);
        SimdVector* constFive = createScalarFp32256(5);
        SimdVector* constSix = createScalarFp32256(6);
        SimdVector* constSeven = createScalarFp32256(7);
        SimdVector* constEight = createScalarFp32256(8);

        SimdVector* constPi = createScalarFp32256(3.14159);
        SimdVector* constE = createScalarFp32256(2.71828);

        SimdVector* vectorX = createVectorFp32256(thread, 500, 0.3f);

        for (int i = 1; i <= max; i++) {
            float ii = (float)(1000.0 / (double)i);

            appendFp32256(vectorX, ii, ii + 1.0f, ii + 2.0f, ii + 3.0f, ii + 4.0f, ii + 5.0f, ii + 6.0f, ii + 7.0f);
        }

        printf("\n~Initialization~ of %i data. Elapsed time: %f seconds", max, (double)(clock() - initStart) / CLOCKS_PER_SEC);

        printf("\n\n==== === ====\n\n");

        clock_t initComputation = clock();

        for (int loop = 0; loop < 100; loop++) {
            SimdVector* group3XY = deriveVectorFp32256(vectorX);

            mulVectorFp32256(group3XY, vectorX, vectorX);
            mulMixedFp32256(group3XY, group3XY, constThree);
                
            SimdVector* group5x = deriveVectorFp32256(vectorX);
            mulMixedFp32256(group5x, vectorX, constFive);

            SimdVector* group7y = deriveVectorFp32256(vectorX);
            mulMixedFp32256(group7y, vectorX, constSeven);

            subVectorFp32256(group3XY, group3XY, group5x);
            addVectorFp32256(group3XY, group3XY, group7y);

            SimdVector* group6y = deriveVectorFp32256(vectorX);
            mulMixedFp32256(group6y, vectorX, constSix);

            SimdVector* group4x = deriveVectorFp32256(vectorX);
            mulMixedFp32256(group4x, vectorX, constFour);

            subVectorFp32256(group4x, group4x, group6y);

            SimdVector* groupEx = deriveVectorFp32256(vectorX);
            mulMixedFp32256(groupEx, vectorX, constE);

            SimdVector* groupPIy = deriveVectorFp32256(vectorX);
            mulMixedFp32256(groupPIy, vectorX, constPi);

            subVectorFp32256(groupEx, groupEx, groupPIy);
            addMixedFp32256(groupEx, groupEx, constEight);
            divMixedFp32256(groupEx, groupEx, constThree);
            addVectorFp32256(group4x, group4x, groupEx);
            divVectorFp32256(vectorX, group3XY, group4x);

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

        return 0;
    }

}