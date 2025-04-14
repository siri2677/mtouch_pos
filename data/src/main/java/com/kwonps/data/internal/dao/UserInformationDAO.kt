package com.kwonps.data.internal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kwonps.data.internal.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInformationDAO {
    @Query("SELECT * FROM user_information WHERE tmnId = :tmnId")
    fun getUserInformation(tmnId: String): UserEntity

    @Query("SELECT * FROM user_information")
    fun getAllUserInformation(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserInformation(userInformationEntity: UserEntity)

    @Query("DELETE FROM user_information WHERE tmnId = :tmnId")
    fun deleteUserInformation(tmnId: String)
}