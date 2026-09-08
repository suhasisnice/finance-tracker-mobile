package com.finsetpacer.app.data.repository

import com.finsetpacer.app.data.db.SettingsDao
import com.finsetpacer.app.data.db.SettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    fun observeSettings(): Flow<SettingsEntity?> = settingsDao.observe()

    suspend fun getSettings(): SettingsEntity? = settingsDao.get()

    suspend fun save(settings: SettingsEntity) = settingsDao.upsert(settings)
}
