# CMakeList.txt : Top-level CMake project file, do global configuration
# and include sub-projects here.

cmake_minimum_required (VERSION 3.18)

project ("swarmcu" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)

find_package(CUDAToolkit REQUIRED) # Cuda
find_package(Java REQUIRED)        # Java
find_package(JNI REQUIRED)         # JNI

include_directories(${JNI_INCLUDE_DIRS}) # JNI - Include DIR

file(GLOB_RECURSE HDR "header/*")  # Source code - Include DIR
file(GLOB_RECURSE SRC "impl/*")    # Source code - Implementation DIR

add_library (swarmcu SHARED
    ${HDR}
    ${SRC}
)

target_link_libraries(swarmcu CUDA::cuda_driver CUDA::nvrtc)

target_compile_options(swarmcu PRIVATE
    $<$<CXX_COMPILER_ID:MSVC>:/O2>
    $<$<CXX_COMPILER_ID:GNU>:-O2>
    $<$<CXX_COMPILER_ID:Clang>:-O2>
)