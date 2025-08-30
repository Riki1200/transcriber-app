cmake_minimum_required(VERSION 3.22)
project(whisper_multi)
set(CMAKE_CXX_STANDARD 17)

# --- RUTAS CANDIDATAS ---
# A) Amalgamado local (tu caso KMM)
set(LOCAL_AMALGAMATED ${CMAKE_CURRENT_SOURCE_DIR}/../../whispercpp/whisper.cpp)

# B) Repo completo: permite override por -DWHISPER_HOME=/ruta/al/repo
set(WHISPER_HOME "" CACHE PATH "Path to whisper.cpp repository (optional)")

# --- FUNCIÓN: buscar repo hacia arriba si no se pasó WHISPER_HOME ---
function(find_whisper_repo OUT_VAR)
    if (WHISPER_HOME AND EXISTS "${WHISPER_HOME}/CMakeLists.txt")
        set(${OUT_VAR} "${WHISPER_HOME}" PARENT_SCOPE)
        return()
    endif()

    # subidas desde el módulo Android
    set(_dir "${CMAKE_SOURCE_DIR}")
    set(_found "")
    foreach(_i RANGE 0 6)  # sube hasta 6 niveles
        if (EXISTS "${_dir}/CMakeLists.txt")
            file(READ "${_dir}/CMakeLists.txt" _root_cmake)
            string(REGEX MATCH "project\\(\"whisper\\.cpp\"" _match "${_root_cmake}")
            if (_match)
                set(_found "${_dir}")
                break()
            endif()
        endif()
        get_filename_component(_dir "${_dir}" DIRECTORY)
    endforeach()

    if (_found)
        set(${OUT_VAR} "${_found}" PARENT_SCOPE)
    else()
        set(${OUT_VAR} "" PARENT_SCOPE)
    endif()
endfunction()

# --- DETECCIÓN FUENTE ---
set(USE_AMALGAMATED OFF)
if (EXISTS "${LOCAL_AMALGAMATED}")
    set(USE_AMALGAMATED ON)
else()
    find_whisper_repo(WHISPER_ROOT)
endif()

# --- LEER VERSION (si hay repo) ---
set(WHISPER_VERSION "unknown")
if (NOT USE_AMALGAMATED AND WHISPER_ROOT)
    if (EXISTS "${WHISPER_ROOT}/CMakeLists.txt")
        file(READ "${WHISPER_ROOT}/CMakeLists.txt" MAIN_CMAKE_CONTENT)
        string(REGEX MATCH "project\\(\"whisper\\.cpp\" VERSION ([0-9]+\\.[0-9]+\\.[0-9]+)\\)" _m "${MAIN_CMAKE_CONTENT}")
        if (CMAKE_MATCH_1)
            set(WHISPER_VERSION "${CMAKE_MATCH_1}")
        endif()
    endif()
endif()

message(STATUS "=== whisper.cpp source ===")
message(STATUS "  USE_AMALGAMATED: ${USE_AMALGAMATED}")
if (USE_AMALGAMATED)
    message(STATUS "  Amalgamated: ${LOCAL_AMALGAMATED}")
else()
    message(STATUS "  Repo root:   ${WHISPER_ROOT}")
    message(STATUS "  Version:     ${WHISPER_VERSION}")
endif()

# --- LIBRERÍAS DEL SISTEMA ---
find_library(LOG_LIB log)
find_library(ANDROID_LIB android)

# --- FUNCION GENERICA PARA CREAR VARIANTES ---
function(build_whisper_variant target_name)
    if (USE_AMALGAMATED)
        # Compila amalgamado + tu JNI
        add_library(${target_name} SHARED
                ${CMAKE_CURRENT_SOURCE_DIR}/whisper_jni.cpp
                ${LOCAL_AMALGAMATED}
        )
        target_include_directories(${target_name} PRIVATE
                ${CMAKE_CURRENT_SOURCE_DIR}/../../whispercpp
        )
        target_compile_definitions(${target_name} PRIVATE
                GGML_USE_CPU
                WHISPER_VERSION="${WHISPER_VERSION}"
        )
    else()
        # Compila contra el repo: usa el whisper.cpp de src/
        set(WHISPER_SRC ${WHISPER_ROOT}/src/whisper.cpp)
        add_library(${target_name} SHARED
                ${CMAKE_CURRENT_SOURCE_DIR}/whisper_jni.cpp
                ${WHISPER_SRC}
        )

        # ggml del repo (como el ejemplo, pero sin descargar nada)
        include(FetchContent)
        FetchContent_Declare(ggml SOURCE_DIR ${WHISPER_ROOT}/ggml)
        FetchContent_MakeAvailable(ggml)

        target_link_libraries(${target_name} ggml)
        target_include_directories(${target_name} PRIVATE
                ${WHISPER_ROOT}
                ${WHISPER_ROOT}/src
                ${WHISPER_ROOT}/include
                ${WHISPER_ROOT}/ggml/include
                ${WHISPER_ROOT}/ggml/src
                ${WHISPER_ROOT}/ggml/src/ggml-cpu
        )
        target_compile_definitions(${target_name} PRIVATE
                GGML_USE_CPU
                WHISPER_VERSION="${WHISPER_VERSION}"
        )
    endif()

    # Optimizaciones típicas (Release)
    if (NOT ${CMAKE_BUILD_TYPE} STREQUAL "Debug")
        target_compile_options(${target_name} PRIVATE -O3 -ffast-math -fvisibility=hidden -fvisibility-inlines-hidden -ffunction-sections -fdata-sections)
        target_link_options(${target_name}    PRIVATE -Wl,--gc-sections -Wl,--exclude-libs,ALL)
    endif()

    target_link_libraries(${target_name} ${LOG_LIB} ${ANDROID_LIB})
endfunction()

# --- SI QUIERES IGUAL QUE EL EJEMPLO: 3 LIBS + FLAGS POR-ABI ---
# Lib por defecto (siempre)
build_whisper_variant(whisper)

# Variantes por ABI
if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    build_whisper_variant(whisper_v8fp16_va)
    target_compile_options(whisper_v8fp16_va PRIVATE -march=armv8.2-a+fp16)
elseif (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    build_whisper_variant(whisper_vfpv4)
    target_compile_options(whisper_vfpv4 PRIVATE -mfpu=neon-vfpv4)
endif()
