#ifndef VECTOR_H_INCLUDED
#define VECTOR_H_INCLUDED

/**
*   32 bit thread indexer
*
*   Can indexing up to 2^32 threads
*/
typedef struct {
    unsigned int totalThreads; // Maximum thread possible (must be power of 2)
    unsigned int counter; // Cyclic thread index
} ThreadIndexer;

/**
*   Data container. This vector work by grouping multiple data into chunk
*   (Can be SSE, AVX2 or AVX512 register)
*   and group every chunk into collections based on thread
*/
typedef struct {
    ThreadIndexer threadIndex;

    int    chunkSize;
    float  incFactor;

    void** threadList;
    int* indexCtr;
} SimdVector;

#endif // VECTOR_H_INCLUDED
