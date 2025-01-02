#include "../header/Vector.h"
#include <vector>
#include <iostream>

using namespace std;

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Construct(JNIEnv*, jclass) {
	std::vector<int16_t>* vec = new std::vector<int16_t>();

	return reinterpret_cast<jlong>(vec);
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Construct(JNIEnv*, jclass) {
	std::vector<int32_t>* vec = new std::vector<int32_t>();

	return reinterpret_cast<jlong>(vec);
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Construct(JNIEnv*, jclass) {
	std::vector<int64_t>* vec = new std::vector<int64_t>();

	return reinterpret_cast<jlong>(vec);
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Construct(JNIEnv*, jclass) {
	std::vector<float_t>* vec = new std::vector<float_t>();

	return reinterpret_cast<jlong>(vec);
}

JNIEXPORT jlong JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Construct(JNIEnv*, jclass) {
	std::vector<double_t>* vec = new std::vector<double_t>();

	return reinterpret_cast<jlong>(vec);
}


JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Delete(JNIEnv*, jclass, jlong ptr) {
	std::vector<int16_t>* vec = reinterpret_cast<std::vector<int16_t>*>(ptr);

	delete vec;
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Delete(JNIEnv*, jclass, jlong ptr) {
	std::vector<int32_t>* vec = reinterpret_cast<std::vector<int32_t>*>(ptr);

	delete vec;
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Delete(JNIEnv*, jclass, jlong ptr) {
	std::vector<int64_t>* vec = reinterpret_cast<std::vector<int64_t>*>(ptr);

	delete vec;
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Delete(JNIEnv*, jclass, jlong ptr) {
	std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

	delete vec;
	//std::cout << "mem deleted" << std::endl;
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Delete(JNIEnv*, jclass, jlong ptr) {
	std::vector<double_t>* vec = reinterpret_cast<std::vector<double_t>*>(ptr);

	delete vec;
}


JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Size(JNIEnv*, jclass, jlong ptr) {
	std::vector<int16_t>* vec = reinterpret_cast<std::vector<int16_t>*>(ptr);

	return static_cast<jint>(vec->size());
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Size(JNIEnv*, jclass, jlong ptr) {
	std::vector<int32_t>* vec = reinterpret_cast<std::vector<int32_t>*>(ptr);

	return static_cast<jint>(vec->size());
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Size(JNIEnv*, jclass, jlong ptr) {
	std::vector<int64_t>* vec = reinterpret_cast<std::vector<int64_t>*>(ptr);

	return static_cast<jint>(vec->size());
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Size(JNIEnv*, jclass, jlong ptr) {
	std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

	return static_cast<jint>(vec->size());
}

JNIEXPORT jint JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Size(JNIEnv*, jclass, jlong ptr) {
	std::vector<double_t>* vec = reinterpret_cast<std::vector<double_t>*>(ptr);

	return static_cast<jint>(vec->size());
}


JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Clear(JNIEnv*, jclass, jlong ptr) {
	std::vector<int16_t>* vec = reinterpret_cast<std::vector<int16_t>*>(ptr);
	vec->clear();
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Clear(JNIEnv*, jclass, jlong ptr) {
	std::vector<int32_t>* vec = reinterpret_cast<std::vector<int32_t>*>(ptr);
	vec->clear();
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Clear(JNIEnv*, jclass, jlong ptr) {
	std::vector<int64_t>* vec = reinterpret_cast<std::vector<int64_t>*>(ptr);
	vec->clear();
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Clear(JNIEnv*, jclass, jlong ptr) {
	std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);
	vec->clear();
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Clear(JNIEnv*, jclass, jlong ptr) {
	std::vector<double_t>* vec = reinterpret_cast<std::vector<double_t>*>(ptr);
	vec->clear();
}


JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Contains(JNIEnv*, jclass, jlong ptr, jshort value) {
	std::vector<int16_t>* vec = reinterpret_cast<std::vector<int16_t>*>(ptr);

	if (find(vec->begin(), vec->end(), static_cast<int16_t>(value)) != vec->end()) {
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Contains(JNIEnv*, jclass, jlong ptr, jint value) {
	std::vector<int32_t>* vec = reinterpret_cast<std::vector<int32_t>*>(ptr);

	if (find(vec->begin(), vec->end(), static_cast<int32_t>(value)) != vec->end()) {
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Contains(JNIEnv*, jclass, jlong ptr, jlong value) {
	std::vector<int64_t>* vec = reinterpret_cast<std::vector<int64_t>*>(ptr);

	if (find(vec->begin(), vec->end(), static_cast<int64_t>(value)) != vec->end()) {
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Contains(JNIEnv*, jclass, jlong ptr, jfloat value) {
	std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

	if (find(vec->begin(), vec->end(), static_cast<float_t>(value)) != vec->end()) {
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Contains(JNIEnv*, jclass, jlong ptr, jdouble value) {
	std::vector<double_t>* vec = reinterpret_cast<std::vector<double_t>*>(ptr);

	if (find(vec->begin(), vec->end(), static_cast<double_t>(value)) != vec->end()) {
		return JNI_TRUE;
	}

	return JNI_FALSE;
}


JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Remove(JNIEnv*, jclass, jlong ptr, jshort value) {
	std::vector<int16_t>* vec = reinterpret_cast<std::vector<int16_t>*>(ptr);

	vector<int16_t>::iterator index = find(vec->begin(), vec->end(), static_cast<int16_t>(value));

	if (index == vec->end()) {
		return JNI_FALSE;
	}

	vec->erase(index);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Remove(JNIEnv*, jclass, jlong ptr, jint value) {
	std::vector<int32_t>* vec = reinterpret_cast<std::vector<int32_t>*>(ptr);

	vector<int32_t>::iterator index = find(vec->begin(), vec->end(), static_cast<int32_t>(value));

	if (index == vec->end()) {
		return JNI_FALSE;
	}

	vec->erase(index);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Remove(JNIEnv*, jclass, jlong ptr, jlong value) {
	std::vector<int64_t>* vec = reinterpret_cast<std::vector<int64_t>*>(ptr);

	vector<int64_t>::iterator index = find(vec->begin(), vec->end(), static_cast<int64_t>(value));

	if (index == vec->end()) {
		return JNI_FALSE;
	}

	vec->erase(index);
	return JNI_TRUE;
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

JNIEXPORT jboolean JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Remove(JNIEnv*, jclass, jlong ptr, jdouble value) {
	std::vector<double_t>* vec = reinterpret_cast<std::vector<double_t>*>(ptr);

	vector<double_t>::iterator index = find(vec->begin(), vec->end(), static_cast<double_t>(value));

	if (index == vec->end()) {
		return JNI_FALSE;
	}

	vec->erase(index);
	return JNI_TRUE;
}


JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Sync(JNIEnv* env, jclass, jlong ptr, jshortArray source, jint count) {
	std::vector<int16_t>* vec = reinterpret_cast<std::vector<int16_t>*>(ptr);

	size_t finalSize = vec->size() + count;
	if (finalSize > vec->capacity()) {
		size_t tSize = vec->capacity() * 2;
		if (tSize < finalSize) {
			tSize = finalSize * 128;
		}

		vec->reserve(tSize);
	}

	jshort* elements = env->GetShortArrayElements(source, nullptr);
	for (jint i = 0; i < count; ++i) {
		vec->push_back(static_cast<int16_t>(elements[i]));
	}	
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Sync(JNIEnv* env, jclass, jlong ptr, jintArray source, jint count) {
	std::vector<int32_t>* vec = reinterpret_cast<std::vector<int32_t>*>(ptr);

	size_t finalSize = vec->size() + count;
	if (finalSize > vec->capacity()) {
		size_t tSize = vec->capacity() * 2;
		if (tSize < finalSize) {
			tSize = finalSize * 128;
		}

		vec->reserve(tSize);
	}

	jint* elements = env->GetIntArrayElements(source, nullptr);
	for (jint i = 0; i < count; ++i) {
		vec->push_back(static_cast<int32_t>(elements[i]));
	}
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Sync(JNIEnv* env, jclass, jlong ptr, jlongArray source, jint count) {
	std::vector<int64_t>* vec = reinterpret_cast<std::vector<int64_t>*>(ptr);

	size_t finalSize = vec->size() + count;
	if (finalSize > vec->capacity()) {
		size_t tSize = vec->capacity() * 2;
		if (tSize < finalSize) {
			tSize = finalSize * 128;
		}

		vec->reserve(tSize);
	}

	jlong* elements = env->GetLongArrayElements(source, nullptr);
	for (jint i = 0; i < count; ++i) {
		vec->push_back(static_cast<int64_t>(elements[i]));
	}
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Sync(JNIEnv* env, jclass, jlong ptr, jfloatArray source, jint count) {
	std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

	size_t finalSize = vec->size() + count;
	if (finalSize > vec->capacity()) {
		size_t tSize = vec->capacity() * 2;
		if (tSize < finalSize) {
			tSize = finalSize * 128;
		}

		vec->reserve(tSize);
	}
	
	jfloat* elements = env->GetFloatArrayElements(source, nullptr);
	vec->insert(vec->end(), elements, elements + count);

	/*jfloat* elements = env->GetFloatArrayElements(source, nullptr);
	for (jint i = 0; i < count; ++i) {
		vec->push_back(static_cast<float_t>(elements[i]));
	}*/

	env->ReleaseFloatArrayElements(source, elements, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Sync(JNIEnv* env, jclass, jlong ptr, jdoubleArray source, jint count) {
	std::vector<double_t>* vec = reinterpret_cast<std::vector<double_t>*>(ptr);

	size_t finalSize = vec->size() + count;
	if (finalSize > vec->capacity()) {
		size_t tSize = vec->capacity() * 2;
		if (tSize < finalSize) {
			tSize = finalSize * 128;
		}

		vec->reserve(tSize);
	}

	jdouble* elements = env->GetDoubleArrayElements(source, nullptr);
	for (jint i = 0; i < count; ++i) {
		vec->push_back(static_cast<double_t>(elements[i]));
	}

	env->ReleaseDoubleArrayElements(source, elements, JNI_ABORT);
}


JNIEXPORT jshortArray JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_ShortVector_int16Fetch(JNIEnv* env, jclass, jlong ptr, jint from, jint to) {
	std::vector<int16_t>* vec = reinterpret_cast<std::vector<int16_t>*>(ptr);

	jint length = to - from + 1;
	jshortArray resultArray = env->NewShortArray(length);

	jshort* tempBuffer = new jshort[length];
	for (jint i = 0; i < length; ++i) {
		tempBuffer[i] = static_cast<jshort>((*vec)[from + i]);
	}

	env->SetShortArrayRegion(resultArray, 0, length, tempBuffer);

	return resultArray;
}

JNIEXPORT jintArray JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_IntegerVector_int32Fetch(JNIEnv* env, jclass, jlong ptr, jint from, jint to) {
	std::vector<int32_t>* vec = reinterpret_cast<std::vector<int32_t>*>(ptr);

	jint length = to - from + 1;
	jintArray resultArray = env->NewIntArray(length);

	jint* tempBuffer = new jint[length];
	for (jint i = 0; i < length; ++i) {
		tempBuffer[i] = static_cast<jint>((*vec)[from + i]);
	}

	env->SetIntArrayRegion(resultArray, 0, length, tempBuffer);

	return resultArray;
}

JNIEXPORT jlongArray JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_LongVector_int64Fetch(JNIEnv* env, jclass, jlong ptr, jint from, jint to) {
	std::vector<int64_t>* vec = reinterpret_cast<std::vector<int64_t>*>(ptr);

	jint length = to - from + 1;
	jlongArray resultArray = env->NewLongArray(length);

	jlong* tempBuffer = new jlong[length];
	for (jint i = 0; i < length; ++i) {
		tempBuffer[i] = static_cast<jlong>((*vec)[from + i]);
	}

	env->SetLongArrayRegion(resultArray, 0, length, tempBuffer);

	return resultArray;
}

JNIEXPORT jfloatArray JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_FloatVector_fp32Fetch(JNIEnv* env, jclass, jlong ptr, jint from, jint to) {
	std::vector<float_t>* vec = reinterpret_cast<std::vector<float_t>*>(ptr);

	jint length = to - from + 1;
	jfloatArray resultArray = env->NewFloatArray(length);

	/*jfloat* tempBuffer = new jfloat[length];
	for (jint i = 0; i < length; ++i) {
		tempBuffer[i] = static_cast<jfloat>((*vec)[from + i]);
	}

	env->SetFloatArrayRegion(resultArray, 0, length, tempBuffer);*/

	env->SetFloatArrayRegion(resultArray, 0, length, reinterpret_cast<const jfloat*>(&(*vec)[from]));

	return resultArray;
}

JNIEXPORT jdoubleArray JNICALL Java_io_github_seal139_jSwarm_runtime_datatype_DoubleVector_fp64Fetch(JNIEnv* env, jclass, jlong ptr, jint from, jint to) {
	std::vector<double_t>* vec = reinterpret_cast<std::vector<double_t>*>(ptr);

	jint length = to - from + 1;
	jdoubleArray resultArray = env->NewDoubleArray(length);

	jdouble* tempBuffer = new jdouble[length];
	for (jint i = 0; i < length; ++i) {
		tempBuffer[i] = static_cast<jdouble>((*vec)[from + i]);
	}

	env->SetDoubleArrayRegion(resultArray, 0, length, tempBuffer);

	return resultArray;
}