# CMakeList.txt : Top-level CMake project file, do global configuration
# and include sub-projects here.
#
cmake_minimum_required (VERSION 3.8)

# Enable Hot Reload for MSVC compilers if supported.
if (POLICY CMP0141)
  cmake_policy(SET CMP0141 NEW)
  set(CMAKE_MSVC_DEBUG_INFORMATION_FORMAT "$<IF:$<AND:$<C_COMPILER_ID:MSVC>,$<CXX_COMPILER_ID:MSVC>>,$<$<CONFIG:Debug,RelWithDebInfo>:EditAndContinue>,$<$<CONFIG:Debug,RelWithDebInfo>:ProgramDatabase>>")
endif()

project ("swarmsimd")

file(GLOB BENCHMARK "benchmark/*")
file(GLOB_RECURSE SRC "impl/*")

add_executable (swarmsimd 
    ${BENCHMARK}
    ${SRC}
)

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