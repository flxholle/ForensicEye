package com.flxholle.forensiceye.data.sources

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.CervicalMucusRecord
import androidx.health.connect.client.records.CyclingPedalingCadenceRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.IntermenstrualBleedingRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.MenstruationFlowRecord
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OvulationTestRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SexualActivityRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsCadenceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.WheelchairPushesRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.reflect.KClass

/**
 * Data source for accessing health data using HealthConnectClient.
 *
 * @param context The application context.
 */
class HealthDataSource(context: Context) :
    KeyValueDataSource(context, isAvailable(context)) {

    /**
     * Retrieves a map of health data records.
     *
     * @return A map where the key is the record type and the value is the list of records.
     */
    @SuppressLint("NewApi")
    override fun getContentMap(): Map<String, Any> {
        if (!isAvailable(context)) {
            return emptyMap()
        }
        val healthConnectManager = HealthConnectClient.getOrCreate(context)

        val data: Map<String, Any> = runBlocking {
            val result: MutableMap<String, Any> = mutableMapOf()
            for (recordClass in getClasses()) {
                val records = readData(healthConnectManager, recordClass)
                result[recordClass.simpleName ?: "Unknown type"] = records
            }
            result
        }

        return data
    }

    override fun getFilename(): String {
        return "health_data.json"
    }

    /**
     * Returns the list of permissions required to access health data.
     * As a shortcut the classes of the records are used to get the permissions.
     *
     * @return A list of PermissionWrapper objects.
     */
    override fun getPermissions(): List<PermissionWrapper> {
        val returnList = mutableListOf<PermissionWrapper>()
        for (recordClass in getClasses()) {
            returnList.add(PermissionWrapper(HealthPermission.getReadPermission(recordClass)))
        }
        returnList.add(PermissionWrapper("android.permission.health.READ_HEALTH_DATA_HISTORY"))
        return returnList
    }

    /**
     * Reads health data records of a specific type.
     *
     * @param T The type of the health data record.
     * @param healthConnectClient The HealthConnectClient instance.
     * @param recordType The class of the health data record.
     * @return A list of health data records.
     */
    private suspend fun <T : Record> readData(
        healthConnectClient: HealthConnectClient,
        recordType: KClass<T>
    ): List<T> {
        // Check if feature is available, otherwise no history longer than 30 days
        val historyPermission =
            PermissionWrapper("android.permission.health.READ_HEALTH_DATA_HISTORY")
        val backlog = if (!historyPermission.isGranted(context)) {
            SHORT_BACKLOG
        } else {
            LONG_BACKLOG
        }

        val request = ReadRecordsRequest(
            recordType = recordType,
            timeRangeFilter = TimeRangeFilter.after(
                LocalDateTime.now().minusDays(backlog)
            ),
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    /**
     * Returns a list of all health data record classes.
     *
     * @return A list of KClass objects representing health data record types.
     */
    private fun getClasses(): List<KClass<out Record>> {
        return listOf(
            ActiveCaloriesBurnedRecord::class,
            BasalBodyTemperatureRecord::class,
            BasalMetabolicRateRecord::class,
            BloodGlucoseRecord::class,
            BloodPressureRecord::class,
            BodyFatRecord::class,
            BodyTemperatureRecord::class,
            BodyWaterMassRecord::class,
            BoneMassRecord::class,
            CervicalMucusRecord::class,
            CyclingPedalingCadenceRecord::class,
            DistanceRecord::class,
            ElevationGainedRecord::class,
            ExerciseSessionRecord::class,
            FloorsClimbedRecord::class,
            HeartRateRecord::class,
            HeartRateVariabilityRmssdRecord::class,
            HeightRecord::class,
            HydrationRecord::class,
            IntermenstrualBleedingRecord::class,
            LeanBodyMassRecord::class,
            MenstruationFlowRecord::class,
            MenstruationPeriodRecord::class,
            NutritionRecord::class,
            OvulationTestRecord::class,
            OxygenSaturationRecord::class,
            PowerRecord::class,
            RespiratoryRateRecord::class,
            RestingHeartRateRecord::class,
            SexualActivityRecord::class,
            SleepSessionRecord::class,
            SpeedRecord::class,
            StepsCadenceRecord::class,
            StepsRecord::class,
            TotalCaloriesBurnedRecord::class,
            Vo2MaxRecord::class,
            WeightRecord::class,
            WheelchairPushesRecord::class
        )
    }

    companion object {
        const val SHORT_BACKLOG: Long = 30 // days
        const val LONG_BACKLOG: Long = 365 // days

        /**
         * Checks if the HealthConnectClient is available on the phone.
         *
         * @param context The application context.
         * @return True if available, false otherwise.
         */
        private fun isAvailable(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return false
            }

            val availabilityStatus = HealthConnectClient.getSdkStatus(context)
            return !(availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE || availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED)
        }
    }
}