#include "../header/Fp32Vector.h"
#include <vector>
#include <iostream>

using namespace std;

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Hook
(JNIEnv* env, jclass, jfloatArray array) {
    jobject* nativeStorage = new jobject;
    *nativeStorage = env->NewGlobalRef(array);

    return reinterpret_cast<jlong>(nativeStorage);
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Unhook
(JNIEnv* env, jclass, jlong cache) {
    jobject* globalRef = reinterpret_cast<jobject*>(cache);
    env->DeleteGlobalRef(*globalRef);
    delete globalRef;
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Construct
(JNIEnv*, jclass, jint cacheSize) {
    std::vector<float_t>* vec = new std::vector<float_t>();

    vec->reserve(cacheSize);
    return reinterpret_cast<jlong>(vec);
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Delete
(JNIEnv*, jclass, jlong ptr) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
    delete vec;
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32GetSize
(JNIEnv*, jclass, jlong ptr) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
    return static_cast<jint>(vec->size());
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Sync
(JNIEnv* env, jclass, jlong ptr, jlong source, jint to, jfloat incrPolicy) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    if (vec->capacity() - vec->size() < to) {
        size_t newSize = static_cast<size_t>(vec->capacity() * incrPolicy);
        vec->reserve(newSize);
    }

    jfloatArray* nativeStorage = reinterpret_cast<jfloatArray*>(source);
    jfloat* elements = (jfloat*)env->GetPrimitiveArrayCritical(*nativeStorage, NULL);
    vec->insert(vec->end(), elements, elements + to);

    env->ReleasePrimitiveArrayCritical(*nativeStorage, elements, JNI_ABORT);   
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Fetch(JNIEnv* env, jclass, jlong ptr, jlong dest, jint from, jint to) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    jint length = to - from + 1;
    jfloatArray* nativeStorage = reinterpret_cast<jfloatArray*>(dest);

    jfloat* elements = reinterpret_cast<jfloat*>(env->GetPrimitiveArrayCritical(*nativeStorage, NULL));
    std::memcpy(elements, &(*vec)[from], length * sizeof(float_t));

    env->ReleasePrimitiveArrayCritical(*nativeStorage, elements, 0);
}

JNIEXPORT jfloat JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32GetByIndex(JNIEnv*, jclass, jlong ptr, jint index) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    return static_cast<jfloat>((*vec)[index]);
}

// ===

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Clear(JNIEnv*, jclass, jlong ptr) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
    vec->clear();
}

//JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Contains(JNIEnv*, jclass, jlong ptr, jfloat value) {
//    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
//
//    if (find(vec->begin(), vec->end(), static_cast<float_t>(value)) != vec->end()) {
//        return JNI_TRUE;
//    }
//
//    return JNI_FALSE;
//}
//
//JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Remove(JNIEnv*, jclass, jlong ptr, jfloat value) {
//    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
//
//    vector<float_t>::iterator index = find(vec->begin(), vec->end(), static_cast<float_t>(value));
//
//    if (index == vec->end()) {
//        return JNI_FALSE;
//    }
//
//    vec->erase(index);
//    return JNI_TRUE;
//}
//
//
//
//
//JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32RemoveByIndex(JNIEnv*, jclass, jlong ptr, jint index) {
//    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
//
//    vec->erase(vec->begin() + index);
//}
//
