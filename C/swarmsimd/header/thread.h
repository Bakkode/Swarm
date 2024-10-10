
/**
    Define worker-thread used by SIMD executor.
    This worker-thread is expected to run simultaneously to perform parallel data computations along with SIMD instructions.

    @author: Septian P.R. <septian13.ian@gmail.com>
    @contributor: -
*/

#include "vector.h"

#ifndef THREAD_H_INCLUDED
#define THREAD_H_INCLUDED

#ifdef _WIN32 // Windows
#include <windows.h>

#define T_MUTEX             HANDLE
#define T_HANDLE            HANDLE
#define T_ALARM             HANDLE
#define T_ARGS              LPVOID
#define T_RET               DWORD WINAPI

#define T_LOCK(q)           WaitForSingleObject(q->mutex, INFINITE)
#define T_UNLOCK(q)         ReleaseMutex(q->mutex)

#define T_WAKE(e)           SetEvent(e)
#define T_WAIT(mutex, e)    WaitForSingleObject(e, INFINITE)

#else // POSIX

#include <pthread.h>
#include <unistd.h> // For sleep()

#define T_MUTEX         pthread_mutex_t
#define T_HANDLE        pthread_t
#define T_ALARM         pthread_cond_t 
#define T_ARGS          void*
#define T_RET           void*

#define T_LOCK(q)       pthread_mutex_lock(&q->mutex)
#define T_UNLOCK(q)     pthread_mutex_unlock(&q->mutex)

#define T_WAKE(e)       pthread_cond_signal(&e)
#define T_WAIT(mutex, e)  \
    pthread_mutex_lock(&mutex);  \
    pthread_cond_wait(&e, &mutex); \
    pthread_mutex_unlock(&mutex)

#endif

#define T_FLAG_IDLE     0
#define T_FLAG_QUEUED   1
#define T_FLAG_BUSY     2
#define T_FLAG_DESTROY -1
typedef int T_FLAG;

typedef void (*LambdaFunc)( //
    SimdVector* /*result*/,
    int /*threadIndex*/,
    SimdVector* /*operand0*/,
    SimdVector* /*operand1*/
    );

typedef struct {
    int threadIndex;

    LambdaFunc function; // Function pointer
    SimdVector* result;
    SimdVector* operand0;
    SimdVector* operand1;
    SimdVector* operand2;

    T_MUTEX mutex;

    T_ALARM queued;
    T_ALARM ready;
    T_FLAG idle_flag;

} ThreadData;

/// <summary>
/// Create worker-threads based on specified max threads
/// </summary>
/// <param name="maxThreads">: Threads to be created. For the best performance, do not exceed physcal thread available specified by hardware or VM </param>
int initExecutor(int maxThreads);

/// <summary>
/// Destroy worker-threads
/// </summary>
void killExecutor();

/// <summary>
/// Wait for thread to completely execute task
/// </summary>
void waitAll();

/// <summary>
/// Lock resource before modifying to prevent race condition
/// </summary>
void mutexLock();

/// <summary>
/// Unlock resources and let the worker-thread continue executing when task is available
/// </summary>
void mutexUnlock();

/// <summary>
/// Assign task to worker
/// </summary>
void enqueue(LambdaFunc function, SimdVector* result, SimdVector* opr1, SimdVector* opr2, SimdVector* opr3);

int getThreadCount();

#endif // THREAD_H_INCLUDED