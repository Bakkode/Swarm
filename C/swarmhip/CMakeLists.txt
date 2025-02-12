# CMakeList.txt : Top-level CMake project file, do global configuration
# and include sub-projects here.
#
cmake_minimum_required (VERSION 3.18)

project ("swarmhip" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_HIP_STANDARD 17)

# Add the source files

# Specify the CUDA toolkit location (optional, usually not needed if installed correctly)
if(WIN32)
    # AMD HIP - ROCm
    set(ROCM_TOOLKIT_ROOT_DIR "C:/Program Files/AMD/ROCm/6.2/")
    set(ROCM_DIR "${ROCM_TOOLKIT_ROOT_DIR}/lib/")

    # Java
    set(JAVA_ROOT_DIR "C:/Program Files/OpenLogic/jdk-17.0.13.11-hotspot/")
    set(JAVA_DIR "${JAVA_ROOT_DIR}/lib/")
elseif(UNIX)

endif()

file(GLOB_RECURSE LIB_ROCM "${ROCM_DIR}/*.lib" "${ROCM_DIR}/*.a" "${ROCM_DIR}/*.so")
file(GLOB_RECURSE LIB_JAVA "${JAVA_DIR}/*.lib" "${JAVA_DIR}/*.a" "${JAVA_DIR}/*.so")

# Find CUDA package (not always necessary, but can be useful)
find_package(ROCmCMakeBuildTools)
find_package(ROCM) # deprecated, but included for backwards compatibility
find_package(Java REQUIRED)

# Include directories
include_directories(${JAVA_ROOT_DIR}/include)
include_directories(${JAVA_ROOT_DIR}/include/win32)
include_directories(${ROCM_TOOLKIT_ROOT_DIR}/include)

# Enable Hot Reload for MSVC compilers if supported.
if (POLICY CMP0141)
  cmake_policy(SET CMP0141 NEW)
  set(CMAKE_MSVC_DEBUG_INFORMATION_FORMAT "$<IF:$<AND:$<C_COMPILER_ID:MSVC>,$<CXX_COMPILER_ID:MSVC>>,$<$<CONFIG:Debug,RelWithDebInfo>:EditAndContinue>,$<$<CONFIG:Debug,RelWithDebInfo>:ProgramDatabase>>")
endif()

file(GLOB_RECURSE SRC "impl/*")
file(GLOB_RECURSE HDR "header/*")

message(STATUS "Source files: ${SRC}")
message(STATUS "Source files: ${HDR}")

add_library (swarmhip SHARED
    ${HDR}
    ${SRC}
)

target_link_libraries(swarmhip ${LIB_ROCM} ${LIB_JAVA})


if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET swarmhip PROPERTY CXX_STANDARD 20)
endif()

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    # For Release builds, enable /O2 and AVX2
    target_compile_options(swarmhip PRIVATE
        $<$<CXX_COMPILER_ID:MSVC>:/O2 /arch:AVX2>
        $<$<CXX_COMPILER_ID:GNU>:-O2 -mavx2>
        $<$<CXX_COMPILER_ID:Clang>:-O2 -mavx2>
    )
elseif (CMAKE_BUILD_TYPE STREQUAL "Debug")
    # For Debug builds, enable /RTC1 but do NOT use optimization /O2
    target_compile_options(swarmhip PRIVATE
        $<$<CXX_COMPILER_ID:MSVC>:/RTC1 /arch:AVX2>
    )
endif()