#include "../vector.h"

#ifndef TRNFRAVX_H_INCLUDED
#define TRNFRAVX_H_INCLUDED

#ifndef DllExport
#ifdef _WIN32
#define DllExport __declspec( dllexport )
#else
#define DllExport __attribute__((visibility("default")))
#endif
#endif //DllExport

DllExport SimdVector* createScalarFp32256(float value);

DllExport SimdVector* createVectorFp32256(int totalThreads, int initialChunkSize, float incFactor);

DllExport SimdVector* deriveVectorFp32256(SimdVector* reference);

DllExport void destroyFp32256(SimdVector* vector);

DllExport int appendFp32256(SimdVector* vector, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7);

DllExport float getFp32256(SimdVector* vector, long long int index);

DllExport int setFp32256(SimdVector* vector, long long int index, float newValue);

DllExport long long int getSizeFp32256(SimdVector* vector);

#endif // TRNFRAVX_H_INCLUDED