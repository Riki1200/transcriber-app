package com.romeodev.core

import com.romeodev.BuildConfig

actual val APPSTORE_URL =
    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
actual val googleAuthClientId: String
    get() = "528440462855-066t34061k51qatmgfff2jev7srsa6gl.apps.googleusercontent.com"