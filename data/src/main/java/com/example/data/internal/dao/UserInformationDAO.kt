package com.example.data.internal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.internal.entity.UserInformationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInformationDAO {
    @Query("SELECT * FROM user_information WHERE tmnId = :tmnId")
    fun getUserInformation(tmnId: String): UserInformationEntity

    @Query("SELECT * FROM user_information")
    fun getAllUserInformation(): Flow<List<UserInformationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserInformation(userInformationEntity: UserInformationEntity)

    @Query("DELETE FROM user_information WHERE tmnId = :tmnId")
    fun deleteUserInformation(tmnId: String)
}