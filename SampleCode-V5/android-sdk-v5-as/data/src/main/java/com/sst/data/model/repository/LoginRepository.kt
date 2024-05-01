package com.sst.data.model.repository

import com.sst.data.model.api.SstPlayApi
import com.sst.data.model.request.LoginRequest
import com.sst.data.model.response.LoginResponse

interface LoginRepository {
    suspend fun doLogin(login: LoginRequest): LoginResponse
}

class LoginRepositoryImpl(
    private val sstPlayApi: SstPlayApi
) : LoginRepository {
    override suspend fun doLogin(login: LoginRequest): LoginResponse =
        sstPlayApi.doLogin(login)

}