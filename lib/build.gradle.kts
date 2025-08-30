plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.romeodev.lib"
    compileSdk = 36



    externalNativeBuild {
        cmake {
            path = File("src/main/jni/whisper/CMakeLists.txt")
        }
    }
    ndkVersion = "26.1.10909125"


    defaultConfig {
        minSdk = 24



        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        externalNativeBuild {
            cmake {

                arguments += listOf("-DGGML_HOME=${rootDir}/ggml")

                if (
                    project.hasProperty("GGML_HOME") &&
                    project.findProperty("GGML_CLBLAST") == "ON"

                ) {
                    // Turning on CLBlast requires GGML_HOME
                    arguments += listOf("-DGGML_HOME=${project.property("GGML_HOME")}",
                    "-DGGML_CLBLAST=ON",
                    "-DOPENCL_LIB=${project.property("OPENCL_LIB")}",
                    "-DCLBLAST_HOME=${project.property("CLBLAST_HOME")}",
                    "-DOPENCL_ROOT=${project.property("OPENCL_ROOT")}",
                    "-DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=BOTH",
                    "-DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=BOTH")
                } else if (project.hasProperty("GGML_HOME")) {
                    arguments += listOf("-DGGML_HOME=${project.property("GGML_HOME")}")
                }

            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

}