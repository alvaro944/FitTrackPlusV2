package com.alvarocervantes.fittrackplus.data.health

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
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
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val request = AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, Instant.now())
            )
            client.aggregate(request)[StepsRecord.COUNT_TOTAL] ?: 0L
        }.getOrNull()
    }

    suspend fun readWeekSteps(): Map<Int, Long>? =
        readDaySteps(weekStart = currentWeekMonday(), localEnd = LocalDateTime.now())

    suspend fun readWeekStepsForWeekStart(weekStart: LocalDate): Map<Int, Long>? =
        readDaySteps(weekStart = weekStart, localEnd = weekStart.plusDays(7).atStartOfDay())

    /**
     * Buckets steps by calendar day using Health Connect's own aggregation engine
     * instead of manually summing raw records by startTime. A single StepsRecord
     * can span across midnight (e.g. 23:40-00:20), and naive bucketing by startTime
     * would attribute its full count to the wrong day. aggregateGroupByPeriod splits
     * each day-sized bucket correctly at the boundary.
     */
    private suspend fun readDaySteps(weekStart: LocalDate, localEnd: LocalDateTime): Map<Int, Long>? {
        if (!hasPermission()) return null
        return runCatching {
            val localStart = weekStart.atStartOfDay()
            val cappedEnd = localEnd.coerceAtMost(LocalDateTime.now())
            val request = AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(localStart, cappedEnd),
                timeRangeSlicer = Period.ofDays(1)
            )
            val result = mutableMapOf<Int, Long>()
            for (bucket in client.aggregateGroupByPeriod(request)) {
                val dayIndex = bucket.startTime.dayOfWeek.value - 1
                result[dayIndex] = bucket.result[StepsRecord.COUNT_TOTAL] ?: 0L
            }
            result
        }.getOrNull()
    }

    private fun currentWeekMonday(): LocalDate = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
