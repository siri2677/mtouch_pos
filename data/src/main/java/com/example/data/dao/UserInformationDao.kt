package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.db.UserInformationEntity

@Dao
interface UserInformationDao {
    @Query("SELECT * FROM user_information WHERE tmnId = :tmnId")
    suspend fun getUserInformation(tmnId: String): UserInformationEntity

    @Query("SELECT * FROM user_information")
    fun getAllUserInformation(): List<UserInformationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserInformation(userInformationEntity: UserInformationEntity)

    @Query("DELETE FROM user_information WHERE tmnId = :tmnId")
    fun deleteUserInformation(tmnId: String)
}