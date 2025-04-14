package com.kwonps.data.internal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kwonps.data.internal.dao.DeviceInfoDAO
import com.kwonps.data.internal.dao.UserInformationDAO
import com.kwonps.data.internal.entity.DeviceEntity
import com.kwonps.data.internal.entity.UserEntity

@Database(entities = [UserEntity::class, DeviceEntity::class], version = 1)
abstract class DatabaseHelper: RoomDatabase() {
    abstract fun userInformationDao(): UserInformationDAO
    abstract fun deviceInfoDao(): DeviceInfoDAO

    companion object {
        @Volatile private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DatabaseHelper {
            val roomCallback = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO device_information (idx, deviceInfo) VALUES ('0', '')")
                }
            }

            return Room.databaseBuilder(context, DatabaseHelper::class.java, "app_database")
                .addCallback(roomCallback)
                .allowMainThreadQueries()
                .build()
        }
    }
}

