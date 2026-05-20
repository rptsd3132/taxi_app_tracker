package com.rptsd.app.data.repository

import com.rptsd.app.data.local.database.dao.UserRulesDao
import com.rptsd.app.data.local.database.entities.UserRulesEntity
import com.rptsd.app.domain.model.UserRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RulesRepository @Inject constructor(
    private val userRulesDao: UserRulesDao,
) {
    fun observeRules(): Flow<UserRules> =
        userRulesDao.getRulesFlow().map { it?.toDomain() ?: UserRules() }

    suspend fun saveRules(rules: UserRules) {
        userRulesDao.upsert(rules.toEntity())
    }

    suspend fun toggleAutoAccept(enabled: Boolean) {
        userRulesDao.updateAutoAcceptEnabled(enabled)
    }

    suspend fun getRules(): UserRules =
        userRulesDao.getRules()?.toDomain() ?: UserRules()
}

private fun UserRulesEntity.toDomain() = UserRules(
    minPrice = minPrice,
    maxPickupDistance = maxPickupDistance,
    workingHoursStart = workingHoursStart,
    workingHoursEnd = workingHoursEnd,
    targetApp = targetApp,
    randomSkipPercent = randomSkipPercent,
    isAutoAcceptEnabled = isAutoAcceptEnabled,
    updatedAt = updatedAt,
)

private fun UserRules.toEntity() = UserRulesEntity(
    id = 1,
    minPrice = minPrice,
    maxPickupDistance = maxPickupDistance,
    workingHoursStart = workingHoursStart,
    workingHoursEnd = workingHoursEnd,
    targetApp = targetApp,
    randomSkipPercent = randomSkipPercent,
    isAutoAcceptEnabled = isAutoAcceptEnabled,
    updatedAt = System.currentTimeMillis(),
)
