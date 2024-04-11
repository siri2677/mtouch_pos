package com.example.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.room.dao.UserInformationDAO
import com.example.data.room.entity.UserInformationEntity

@Database(entities = [UserInformationEntity::class], version = 1)
abstract class UserInformationDatabase: RoomDatabase() {
    abstract fun userInformationDao(): UserInformationDAO

    companion object {
        @Volatile private var instance: UserInformationDatabase? = null
        fun getInstance(context: Context): UserInformationDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        private fun buildDatabase(context: Context): UserInformationDatabase {
            return Room.databaseBuilder(context, UserInformationDatabase::class.java, "app_database")
                .allowMainThreadQueries()
                .build()
        }
    }
}

