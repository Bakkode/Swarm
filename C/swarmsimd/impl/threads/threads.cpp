#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include "../vector.h"

#define MAX_THREADS 10

// Function pointer type
typedef void (*ThreadFunction)(void*, int, void*, void*, void*);
typedef void (*ThreadFunction)(SimdVector*, int, SimdVector*, SimdVector*);
typedef void (*ThreadFunction)(SimdVector*, SimdVector*, SimdVector*);

// Thread structure
typedef struct {
    pthread_t thread;
    ThreadFunction func;

    void* resultPtr; 
    int   threadIndex;
    
    void* operand0;
    void* operand1;
} ReusableThread;

// Thread function that calls the function pointer
void* thread_start(void* arg) {
    ReusableThread* reusableThread = (ReusableThread*)arg;
    reusableThread->func(reusableThread->arg);
    return NULL;
}

// Initialize threads
void initialize_threads(ReusableThread* threadPtr, int numThreads) {
    for (int i = 0; i < numThreads; i++) {
        threadPtr[i].func = NULL;  // Initialize function pointer to NULL
        threadPtr[i].arg = NULL;   // Initialize argument to NULL
    }
}

// Create a thread and store it in the array
int create_thread(ReusableThread* threadPtr, int index, ThreadFunction func, void* arg) {
    if (index < 0 || index >= MAX_THREADS) {
        return -1; // Invalid index
    }

    threadPtr[index].func = func;
    threadPtr[index].arg = arg;

    if (pthread_create(&threadPtr[index].thread, NULL, thread_start, &threadPtr[index]) != 0) {
        perror("Failed to create thread");
        return -1;
    }

    return 0; // Success
}

// Wait for all threads to complete
void wait_for_threads(ReusableThread* threadPtr, int numThreads) {
    for (int i = 0; i < numThreads; i++) {
        if (threadPtr[i].func != NULL) { // Check if thread was created
            pthread_join(threadPtr[i].thread, NULL);
        }
    }
}

// Example function to be executed by the thread
void my_function(void* arg) {
    int* num = (int*)arg;
    printf("Thread running with argument: %d\n", *num);
}

int main() {
    ReusableThread threadPtr[MAX_THREADS];

    // Initialize threads
    initialize_threads(threadPtr, MAX_THREADS);

    // Create threads and pass arguments
    for (int i = 0; i < 5; i++) {
        int* arg = malloc(sizeof(int)); // Allocate memory for the argument
        *arg = i; // Set argument value
        create_thread(threadPtr, i, my_function, arg);
    }

    // Wait for all threads to complete
    wait_for_threads(threadPtr, 5);

    // Clean up dynamically allocated memory
    for (int i = 0; i < 5; i++) {
        free(threadPtr[i].arg); // Free the argument memory
    }

    return 0;
}
