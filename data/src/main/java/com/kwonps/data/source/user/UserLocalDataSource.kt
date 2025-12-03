package com.kwonps.data.source.user

import android.content.SharedPreferences
import com.kwonps.data.internal.dao.UserInformationDAO
import com.kwonps.data.internal.entity.UserEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Local persistence for user metadata. Cache policy: SharedPreferences keeps the last
 * successful login payload, Room keeps user detail history. Synchronization policy:
 * operations are scoped to caller threads; Room emits flow updates for downstream
 * subscribers.
 */
class UserLocalDataSource @Inject constructor(
    private val dao: UserInformationDAO,
    private val sharedPreferences: SharedPreferences,
    private val preferenceKey: String
) {
    fun getCurrentLoginUserInformation(): String? = sharedPreferences.getString(preferenceKey, null)

    fun setCurrentLoginUserInformation(responseLoginModelString: String) {
        sharedPreferences.edit().putString(preferenceKey, responseLoginModelString).apply()
    }

    fun insertUserInformation(userInfo: UserEntity) {
        dao.insertUserInformation(userInfo)
    }

    fun getAllUserInformation(): Flow<List<UserEntity>> = dao.getAllUserInformation()

    fun deleteUserInformation(tmnId: String) {
        dao.deleteUserInformation(tmnId)
    }
}
