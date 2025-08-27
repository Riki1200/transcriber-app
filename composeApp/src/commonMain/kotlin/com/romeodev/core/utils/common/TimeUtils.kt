package com.romeodev.core.utils.common

import kotlin.time.Clock
import kotlin.time.ExperimentalTime


fun epochMillis(): Long = currentMillis()

@OptIn(ExperimentalTime::class)
fun currentMillis() = Clock.System.now().toEpochMilliseconds()

val currentMillis: Long get() = currentMillis()

fun hoursToMillis(hour: Int) = hour * 60 * 60 * 1000