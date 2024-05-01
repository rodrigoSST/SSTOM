package com.sst.data.model.api

import com.reisdeveloper.listeneraccuracy.api.RetrofitService

class SstApiClient {
    fun provideAPIEndpoints(): SstPlayApi = RetrofitService.getInstance(
        baseUrl = "http://54.233.253.75:5000/",
        interceptors = listOf()
    )
}