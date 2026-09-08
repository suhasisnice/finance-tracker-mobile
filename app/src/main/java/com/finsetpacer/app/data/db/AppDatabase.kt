package com.finsetpacer.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SettingsEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DB_NAME = "finset_pacer.db"
    }
}
