# Swarm
Welcome to my final bachelor project.

My aim for this framework is to utilize CUDA and OpenCL (primarily) and HIP (will be added later) to be used by Java for GPGPU parallel computing.
Main feature of this framework is
1. Extensible backend (Develop new one as needed)
2. Java native to kernel transpiler (using Java reflection and decompiler, then build a valid kernel code from that)
3. Native Vector (Works like C++ vector or pointer)
4. Java-native kernel debugging
