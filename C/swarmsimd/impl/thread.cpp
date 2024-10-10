extern "C" {
#include "../header/thread.h"
#include "../header/vector.h"

#include <stdio.h>


    static ThreadData* threadData;    // Hold data for worker-thread
    static T_HANDLE* threads;       // Hold individual thread
    static int threadsCount;

    int getThreadCount() {
        return threadsCount;
    }

    T_RET threadExecutor(T_ARGS arg) {
        ThreadData* tData = (ThreadData*)arg;

        // Infinite loop to keep the thread running
        while(1) {

            // Wait for the 'queued' event to be signaled before continue executing
            T_WAIT(tData->mutex, tData->queued);

            if (tData->idle_flag == T_FLAG_QUEUED) {
                T_LOCK(tData); // Lock resource to prevent race condition

                tData->idle_flag = T_FLAG_BUSY;
                tData->function(tData->result, tData->threadIndex, tData->operand0, tData->operand1);
                tData->idle_flag = T_FLAG_IDLE; // Mark the thread as idle again

                T_WAKE(tData->ready);   // Notify another thread that this thread is idle and ready for next task            
                T_UNLOCK(tData);        // Unlock resource. Allowing another thread to access data
            }
            else if (tData->idle_flag == T_FLAG_DESTROY) {
                return NULL;    // Self-destruction signal
            }
         }

        return NULL; // Will never be called
    }

    int initExecutor(int maxThread) {
        threadsCount = maxThread;

        threadData = (ThreadData*)malloc(maxThread * sizeof(ThreadData));
        if (threadData == NULL) {
            return -1;
        }

        threads = (T_HANDLE*)malloc(maxThread * sizeof(T_HANDLE));
        if (threads == NULL) {
            return -1;
        }

        for (int i = 0; i < maxThread; i++) {
            threadData[i].threadIndex = i;
            threadData[i].idle_flag = T_FLAG_IDLE; // Initially idle

#ifdef _WIN32
            threadData[i].mutex = CreateMutex(NULL, FALSE, NULL);
            threadData[i].queued = CreateEvent(NULL, FALSE, FALSE, NULL);
            threadData[i].ready = CreateEvent(NULL, FALSE, FALSE, NULL);

            threads[i] = CreateThread(NULL, 0, threadExecutor, &threadData[i], 0, NULL);
            if (threads[i] == NULL) {
                exit(EXIT_FAILURE);
            }

            
#else

            pthread_mutex_init(&threadData[i].mutex, NULL);
            pthread_cond_init(&thread_data[i].queued, NULL);
            pthread_cond_init(&thread_data[i].ready, NULL);

            if (pthread_create(&threads[i], NULL, threadExecutor, &thread_data[i])) {
                fprintf(stderr, "Error creating thread %d\n", i);
                exit(EXIT_FAILURE);
            }
#endif
        }

        return 0;
    }

    void killExecutor() {
        for (int i = 0; i < threadsCount; i++) {
#ifdef _WIN32
            CloseHandle(threads[i]);
            CloseHandle(threadData[i].mutex);  // Clean up mutex handle
            CloseHandle(threadData[i].queued); // Clean up event handle
            CloseHandle(threadData[i].ready);
#else
            pthread_join(threads[i], NULL);
            pthread_mutex_destroy(&threadData[i].mutex); // Clean up mutex
            pthread_cond_destroy(&threadData[i].queued);
            pthread_cond_destroy(&threadData[i].ready);
#endif
        }

        free(threadData);
        free(threads);
    }

    // Function to set data and wake the thread
    void enqueue(LambdaFunc function, SimdVector* result, SimdVector* opr1, SimdVector* opr2, SimdVector* opr3) {
        for (int i = 0; i < threadsCount; i++) {
            ThreadData* tData = &threadData[i];
            
            // Lock the mutex for thread-specific data access
            T_LOCK(tData);

            // Set the data
            tData->function = function;
            tData->result = result;
            tData->operand0 = opr1;
            tData->operand1 = opr2;
            tData->operand2 = opr3;

            // Mark the thread as busy
            tData->idle_flag = T_FLAG_QUEUED;

            // Signal the thread to wake up
            T_WAKE(tData->queued);
            T_UNLOCK(tData);            
        }

        for (int i = 0; i < threadsCount; i++) {
            ThreadData* tData = &threadData[i];
            T_WAIT(tData->mutex, tData->ready);
        }
    }

    void waitAll() {
        for (int i = 0; i < threadsCount; i++) {
            ThreadData* tData = &threadData[i];

            // Wait for thread to become available
            if (tData->idle_flag != T_FLAG_IDLE) {
                T_WAIT(tData->mutex, tData->ready);
            }            
        }
    }

    void mutexLock() {
        for (int i = 0; i < threadsCount; i++) {
            ThreadData* tData = &threadData[i];

            // Wait for thread to become available
            if (tData->idle_flag != T_FLAG_IDLE) {
                T_WAIT(tData->mutex, tData->ready);
            }

            T_LOCK(tData);
        }
    }

    void mutexUnlock() {
        for (int i = 0; i < threadsCount; i++) {
            ThreadData* tData = &threadData[i];

            T_UNLOCK(tData);
        }
    }
}