package com.example.data.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.UserInformationDao
import com.example.data.entity.db.UserInformationEntity

@Database(entities = [UserInformationEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userInformationDao(): UserInformationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .allowMainThreadQueries()
                .build()
        }
    }
}