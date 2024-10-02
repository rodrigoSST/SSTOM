package com.sst.data.api

import com.sst.data.BuildConfig

class SstApiClient {
    fun provideAPIEndpoints(): SstPlayApi = RetrofitService.getInstance(
        baseUrl = if (BuildConfig.DEBUG) /*"https://dm.sstdev.in/"*/"https://dm.spyskytech.com/" else "https://dm.spyskytech.com/",
        interceptors = listOf()
    )
}