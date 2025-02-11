/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class io_github_seal139_jSwarm_backend_cuda_CudaDriver */

#ifndef _Included_io_github_seal139_jSwarm_backend_cuda_CudaDriver
#define _Included_io_github_seal139_jSwarm_backend_cuda_CudaDriver
#ifdef __cplusplus
extern "C" {
#endif
	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaGetVersion
	 * Signature: ()Ljava/lang/String;
	 */
	JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetVersion
	(JNIEnv*, jclass);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaEnumerateDevices
	 * Signature: ()J
	 */
	JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaEnumerateDevices
	(JNIEnv*, jclass);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaGetDeviceInfo
	 * Signature: (I)J
	 */
	JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetDeviceInfo
	(JNIEnv*, jclass, jint);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaGetDeviceName
	 * Signature: (I)Ljava/lang/String;
	 */
	JNIEXPORT jstring JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetDeviceName
	(JNIEnv*, jclass, jint);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaCreateContext
	 * Signature: (I)J
	 */
	JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaCreateContext
	(JNIEnv*, jclass, jint);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaDeleteContext
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaDeleteContext
	(JNIEnv*, jclass, jlong);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaSetContext
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaSetContext
	(JNIEnv*, jclass, jlong);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaAddQueue
	 * Signature: (I)J
	 */
	JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaAddQueue
	(JNIEnv*, jclass, jint);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaDeleteQueue
	 * Signature: ([JI)I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaDeleteQueue
	(JNIEnv*, jclass, jlongArray, jint);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaCreateProgram
	 * Signature: (Ljava/lang/String;)J
	 */
	JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaCreateProgram
	(JNIEnv*, jclass, jstring);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaDeleteProgram
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaDeleteProgram
	(JNIEnv*, jclass, jlong);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaGetKernel
	 * Signature: (JLjava/lang/String;)J
	 */
	JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaGetKernel
	(JNIEnv*, jclass, jlong, jstring);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaLaunch
	 * Signature: (JJIIIIII[JI)V
	 */
	JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaLaunch
	(JNIEnv*, jclass, jlong, jlong, jint, jint, jint, jint, jint, jint, jlongArray, jint);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaHook
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaHook
	(JNIEnv*, jclass, jlong);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaSyncDataTo
	 * Signature: (JJJJ)I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaSyncDataTo
	(JNIEnv*, jclass, jlong, jlong, jlong, jlong);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaSyncDataFrom
	 * Signature: (JJJJ)I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaSyncDataFrom
	(JNIEnv*, jclass, jlong, jlong, jlong, jlong);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaUnhook
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaUnhook
	(JNIEnv*, jclass, jlong);

	/*
	 * Class:     io_github_seal139_jSwarm_backend_cuda_CudaDriver
	 * Method:    cudaWaitAll
	 * Signature: ()I
	 */
	JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_backend_cuda_CudaDriver_cudaWaitAll
	(JNIEnv*, jclass);

#ifdef __cplusplus
}
#endif
#endif
