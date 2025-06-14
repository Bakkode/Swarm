# Swarm: A Generic Java Framework for GPGPU Programming

**Swarm** is a modular, extensible framework for general-purpose GPU (GPGPU) computing in Java. It enables developers to write GPU-accelerated code directly in simplified Java syntax, which is then transpiled to CUDA, OpenCL or any other kernel code and executed on compatible hardware.

Swarm is designed to bridge the gap between the Java programming environment and high-performance GPU computing, providing both runtime abstraction and a source-to-kernel transpilation pipeline. It also includes a built-in SIMT (Single Instruction, Multiple Threads) execution simulator, allowing for CPU-based emulation of kernel logic, useful for debugging and validation.

---

## Key Features

- ✅ Write GPU kernels in a subset of **pure Java syntax**
- 🎯 Target **CUDA, OpenCL, and any other (as a plugin)** backends using platform-specific transpilation.
- 🧠 Debug and verify logic with an integrated **SIMT CPU execution simulator**
- 🧩 Modular architecture with clearly defined interfaces for extensibility
- 📋 Automatic error handling
- 🔄 Reusable memory and kernel caching
- 🧬 Custom vector data types (`FloatVector`, `IntVector`, etc.)