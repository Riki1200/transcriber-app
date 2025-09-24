package com.romeodev.core.utils.platform

enum class PlatformType {
    IOS,
    ANDROID;



    val isIos: Boolean get() = this == IOS
    val isAndroid: Boolean get() = this == ANDROID
}


expect val platformType:PlatformType