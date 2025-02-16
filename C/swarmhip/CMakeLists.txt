# CMakeList.txt : Top-level CMake project file, do global configuration
# and include sub-projects here.
#
cmake_minimum_required (VERSION 3.18)

project ("swarmhip" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)

if(WIN32)
    #AMD HIP - ROCm
    set(ROCM_TOOLKIT_ROOT_DIR "C:/Program Files/AMD/ROCm/6.2/")
    set(ROCM_DIR "${ROCM_TOOLKIT_ROOT_DIR}/lib/")
    file(GLOB_RECURSE LIB_ROCM "${ROCM_DIR}/*.lib" "${ROCM_DIR}/*.a" "${ROCM_DIR}/*.so")
    include_directories(${ROCM_TOOLKIT_ROOT_DIR}/include)
elseif(UNIX)

endif()

find_package(Java REQUIRED) # Java
find_package(JNI REQUIRED)  # JNI

include_directories(${JNI_INCLUDE_DIRS})

file(GLOB_RECURSE SRC "impl/*")
file(GLOB_RECURSE HDR "header/*")

add_library (swarmhip SHARED
    ${HDR}
    ${SRC}
)

target_link_libraries(swarmhip ${LIB_ROCM})

target_compile_options(swarmhip PRIVATE
    $<$<CXX_COMPILER_ID:MSVC>:/O2>
    $<$<CXX_COMPILER_ID:GNU>:-O2>
    $<$<CXX_COMPILER_ID:Clang>:-O2>
)