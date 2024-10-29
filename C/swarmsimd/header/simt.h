#ifndef SIMT_H_INCLUDED
#define SIMT_H_INCLUDED

#include <string>

typedef struct {
    std::string deviceName;

    int processorCount;
    long maxMemory;

    int maxDimXIndex;
    int maxDimYIndex;
    int maxDimZIndex;

    int maxThreadsPer3DNode;

    float ComputationPower;
} DeviceInfo;


#endif // SIMT_H_INCLUDED