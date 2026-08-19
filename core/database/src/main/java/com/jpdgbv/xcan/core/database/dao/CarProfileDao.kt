package com.jpdgbv.xcan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jpdgbv.xcan.core.database.entity.CarProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarProfileDao {
    @Query("SELECT * FROM car_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<CarProfileEntity>>

    @Query("SELECT * FROM car_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<CarProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfile(profile: CarProfileEntity)

    @Query("UPDATE car_profiles SET isActive = 0")
    fun deactivateAllProfiles()

    @Query("UPDATE car_profiles SET isActive = 1 WHERE id = :id")
    fun activateProfile(id: String)

    @Transaction
    fun setActiveProfile(id: String) {
        deactivateAllProfiles()
        activateProfile(id)
    }

    @Query("DELETE FROM car_profiles WHERE id = :id")
    fun deleteProfile(id: String)
}
