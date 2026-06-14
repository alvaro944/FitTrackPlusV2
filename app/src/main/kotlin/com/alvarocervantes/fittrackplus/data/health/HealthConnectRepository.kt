package com.alvarocervantes.fittrackplus.data.health

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context)

    fun isAvailable(): Boolean = sdkStatus() == HealthConnectClient.SDK_AVAILABLE

    fun needsInstallation(): Boolean =
        sdkStatus() == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED

    suspend fun hasPermission(): Boolean {
        if (!isAvailable()) return false
        return runCatching {
            client.permissionController.getGrantedPermissions()
                .containsAll(requiredPermissions)
        }.getOrDefault(false)
    }

    fun permissionsContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    suspend fun readTodaySteps(): Long? {
        if (!hasPermission()) return null
        return runCatching {
            val now = Instant.now()
            val startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
            )
            client.readRecords(request).records.sumOf { it.count }
        }.getOrNull()
    }

    suspend fun readWeekSteps(): Map<Int, Long>? {
        if (!hasPermission()) return null
        return runCatching {
            val now = Instant.now()
            val weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(weekStart, now)
            )
            val result = mutableMapOf<Int, Long>()
            for (record in client.readRecords(request).records) {
                val dayIndex = dayIndexMondayFirst(record.startTime)
                result[dayIndex] = (result[dayIndex] ?: 0L) + record.count
            }
            result
        }.getOrNull()
    }

    suspend fun readWeekStepsForWeekStart(weekStart: LocalDate): Map<Int, Long>? {
        if (!hasPermission()) return null
        return runCatching {
            val zoneId = ZoneId.systemDefault()
            val start = weekStart.atStartOfDay(zoneId).toInstant()
            val end = weekStart.plusDays(7).atStartOfDay(zoneId).toInstant()
                .coerceAtMost(Instant.now())
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
            val result = mutableMapOf<Int, Long>()
            for (record in client.readRecords(request).records) {
                val dayIndex = dayIndexMondayFirst(record.startTime)
                result[dayIndex] = (result[dayIndex] ?: 0L) + record.count
            }
            result
        }.getOrNull()
    }

    private fun dayIndexMondayFirst(instant: Instant): Int {
        val dayOfWeek = instant.atZone(ZoneId.systemDefault()).toLocalDate().dayOfWeek
        return dayOfWeek.value - 1
    }
}
