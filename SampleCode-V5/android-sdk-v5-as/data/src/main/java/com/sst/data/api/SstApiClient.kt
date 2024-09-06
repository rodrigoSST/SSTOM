package com.sst.data.api

import com.sst.data.BuildConfig

class SstApiClient {
    fun provideAPIEndpoints(): SstPlayApi = RetrofitService.getInstance(
        baseUrl = if (BuildConfig.DEBUG) "https://dm.sstdev.in/" else "https://dm.spyskytech.com/",
        interceptors = listOf()
    )
}