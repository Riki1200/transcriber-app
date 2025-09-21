import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.swift.klib)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.romeodev.transcriberFast")
            // Required when using NativeSQLiteDriver
            linkerOpts.add("-lc++")
            linkerOpts.add("-lsqlite3")

        }


        iosTarget.compilations {
            val main by getting {
                cinterops {
                    create("IosTranscriber")
                }
            }
        }

        iosTarget.compilations.getByName("main").cinterops {
            create("whisper") {
                defFile(project.file("src/iosMain/c_interop/whisper.def"))
            }
        }
    }

    swiftklib {
        create("IosTranscriber") {
            path = file("../iosApp/iosApp/Transcriber")
            packageName("com.romeodev.transcriberFast.Transcriber")
        }
    }

    sourceSets {

        androidMain.dependencies {


            implementation(project(":lib"))

            // Compose UI
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)


            // Coroutines
            implementation(libs.kotlinx.coroutines.android)


            // Dependency Injection
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            // Google Play Services
            implementation(libs.play.app.update.ktx)
            implementation(libs.play.app.review.ktx)

            // firebase
            implementation(project.dependencies.platform(libs.firebase.bom))

            // ktor
            implementation(libs.ktor.client.okhttp)

            // accompanist
            implementation(libs.accompanist.system.ui.controller)


            implementation(libs.androidx.media3.transformer)
            implementation(libs.androidx.media3.common)


        }
        commonMain.dependencies {
            // Compose Core .
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            // ktor
            implementation(libs.ktor.client.core)
            // ktor plugins
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // AndroidX Libraries
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Navigation
            implementation(libs.navigation.compose)

            // Serialization
            implementation(libs.kotlinx.serialization.json)


            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Date & Time
            implementation(libs.kotlinx.datetime)

            // RevenueCat Purchases
            implementation(libs.purchases.core)


            // Dependency Injection (Koin)
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)


            // Data Storage
            implementation(libs.datastore.preferences)
            implementation(libs.stately.common)
            implementation(libs.atomic.fu)

            // Database
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // Logging
            api(libs.logging)

            // image libs
            implementation(libs.coil.compose)
            implementation(libs.calf.file.picker)

            // firebase
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.analytics)
            implementation(libs.gitlive.firebase.auth)

            // google sign in
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.firebase)

            // backhandler
            implementation(libs.ui.backhandler)


            implementation(libs.cupertino)
            implementation(libs.cupertino.native)

            implementation(libs.cupertino.adaptive)
            implementation(libs.cupertino.icons.extended)
        }
        commonTest.dependencies {
            // Testing Framework
            implementation(libs.kotlin.test)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            // ktor
            implementation(libs.ktor.client.darwin)
        }
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
}


android {

    val keystorePropsFile = rootProject.file("composeApp/keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropsFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropsFile))
    } else {
        println("No se encontró el archivo de propiedades de keystore: $keystorePropsFile")
    }


    fun envOrProp(keyProp: String, envKey: String): String? {
        val fromProp = keystoreProperties[keyProp]?.toString()?.trim()
        if (!fromProp.isNullOrBlank()) return fromProp
        val fromEnv = System.getenv(envKey)?.trim()
        if (!fromEnv.isNullOrBlank()) return fromEnv
        return null
    }


    val ksFilePath = envOrProp("storeFile", "KEYSTORE_FILE")
    val ksStorePassword = envOrProp("storePassword", "KEYSTORE_PASSWORD")
    val ksKeyAlias = envOrProp("keyAlias", "KEY_ALIAS")
    val ksKeyPassword = envOrProp("keyPassword", "KEY_PASSWORD")


    namespace = "com.romeodev"
    compileSdk = libs.versions.android.compileSdk.get().toInt()


    defaultConfig {
        buildFeatures {
            buildConfig = true
        }
        applicationId = "com.romeodev.transcriberFast"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
        buildConfigField("boolean", "ENABLE_LOGS", "true")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }



    signingConfigs {
        create("release") {
            if (!ksFilePath.isNullOrBlank()) {
                val f = file(ksFilePath)
                if (f.exists()) {
                    storeFile = f
                }
            }
            storePassword = ksStorePassword
            keyAlias = ksKeyAlias
            keyPassword = ksKeyPassword
        }



        getByName("debug") {

            val debugKeystore = file(System.getProperty("user.home") + "/.android/debug.keystore")
            if (debugKeystore.exists()) {
                storeFile = debugKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    val releaseSigningConfig = signingConfigs.findByName("release")
    val hasValidReleaseSigning = run {
        try {
            val sf = releaseSigningConfig?.storeFile
            val sp = releaseSigningConfig?.storePassword
            val ka = releaseSigningConfig?.keyAlias
            val kp = releaseSigningConfig?.keyPassword

            logger.warn("Release signing ${sf?.absolutePath} (alias=$ka)")

            val valid = sf != null && sf.exists() && !sp.isNullOrBlank() && !ka.isNullOrBlank() && !kp.isNullOrBlank()
            if (valid) {
                logger.lifecycle("Release signing configurada correctamente: ${sf?.absolutePath} (alias=$ka)")
            } else {
                logger.warn("Release signing inválida/incompleta. storeFile: ${sf?.absolutePath ?: "null"}, alias: ${ka ?: "null"}, storePwdSet: ${!sp.isNullOrBlank()}, keyPwdSet: ${!kp.isNullOrBlank()}")
            }
            valid
        } catch (e: Exception) {
            logger.warn("Error comprobando release signing: ${e.message}")
            false
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://staging.example.com/\"")
            buildConfigField("boolean", "ENABLE_LOGS", "true")

            signingConfig = signingConfigs.findByName("debug")
        }

        release {
            /*Todo set this to true in prod*/
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
            buildConfigField("boolean", "ENABLE_LOGS", "false")

            signingConfig = if (hasValidReleaseSigning) {
                signingConfigs.getByName("release")
            } else {

                println("WARNING: No valida release keystore encontrada. Usando keystore DEBUG como fallback para release build.")
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {

    debugImplementation(compose.uiTooling)


    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}