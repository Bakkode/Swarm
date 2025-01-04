#include "../header/Vector.h"
#include <vector>
#include <iostream>

using namespace std;

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Construct(JNIEnv*, jclass, jint bufferSize) {
    std::vector<float_t>* vec = new std::vector<float_t>();

    vec->reserve(bufferSize);
    return reinterpret_cast<jlong>(vec);
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Delete(JNIEnv*, jclass, jlong ptr) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    delete vec;
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32GetSize(JNIEnv*, jclass, jlong ptr) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    return static_cast<jint>(vec->size());
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32AllocateMore(JNIEnv*, jclass, jlong ptr, jint cache, jfloat incrPolicy) {
//    cout << "allocated" << endl;
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    size_t newSize = static_cast<size_t>(vec->capacity() * incrPolicy);
    newSize       -= (newSize % cache);

    vec->reserve(newSize);
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Clear(JNIEnv*, jclass, jlong ptr) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
    vec->clear();
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Contains(JNIEnv*, jclass, jlong ptr, jfloat value) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    if (find(vec->begin(), vec->end(), static_cast<float_t>(value)) != vec->end()) {
        return JNI_TRUE;
    }

    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Remove(JNIEnv*, jclass, jlong ptr, jfloat value) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    vector<float_t>::iterator index = find(vec->begin(), vec->end(), static_cast<float_t>(value));

    if (index == vec->end()) {
        return JNI_FALSE;
    }

    vec->erase(index);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Sync(JNIEnv* env, jclass, jlong ptr, jfloatArray source, jint count, jint cacheSize) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    jfloat* elements = env->GetFloatArrayElements(source, nullptr);
    vec->insert(vec->end(), elements, elements + count);
    env->ReleaseFloatArrayElements(source, elements, JNI_ABORT);


    //jfloat* elements = reinterpret_cast<jfloat*>(env->GetPrimitiveArrayCritical(source, NULL));
    //vec->insert(vec->end(), elements, elements + count);
    //env->ReleasePrimitiveArrayCritical(source, elements, JNI_ABORT);

    return vec->capacity() - vec->size() == cacheSize;
}

JNIEXPORT jfloatArray JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Fetch(JNIEnv* env, jclass, jlong ptr, jint from, jint to) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    jint length = to - from + 1;
    jfloatArray resultArray = env->NewFloatArray(length);

    env->SetFloatArrayRegion(resultArray, 0, length, reinterpret_cast<const jfloat*>(&(*vec)[from]));

    return resultArray;
}
JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32RemoveByIndex(JNIEnv*, jclass, jlong ptr, jint index) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    vec->erase(vec->begin() + index);
}

JNIEXPORT jfloat JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32GetByIndex(JNIEnv*, jclass, jlong ptr, jint index) {
    std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

    return static_cast<jfloat>((*vec)[index]);
}