package com.sst.data.api

class SstApiClient {
    fun provideAPIEndpoints(): SstPlayApi = RetrofitService.getInstance(
        baseUrl = "https://spyskytech.net:443/",
        interceptors = listOf()
    )
}