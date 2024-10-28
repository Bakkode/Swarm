# CMakeList.txt : Top-level CMake project file, do global configuration
# and include sub-projects here.
#
cmake_minimum_required (VERSION 3.18)

project ("swarmsimd" LANGUAGES CXX CUDA)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CUDA_STANDARD 17)

# Specify the CUDA toolkit location (optional, usually not needed if installed correctly)
if(WIN32)
    set(CUDA_TOOLKIT_ROOT_DIR "C:/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v12.6")
    set(LIB_DIR "${CUDA_TOOLKIT_ROOT_DIR}/lib/x64/")
    set(CMAKE_CUDA_COMPILER "${CUDA_TOOLKIT_ROOT_DIR}/bin/nvcc.exe")
elseif(UNIX)
    set(CMAKE_CUDA_COMPILER "/usr/local/cuda/bin/nvcc")
endif()


file(GLOB_RECURSE LIB_FILES "${LIB_DIR}/*.lib" "${LIB_DIR}/*.a" "${LIB_DIR}/*.so")


# Find CUDA package (not always necessary, but can be useful)
find_package(CUDA REQUIRED)

# Include directories
include_directories(${CUDA_INCLUDE_DIRS})

# Enable Hot Reload for MSVC compilers if supported.
if (POLICY CMP0141)
  cmake_policy(SET CMP0141 NEW)
  set(CMAKE_MSVC_DEBUG_INFORMATION_FORMAT "$<IF:$<AND:$<C_COMPILER_ID:MSVC>,$<CXX_COMPILER_ID:MSVC>>,$<$<CONFIG:Debug,RelWithDebInfo>:EditAndContinue>,$<$<CONFIG:Debug,RelWithDebInfo>:ProgramDatabase>>")
endif()

file(GLOB BENCHMARK "benchmark/*")
file(GLOB_RECURSE SRC "impl/*")
file(GLOB_RECURSE HDR "header/*")

message(STATUS "Source files: ${SRC}")
message(STATUS "Source files: ${BENCHMARK}")
message(STATUS "Source files: ${HDR}")

add_executable (swarmsimd 
    ${HDR}
    ${BENCHMARK}
    ${SRC}
)

target_link_libraries(swarmsimd ${LIB_FILES})


if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET swarmsimd PROPERTY CXX_STANDARD 20)
endif()

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    # For Release builds, enable /O2 and AVX2
    target_compile_options(swarmsimd PRIVATE
        $<$<CXX_COMPILER_ID:MSVC>:/O2 /arch:AVX2>
        $<$<CXX_COMPILER_ID:GNU>:-O2 -mavx2>
        $<$<CXX_COMPILER_ID:Clang>:-O2 -mavx2>
    )
elseif (CMAKE_BUILD_TYPE STREQUAL "Debug")
    # For Debug builds, enable /RTC1 but do NOT use optimization /O2
    target_compile_options(swarmsimd PRIVATE
        $<$<CXX_COMPILER_ID:MSVC>:/RTC1 /arch:AVX2>
    )
endif()