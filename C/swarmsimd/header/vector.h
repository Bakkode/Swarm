#ifndef VECTOR_H_INCLUDED
#define VECTOR_H_INCLUDED

/**
*   32 bit thread indexer
*
*   Can indexing up to 2^32 threads
*/
typedef struct {
    int totalThreads; // Maximum thread possible (must be power of 2)
    int counter; // Cyclic thread index
} ThreadIndexer;

/**
*   Data container. This vector work by grouping multiple data into chunk
*   (Can be SSE, AVX2 or AVX512 register)
*   and group every chunk into collections based on thread
*/
typedef struct {
    ThreadIndexer threadIndex;

    long long int    chunkSize;
    float  incFactor;

    void** threadList;
    long long int* indexCtr;
} SimdVector;

#endif // VECTOR_H_INCLUDED
