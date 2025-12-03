package com.kwonps.data.source.user

import com.kwonps.data.common.dispatcher.DispatcherProvider
import com.kwonps.data.remote.DataFormat
import com.kwonps.data.remote.apiservice.TmsAPIService
import com.kwonps.data.remote.dto.request.RequestUser
import com.kwonps.data.remote.dto.response.ResponseUser
import javax.inject.Inject
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Remote source for user login validation. No caching beyond the persistence handled by
 * [UserLocalDataSource].
 */
class UserRemoteDataSource @Inject constructor(
    private val apiService: TmsAPIService,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun validateLogin(request: RequestUser.Login): Response<ResponseUser.Login> =
        withContext(dispatcherProvider.io) { apiService.key(DataFormat(request)) }
}
