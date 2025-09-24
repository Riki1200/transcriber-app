package com.romeodev.core

import com.romeodev.BuildConfig

actual val APPSTORE_URL =
    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
actual val googleAuthClientId: String
    get() = "558815437752-4j0m4jc4tegei70bc1g06mgrsgb2bu3t.apps.googleusercontent.com"

actual val revCatApiKey: String
    get() = "goog_RzmcFJgtEruXYeEANZviMWfdLGk"