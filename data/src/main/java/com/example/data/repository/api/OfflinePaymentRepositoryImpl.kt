package com.example.data.repository.api

import android.util.Log
import com.example.data.entity.api.request.tms.RequestCancelPaymentEntity
import com.example.data.entity.api.request.tms.RequestInsertPaymentDataEntity
import com.example.data.entity.api.request.tms.RequestPaymentEntity
import com.example.data.mapper.api.RequestDataMapper
import com.example.data.mapper.api.ResponseDataMapper
import com.example.data.retrofitInterface.ApiServiceRepositoryImpl
import com.example.data.util.KsnetUtils
import com.example.domain.dto.request.tms.RequestCancelPaymentDTO
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.dto.request.tms.RequestPaymentDTO
import com.example.domain.dto.response.tms.ResponseCancelPaymentDTO
import com.example.domain.dto.response.tms.ResponseInsertPaymentDataDTO
import com.example.domain.dto.response.tms.ResponsePaymentDTO
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.ksnet.interfaces.Approval
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onException
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.ResponseBody.Companion.asResponseBody
import org.mapstruct.factory.Mappers
import java.nio.charset.StandardCharsets

class OfflinePaymentRepositoryImpl: OfflinePaymentRepository {
    override fun approve(
        onSuccess: (ResponsePaymentDTO) -> Unit,
        onError: (String) -> Unit,
        body: RequestPaymentDTO
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            flow<Unit> {
                ApiServiceRepositoryImpl().getAPIService().rule(
                    body.token,
                    RequestPaymentEntity(
                        Mappers.getMapper(RequestDataMapper::class.java).paymentDtoToEntity(body)
                    )
                ).suspendOnSuccess {
                    onSuccess(
                        Mappers.getMapper(ResponseDataMapper::class.java)
                            .paymentEntityToDto(data.data!!)
                    )
                }.suspendOnError {
                    onError(String(response.errorBody()!!.bytes(), StandardCharsets.UTF_8))
                }.suspendOnException {
                    onError(message!!)
                }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    override fun refund(
        onSuccess: (ResponseCancelPaymentDTO) -> Unit,
        onError: (String) -> Unit,
        body: RequestCancelPaymentDTO
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            flow<Unit> {
                ApiServiceRepositoryImpl().getAPIService().crule(
                    body.token,
                    RequestCancelPaymentEntity(
                        Mappers.getMapper(RequestDataMapper::class.java)
                            .paymentCancelDtoToEntity(body)
                    )
                ).suspendOnSuccess {
                    onSuccess(
                        Mappers.getMapper(ResponseDataMapper::class.java)
                            .cancelPaymentEntityToDto(data.data!!)
                    )
                }.onError() {
                    onError(String(response.errorBody()!!.bytes(), StandardCharsets.UTF_8))
                }.onException {
                    onError(message!!)
                }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    override fun push(
        onSuccess: (ResponseInsertPaymentDataDTO) -> Unit,
        onError: (String) -> Unit,
        body: RequestInsertPaymentDataDTO,
        requestTelegram: ByteArray
    ) {
        val responseTelegram = ByteArray(2048)
        val rtn: Int = Approval().request(
            "210.181.28.137",
            9562,
            5,
            requestTelegram,
            responseTelegram,
            16000
        )

        KsnetUtils().reqDataPrint(requestTelegram)
        KsnetUtils().respGetHashData(responseTelegram)

        if (rtn < 0 && rtn != -102 && rtn != -103 && rtn != -104) {
            Log.w("threadAdmmision", "threadAdmmision")
        }
        if(byteToString(responseTelegram, 40, 1) == "X") {
            onError(byteToString(responseTelegram, 62, 16) + "\n" + byteToString(responseTelegram, 78, 16))
        }
        if(byteToString(responseTelegram, 40, 1) == "O") {
            body.authCd = byteToString(responseTelegram, 94, 12).trim()
            body.issuerCode = byteToString(responseTelegram, 141, 2)
            body.acquirerCode = byteToString(responseTelegram, 159, 2)
            body.regDate = byteToString(responseTelegram, 49, 12)

            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                flow<Unit> {
                    ApiServiceRepositoryImpl().getAPIService().push(
                        body.token,
                        RequestInsertPaymentDataEntity(
                            Mappers.getMapper(RequestDataMapper::class.java)
                                .insertPaymentDataDtoToEntity(body)
                        )
                    ).suspendOnSuccess {
                        val responseInsertPaymentDataDTO = Mappers.getMapper(ResponseDataMapper::class.java)
                            .insertPaymentDataEntityToDto(data.data!!)
                        responseInsertPaymentDataDTO.authCode = byteToString(responseTelegram, 94, 12).trim()
                        responseInsertPaymentDataDTO.regDay = byteToString(responseTelegram, 49, 6)
                        onSuccess(responseInsertPaymentDataDTO)
                    }.onError() {
                        onError(String(response.errorBody()!!.bytes(), StandardCharsets.UTF_8))
                    }.onException {
                        onError(message!!)
                    }
                }.flowOn(Dispatchers.IO).collect()
            }
        }
    }

    private fun byteToString(srcBytes: ByteArray, startIdx: Int, len: Int): String {
        if (startIdx + len > srcBytes.size) {
            return "~~~"
        }
        val arrByte = ByteArray(len)
        System.arraycopy(srcBytes, startIdx, arrByte, 0, len)
        return try {
            String(arrByte, charset("EUC-KR"))
        } catch (e: Exception) {
            e.printStackTrace()
            return "X"
        }
    }
}