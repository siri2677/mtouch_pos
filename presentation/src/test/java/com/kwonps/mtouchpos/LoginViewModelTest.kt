package com.kwonps.mtouchpos

import app.cash.turbine.test
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.usecase.user.DeleteUserInfo
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import com.kwonps.domain.usecase.user.FetchSavedUserInfo
import com.kwonps.domain.usecase.user.LoginUser
import com.kwonps.mtouchpos.viewmodel.LoginVM
import com.kwonps.mtouchpos.viewmodel.mapper.toUseCaseResult
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LoginVM
    private lateinit var fetchSavedUserInfoUseCase: FetchSavedUserInfo
    private lateinit var deleteUserInfoUseCase: DeleteUserInfo
    private lateinit var fetchConnectedUserInfoUseCase: FetchConnectedUserInfo
    private lateinit var loginUserUseCase: LoginUser

    private val userInfo = LoginVM.UserInfo(
        tmnId = "test0003",
        serial = "12345",
        mchtId = "ktest"
    )

    private val userDetailData = UserDetailData(
        tmnId = "test0003",
        serial = "12345",
        mchtId = "ktest",
        semiAuth = "Y",
        appDirect = "Y",
        key = "Key123456789XYZ",
        vat = "Y",
        apiMaxInstall = "12",
        payKey = "PaymentKey1234567890"
    )

    @Before
    fun setup() {
        fetchSavedUserInfoUseCase = mockk<FetchSavedUserInfo>()
        deleteUserInfoUseCase = mockk<DeleteUserInfo>()
        fetchConnectedUserInfoUseCase = mockk<FetchConnectedUserInfo>()
        loginUserUseCase = mockk<LoginUser>()

        viewModel = LoginVM(
            fetchSavedUserInfoUseCase = fetchSavedUserInfoUseCase,
            deleteUserInfoUseCase = deleteUserInfoUseCase,
            fetchConnectedUserInfoUseCase = fetchConnectedUserInfoUseCase,
            loginUserUseCase = loginUserUseCase
        )
    }

    @Test
    fun `updateUserInfo correctly`() = runTest {
        viewModel.updateUserInfo(userInfo)

        with(viewModel.loginInfo.value) {
            assertEquals(tmnId, userInfo.tmnId)
            assertEquals(serial, userInfo.serial)
            assertEquals(mchtId, userInfo.mchtId)
        }
    }

    @Test
    fun `deleteUserInfo correctly`() = runTest {
        coEvery { deleteUserInfoUseCase(any()) } just Runs

        viewModel.deleteUserInfo("test0003")

        coVerify(exactly = 1) { deleteUserInfoUseCase(any()) }
    }

    @Test
    fun `fetchUserInfoList success emits userInfoList result`() = runTest {
        val apiResult = listOf(userDetailData)
        val useCaseResult = apiResult.map {
            LoginVM.UserInfo(
                tmnId = it.tmnId,
                serial = it.serial,
                mchtId = it.mchtId
            )
        }

        coEvery { fetchSavedUserInfoUseCase() } returns flow { emit(apiResult) }

        viewModel.fetchUserInfoList()

        coVerify(exactly = 1) { fetchSavedUserInfoUseCase() }

        assertEquals(viewModel.loginInfoList.value, useCaseResult)
    }

    @Test
    fun `fetchConnectedUserInfo correctly mapping from userDetailData to userDetailInfo result`() = runTest {
        coEvery { fetchConnectedUserInfoUseCase() } returns userDetailData

        val userDetailInfo = viewModel.fetchCurrentConnectedUserInfo()

        coVerify(exactly = 1) { fetchConnectedUserInfoUseCase() }

        with(userDetailData) {
            assertEquals(tmnId, userDetailInfo?.tmnId ?: "")
            assertEquals(serial, userDetailInfo?.serial ?: "")
            assertEquals(mchtId, userDetailInfo?.mchtId ?: "")
            assertEquals(semiAuth, userDetailInfo?.semiAuth ?: "")
            assertEquals(appDirect, userDetailInfo?.appDirect ?: "")
            assertEquals(key, userDetailInfo?.key ?: "")
            assertEquals(vat, userDetailInfo?.vat ?: "")
            assertEquals(apiMaxInstall, userDetailInfo?.apiMaxInstall ?: "")
            assertEquals(payKey, userDetailInfo?.payKey ?: "")
        }
    }

    @Test
    fun `login success emits tmnId result`() = runTest  {
        val apiResult = ApiResult.Success(userDetailData)
        val useCaseResult = apiResult.toUseCaseResult { userDetailData.tmnId }

        coEvery { loginUserUseCase(any()) } returns flow { emit(apiResult) }

        viewModel.reactLogin.test {
            viewModel.login()
            assertEquals(awaitItem(), useCaseResult)
        }

        coVerify(exactly = 1) { loginUserUseCase(any()) }
    }
}
