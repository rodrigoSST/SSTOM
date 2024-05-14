package com.sst.data.model.api

import com.reisdeveloper.listeneraccuracy.api.RetrofitService

class SstApiClient {
    fun provideAPIEndpoints(): SstPlayApi = RetrofitService.getInstance(
        baseUrl = "https://spyskytech.net:443/",
        interceptors = listOf()
    )
}