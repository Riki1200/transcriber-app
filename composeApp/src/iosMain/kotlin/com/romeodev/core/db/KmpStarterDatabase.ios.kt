package com.romeodev.core.db

import androidx.room.RoomDatabaseConstructor

@Suppress(names = ["NO_ACTUAL_FOR_EXPECT"])
actual object KmpStarterDatabaseConstructor :
    RoomDatabaseConstructor<KmpStarterDatabase> {
    actual override fun initialize(): KmpStarterDatabase {
        TODO("Not yet implemented")
    }
}