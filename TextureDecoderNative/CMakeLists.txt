# Set the minimum version of CMake that can be used
cmake_minimum_required (VERSION 3.15)

# Set the project name
project("TextureDecoder" LANGUAGES CXX)

# Add the given directories to those the compiler uses to search for include files
include_directories("C:/Program Files/Java/jdk-17.0.12+7/include")
include_directories("C:/Program Files/Java/jdk-17.0.12+7/include/win32")
include_directories(./lib/crunch)
include_directories(./lib/fp16)
include_directories(./lib/unitycrunch)

# Generate the shared library from the library sources
add_library(TextureDecoder SHARED
        lib/crunch/crn_decomp.h
        lib/crunch/crnlib.h
        lib/fp16/bitcasts.h
        lib/fp16/fp16.h
        lib/unitycrunch/crn_decomp.h
        lib/unitycrunch/crn_defs.h
        lib/unitycrunch/crnlib.h
        lib/astc.cpp
        lib/astc.h
        lib/atc.cpp
        lib/atc.h
        lib/bcn.cpp
        lib/bcn.h
        lib/bool32_t.h
        lib/color.h
        lib/crunch.cpp
        lib/crunch.h
        lib/dllexport.h
        lib/dllmain.cpp
        lib/endianness.h
        lib/etc.cpp
        lib/etc.h
        lib/fp16.h
        lib/pvrtc.cpp
        lib/pvrtc.h
        lib/resource.h
        lib/unitycrunch.cpp
        lib/unitycrunch.h
        io_github_deficuet_unitykt_extension_TextureDecoder.h
        io_github_deficuet_unitykt_extension_TextureDecoder.cc)

set_target_properties(TextureDecoder PROPERTIES
    CXX_STANDARD 14
    CXX_STANDARD_REQUIRED YES
    MSVC_RUNTIME_LIBRARY "MultiThreadedDLL"
)

target_compile_definitions(TextureDecoder PRIVATE
    _T2D_DLL
    TEXTURE2DDECODERNATIVE_EXPORTS
    $<$<CONFIG:Release>:NDEBUG>
)
