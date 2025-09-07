cmake_minimum_required(VERSION 3.10)
project(whisper_android)
set(CMAKE_CXX_STANDARD 17)

set(PROJECT_ROOT ${CMAKE_SOURCE_DIR}/../../../../../)

set(WHISPER_ROOT ${PROJECT_ROOT}/whisper.cpp)

set(WHISPER_VERSION "unknown")
if (EXISTS "${WHISPER_ROOT}/CMakeLists.txt")
    file(READ "${WHISPER_ROOT}/CMakeLists.txt" _root_cmake)
    string(REGEX MATCH "project\\(\"whisper\\.cpp\" VERSION ([0-9]+\\.[0-9]+\\.[0-9]+)\\)" _m "${_root_cmake}")
    if (CMAKE_MATCH_1)
        set(WHISPER_VERSION "${CMAKE_MATCH_1}")
    endif()
endif()
message(STATUS "Whisper version: ${WHISPER_VERSION}")


set(SOURCE_FILES
        ${WHISPER_ROOT}/src/whisper.cpp
        ${CMAKE_SOURCE_DIR}/jni.c
)

# Si NO pasas GGML_HOME desde Gradle, usa la copia local dentro de whisper.cpp
if (NOT DEFINED GGML_HOME OR GGML_HOME STREQUAL "OFF")
    set(GGML_HOME ${WHISPER_ROOT}/ggml)
endif()

# Comprobaciones mínimas para que falle “bonito”
if (NOT EXISTS ${WHISPER_ROOT}/src/whisper.cpp)
    message(FATAL_ERROR "No se encontró ${WHISPER_ROOT}/src/whisper.cpp")
endif()
if (NOT EXISTS ${GGML_HOME}/CMakeLists.txt)
    message(FATAL_ERROR "No se encontró ggml en ${GGML_HOME}. ¿Está tu submódulo/copía local?")
endif()

find_library(LOG_LIB log)
find_library(ANDROID_LIB android)

include(FetchContent)

# Declarar ggml desde un directorio local (NO lo descarga)
FetchContent_Declare(ggml SOURCE_DIR ${GGML_HOME})
FetchContent_MakeAvailable(ggml)

function(build_library target_name)
    add_library(${target_name} SHARED ${SOURCE_FILES})

    target_compile_definitions(${target_name}
            PRIVATE
            GGML_USE_CPU
            WHISPER_VERSION="${WHISPER_VERSION}"
    )

    target_compile_definitions(${target_name} PUBLIC GGML_USE_CPU)

    if (NOT ${CMAKE_BUILD_TYPE} STREQUAL "Debug")
        target_compile_options(${target_name} PRIVATE -O3 -fvisibility=hidden -fvisibility-inlines-hidden -ffunction-sections -fdata-sections)
        target_link_options(${target_name} PRIVATE -Wl,--gc-sections -Wl,--exclude-libs,ALL -flto)
    endif()

    # Flags opcionales por ABI
    if (${target_name} STREQUAL "whisper_v8fp16_va")
        target_compile_options(${target_name} PRIVATE -march=armv8.2-a+fp16)
        target_compile_options(ggml PRIVATE -march=armv8.2-a+fp16)
    elseif (${target_name} STREQUAL "whisper_vfpv4")
        target_compile_options(${target_name} PRIVATE -mfpu=neon-vfpv4)
        target_compile_options(ggml PRIVATE -mfpu=neon-vfpv4)
    endif()

    target_link_libraries(${target_name} ${LOG_LIB} ${ANDROID_LIB} ggml)

    target_include_directories(${target_name} PRIVATE
            ${WHISPER_ROOT}
            ${WHISPER_ROOT}/include
            ${WHISPER_ROOT}/src
            ${GGML_HOME}/include
            ${GGML_HOME}/src
            ${GGML_HOME}/src/ggml-cpu
    )
endfunction()

# Variantes por ABI (opcional)
if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    build_library(whisper_v8fp16_va)
elseif (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    build_library(whisper_vfpv4)
endif()

# Target genérico
build_library(whisper)


