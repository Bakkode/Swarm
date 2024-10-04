#include<stdio.h>
#include<immintrin.h>
#include <time.h>
#include <string.h>

#include <simd.h>

#define ChunkSize 8


// ===== Vector =====



// ===== Constructor - Destructor =====

/**
*   Initialize FP32 scalar for SIMD processing
*/
SimdVector* createScalarFp32(float value) {
    SimdVector* vector = (SimdVector*)malloc(sizeof(SimdVector));
    ThreadIndexer cc   = {0, 0};

    vector->threadIndex = cc;
    vector->chunkSize   = 1;
    vector->incFactor   = 0;

    vector->indexCtr    = malloc(sizeof(int));
    vector->threadList  = _aligned_malloc(sizeof(__m256*), sizeof(__m256*));

    vector->threadList[0] = _aligned_malloc(sizeof(__m256), sizeof(__m256));
    vector->indexCtr[0] = 0;

    ((__m256*)(vector->threadList[0]))[0] = _mm256_set1_ps(value);

    return vector;
};

/**
*   Initialize FP32 vector for SIMD processing
*/
SimdVector* createVectorFp32(int totalThreads, int initialChunkSize, float incFactor) {
    SimdVector* vector = (SimdVector*)malloc(sizeof(SimdVector));
    ThreadIndexer cc   = {totalThreads - 1, totalThreads - 1};

    vector->threadIndex = cc;
    vector->chunkSize   = initialChunkSize;
    vector->incFactor   = 1 + incFactor;

    vector->indexCtr    = malloc(vector->threadIndex.totalThreads * sizeof(int));
    vector->threadList  = _aligned_malloc(vector->threadIndex.totalThreads * sizeof(__m256*), sizeof(__m256*));

    for(int i = 0; i <= vector->threadIndex.totalThreads; i++){
        vector->threadList[i] = _aligned_malloc(vector->chunkSize * sizeof(__m256), sizeof(__m256));
        vector->indexCtr[i] = -1;
    }

    return vector;
}

/**
*   Initialize FP32 vector based on reference
*/
SimdVector* deriveVectorFp32(SimdVector* reference) {
    SimdVector* vector = (SimdVector*)malloc(sizeof(SimdVector));

    *vector = *reference;

    vector->indexCtr    = malloc(reference->threadIndex.totalThreads * sizeof(int));
    vector->threadList  = _aligned_malloc(reference->threadIndex.totalThreads * sizeof(__m256*), sizeof(__m256*));
    vector->chunkSize   = reference->indexCtr[0];

    for(int i = 0; i <= reference->threadIndex.totalThreads; i++){
        vector->threadList[i] = _aligned_malloc(reference->chunkSize * sizeof(__m256), sizeof(__m256));
        vector->indexCtr[i] = reference->indexCtr[i];
    }

    return vector;
};

/**
*   Destroy FP32 container
*/
void destroyFp32(SimdVector* vector) {
    for(int i = 0; i <= vector->threadIndex.totalThreads; i++) {
        _mm_free(vector->threadList[i]);
    }

    _mm_free(vector->threadList);
    free(vector->indexCtr);
}

// ===== ===== =====


void appendFp32(SimdVector* vector,
         float f0, float f1, float f2, float f3,
         float f4, float f5, float f6, float f7){

    // Thread index
    int threadIndex = cycle(&(vector->threadIndex));

    // Collection index
    vector->indexCtr[threadIndex] += 1;
    int* indexCtr = &(vector->indexCtr[threadIndex]);

    // Max allocated value
    int* maxMember = &(vector->chunkSize);

    // Check if memory is sufficient?
    if(*indexCtr == *maxMember){
        // if no, reallocate

        // Define new size first
        *maxMember = (int)(*maxMember * vector->incFactor);
        int newSize = *maxMember * sizeof(__m256);

        // Reallocate all collection across the threads
        for(int i = 0; i <= vector->threadIndex.totalThreads; i++){
            vector->threadList[i] = _aligned_realloc(vector->threadList[i], newSize, sizeof(__m256));
        }
    }

    ((__m256*)(vector->threadList[threadIndex]))[*indexCtr] = _mm256_set_ps(f7, f6, f5, f4, f3, f2, f1, f0);
}

float getFp32(SimdVector* vector, long long int index) {
    int maxThread = 1 + (vector->threadIndex.totalThreads);

    // Define which global collection is used
    int q = index / ChunkSize; // AVX bulk value;

    int threadIndex     = q % maxThread;
    int collectionIndex = q / maxThread;
    int registerIndex   = index % ChunkSize; // AVX bulk value;

	float values[8];

	__m256 simd = ((__m256*)vector->threadList[threadIndex])[collectionIndex];
    _mm256_storeu_ps(values, simd);

    return values[registerIndex];
}

void setFp32(SimdVector* vector, long long int index, float newValue) {
    int maxThread = 1+(vector->threadIndex.totalThreads);

    // Define which global collection is used
    int q = index / ChunkSize; // AVX bulk value;

    int threadIndex     = q % maxThread;
    int collectionIndex = q / maxThread;
    int registerIndex   = index % ChunkSize; // AVX bulk value;

	float v[ChunkSize];

	__m256 simd = ((__m256*)vector->threadList[threadIndex])[collectionIndex];
    _mm256_storeu_ps(v, simd);
    v[registerIndex] = newValue;

    ((__m256*)vector->threadList[threadIndex])[collectionIndex] = _mm256_set_ps(v[7], v[6], v[5], v[4], v[3], v[2], v[1], v[0]);
}

// ===== ===== =====

// ===== Arithmetic =====

void addScalarFp32(SimdVector* result,
                   SimdVector* scalar1, SimdVector* scalar2) {

    ((__m256*)(result->threadList[0]))[0] = _mm256_add_ps(((__m256*)scalar1->threadList[0])[0],
                                                          ((__m256*)scalar2->threadList[0])[0]);
}


void addVectorFp32(SimdVector* result, int threadIndex,
                   SimdVector* vector1, SimdVector* vector2) {

    int indexMax = vector1->indexCtr[threadIndex];
    for(int ii = 0; ii < indexMax; ii++){
        ((__m256*)result->threadList[threadIndex])[ii] = _mm256_add_ps(((__m256*)vector1->threadList[threadIndex])[ii],
                                                                       ((__m256*)vector2->threadList[threadIndex])[ii]);
    }
}

void addMixedFp32(SimdVector* result, int threadIndex,
                  SimdVector* vector, SimdVector* scalar) {

    int indexMax = vector->indexCtr[threadIndex];
    for(int ii = 0; ii < indexMax; ii++){
        ((__m256*)result->threadList[threadIndex])[ii] = _mm256_add_ps(((__m256*)vector->threadList[threadIndex])[ii],
                                                                       ((__m256*)scalar->threadList[0])[0]);
    }
}

// ===== ===== =====





// ====== Thread Management =====


// ===== ===== =====


// ====== Benchmark =====

// ===== ===== =====

int main(void)
{

    SimdVector* vectorPtr = createVectorFp32(4, 40, 0.4);

    clock_t start = clock();
    for(int i = 0; i < 1000000; i++) {
        int ii = i * 10;

        appendFp32(vectorPtr, ii + 0, ii + 1, ii + 2, ii + 3, ii + 4, ii + 5, ii + 6, ii + 7);
    }

    SimdVector* scalar1Ptr = createScalarFp32(8);
    SimdVector* scalar2Ptr = createScalarFp32(2);

    // Print the result
    double time = (double)(clock() - start) / CLOCKS_PER_SEC;
    printf("Elapsed time: %f seconds for %i insertion. Showing first 45 data", time, 7000000);


    for(int i = 0; i < 45; i++){
        if(i % ChunkSize == 0){
            printf("\nValue is:");
        }

        printf("%f, ", getFp32(vectorPtr, i));
    }

    printf("\n-----------\n");



    clock_t start2 = clock();



    SimdVector* scalar3 = deriveVectorFp32(scalar1Ptr);
    addScalarFp32(scalar3, scalar1Ptr, scalar2Ptr);

    SimdVector* vector5Ptr = deriveVectorFp32(vectorPtr);
    for(int i = 0; i <= vectorPtr->threadIndex.totalThreads; i++){
        addVectorFp32(vector5Ptr, i, vectorPtr, vectorPtr);
    }

    SimdVector* result = deriveVectorFp32(vector5Ptr);
    for(int i = 0; i <= vector5Ptr->threadIndex.totalThreads; i++){
        addMixedFp32(result, i, vector5Ptr, scalar3);
    }

    printf("\nElapsed time: %f seconds for addition. Showing first 45 data", (double)(clock() - start2) / CLOCKS_PER_SEC);

    float values[8];
    __m256 simd = ((__m256*)(scalar3->threadList[0]))[0];
    _mm256_storeu_ps(values, simd);

    for(int i = 0; i < 45; i++){
        if(i % 8 == 0){
            printf("\nValue is:");
        }

        printf("%f, ", getFp32(result, i));
    }

    destroyFp32(vectorPtr);

    int num;
    scanf("%d", &num);
    return 0;
}
