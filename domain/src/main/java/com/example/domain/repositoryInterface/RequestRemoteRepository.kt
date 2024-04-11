package com.example.domain.repositoryInterface

import com.example.domain.model.request.RequestModel
import com.example.domain.model.response.ResponseModel
import kotlinx.coroutines.flow.MutableStateFlow

interface RequestRemoteRepository {
    operator fun invoke(
        responseModel: MutableStateFlow<ResponseModel>,
        requestModel: RequestModel
    )
}