package com.romeodev.core

object AppConstants {
     val REVENUE_CAT_API_KEY = revCatApiKey
    val GOOGLE_WEB_CLIENT_ID = googleAuthClientId
}

expect val revCatApiKey: String
expect val googleAuthClientId: String
expect val APPSTORE_URL: String