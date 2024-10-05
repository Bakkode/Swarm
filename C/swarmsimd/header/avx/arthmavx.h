#include "../vector.h"

#ifndef DllExport
#ifdef _WIN32
#define DllExport __declspec( dllexport )
#else
#define DllExport __attribute__((visibility("default")))
#endif
#endif //DllExport

#ifndef ARTHMAVX_H_INCLUDED
#define ARTHMAVX_H_INCLUDED

// Addition
DllExport void addScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2);

DllExport void addVectorFp32256(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2);

DllExport void addMixedFp32256(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar);

// Substration
DllExport void subScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2);

DllExport void subVectorFp32256(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2);

DllExport void subMixedFp32256(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar);

// Multiplication
DllExport void mulScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2);

DllExport void mulVectorFp32256(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2);

DllExport void mulMixedFp32256(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar);

// Division
DllExport void divScalarFp32256(SimdVector* result, SimdVector* scalar1, SimdVector* scalar2);

DllExport void divVectorFp32256(SimdVector* result, int threadIndex, SimdVector* vector1, SimdVector* vector2);

DllExport void divMixedFp32256(SimdVector* result, int threadIndex, SimdVector* vector, SimdVector* scalar);

#endif // ARTHMAVX_H_INCLUDED