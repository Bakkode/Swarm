extern "C" {

#include "../../header/avx/trnfravx.h"
#include <immintrin.h>
#include <intrin.h>

static unsigned int ONE = 1;

static inline int cycle(ThreadIndexer* cc) {
    cc->counter = (cc->counter + 1) & cc->totalThreads;
    return cc->counter;
}

/**
*   Initialize FP32 scalar for SIMD processing
*/
SimdVector* createScalarFp32256(float value) {
    SimdVector* vector = (SimdVector*)malloc(sizeof(SimdVector));
    if (vector == NULL) {
        return NULL;
    }

    ThreadIndexer cc = { 0, 0 };
    vector->threadIndex = cc;
    vector->chunkSize = 1;
    vector->incFactor = 0;

    vector->indexCtr = (long long int*)malloc(sizeof(long long int));
    if (vector->indexCtr == NULL) {
        free(vector);

        return NULL;
    }

    vector->threadList = (void**)_aligned_malloc(sizeof(__m256*), sizeof(__m256*));
    if (vector->threadList == NULL) {
        free(vector->indexCtr);
        free(vector);

        return NULL;
    }

    vector->threadList[0] = _aligned_malloc(sizeof(__m256), sizeof(__m256));
    if (vector->threadList[0] == NULL) {
        _mm_free(vector->threadList);
        free(vector->indexCtr);
        free(vector);

        return NULL;
    }

    vector->indexCtr[0] = 0;

    ((__m256*)(vector->threadList[0]))[0] = _mm256_set1_ps(value);

    return vector;
};

/**
*   Initialize FP32 vector for SIMD processing
*/
SimdVector* createVectorFp32256(int totalThreads, int initialChunkSize, float incFactor) {
    SimdVector* vector = (SimdVector*)malloc(sizeof(SimdVector));
    if (vector == NULL) {
        return NULL;
    }

    ThreadIndexer cc = { totalThreads - 1, totalThreads - 1 };
    vector->threadIndex = cc;
    vector->chunkSize = initialChunkSize;
    vector->incFactor = 1 + incFactor;

    vector->indexCtr = (long long int*)malloc(totalThreads * sizeof(long long int));
    if (vector->indexCtr == NULL) {
        free(vector);

        return NULL;
    }

    vector->threadList = (void**)_aligned_malloc(totalThreads * sizeof(__m256*), sizeof(__m256*));
    if (vector->threadList == NULL) {
        free(vector->indexCtr);
        free(vector);

        return NULL;
    }

    for (int i = 0; i < totalThreads; i++) {
        vector->threadList[i] = _aligned_malloc(vector->chunkSize * sizeof(__m256), sizeof(__m256));
        if (vector->threadList[i] == NULL) {
            if (i > 0) {
                for (int j = 0; j < i; j++) {
                    _mm_free(vector->threadList[j]);
                }
            }

            _mm_free(vector->threadList);
            free(vector->indexCtr);
            free(vector);

            return NULL;
        }

        vector->indexCtr[i] = -1;
    }

    return vector;
}

/**
*   Initialize FP32 vector or scalar with configuration based on reference
*/
SimdVector* deriveVectorFp32256(SimdVector* reference) {
    SimdVector* vector = (SimdVector*)malloc(sizeof(SimdVector));
    if (vector == NULL) {
        return NULL;
    }

    *vector = *reference;

    vector->indexCtr = (long long int*)malloc((long long int)(1 + reference->threadIndex.totalThreads) * sizeof(long long int));
    if (vector->indexCtr == NULL) {
        free(vector);

        return NULL;
    }

    vector->threadList = (void**)_aligned_malloc((unsigned long long int)(1 + reference->threadIndex.totalThreads) * sizeof(__m256*), sizeof(__m256*));
    if (vector->threadList == NULL) {
        free(vector->indexCtr);
        free(vector);

        return NULL;
    }

    vector->chunkSize = reference->indexCtr[0];

    for (int i = 0; i <= reference->threadIndex.totalThreads; i++) {
        vector->threadList[i] = _aligned_malloc(reference->chunkSize * sizeof(__m256), sizeof(__m256));
        if (vector->threadList[i] == NULL) {
            if (i > 0) {
                for (int j = 0; j < i; j++) {
                    _mm_free(vector->threadList[j]);
                }
            }

            _mm_free(vector->threadList);
            free(vector->indexCtr);
            free(vector);

            return NULL;
        }

        vector->indexCtr[i] = reference->indexCtr[i];
    }

    return vector;
}

/**
*   Destroy FP32 container
*/
void destroyFp32256(SimdVector* vector) {
    for (int i = 0; i <= vector->threadIndex.totalThreads; i++) {
        _mm_free(vector->threadList[i]);
    }

    _mm_free(vector->threadList);
    free(vector->indexCtr);
    
    free(vector);
}

int appendFp32256(SimdVector* vector,
    float f0, float f1, float f2, float f3,
    float f4, float f5, float f6, float f7) {

    // Thread index
    int threadIndex = cycle(&(vector->threadIndex));

    // Collection index
    long long int* indexCtr = &(vector->indexCtr[threadIndex]);
    *indexCtr += 1;

    // Max allocated value
    long long int* maxMember = &(vector->chunkSize);

    // Check if memory is sufficient?
    if (*indexCtr == *maxMember) {
        // if no, reallocate

        // Define new size first
        *maxMember = (long long int)(*maxMember * vector->incFactor) + 1;
        long long int newSize =  (*maxMember * sizeof(__m256));

        // Reallocate all collection across the threads
        for (int i = 0; i <= vector->threadIndex.totalThreads; i++) {

            void* v = _aligned_realloc(vector->threadList[i], newSize, sizeof(__m256));
            if (v == NULL) {
                return 0;
            }

            vector->threadList[i] = v;
        }
    }

    ((__m256*)(vector->threadList[threadIndex]))[*indexCtr] = _mm256_set_ps(f7, f6, f5, f4, f3, f2, f1, f0);

    return 1;
}

constexpr long long int ChunkSize = 8;

float getFp32256(SimdVector* vector, long long int index) {
    int maxThread = 1 + (vector->threadIndex.totalThreads);

    // Define which global collection is used
    long long int q = index / ChunkSize; // AVX bulk value;

    int threadIndex = q % maxThread;
    long long int collectionIndex = q / maxThread;
    int registerIndex = index % ChunkSize; // AVX bulk value;

    float values[8];

    __m256 simd = ((__m256*)vector->threadList[threadIndex])[collectionIndex];
    _mm256_storeu_ps(values, simd);

    return values[registerIndex];
}

int setFp32256(SimdVector* vector, long long int index, float newValue) {
    int maxThread = 1 + (vector->threadIndex.totalThreads);

    // Define which global collection is used
    long long int q = index / ChunkSize; // AVX bulk value;

    int threadIndex = q % maxThread;
    long long int collectionIndex = q / maxThread;
    int registerIndex = index % ChunkSize; // AVX bulk value;


    // Max allocated value
    long long int* maxMember = &(vector->indexCtr[threadIndex]);

    // Check if memory is sufficient?
    if (collectionIndex > *maxMember) {
        return 0;
    }

    float v[ChunkSize];

    __m256 simd = ((__m256*)vector->threadList[threadIndex])[collectionIndex];
    _mm256_storeu_ps(v, simd);
    v[registerIndex] = newValue;

    ((__m256*)vector->threadList[threadIndex])[collectionIndex] = _mm256_set_ps(v[7], v[6], v[5], v[4], v[3], v[2], v[1], v[0]);

    return 1;
}

long long int getSizeFp32256(SimdVector* vector) {
    long long int size = 0;

    for (int i = 0; i <= vector->threadIndex.totalThreads; i++) {
        size += (vector->indexCtr[i] + 1) * 8;
    }

    return size;
}
}