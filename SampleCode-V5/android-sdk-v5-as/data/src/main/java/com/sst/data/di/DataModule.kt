package com.sst.data.di

import com.sst.data.api.SstApiClient
import com.sst.data.api.SstPlayApi
import com.sst.data.repository.DeviceRepository
import com.sst.data.repository.DeviceRepositoryImpl
import com.sst.data.repository.LoginRepository
import com.sst.data.repository.LoginRepositoryImpl
import com.sst.data.repository.StreamRepository
import com.sst.data.repository.StreamRepositoryImpl
import org.koin.dsl.module

val dataModule = module {
    //API
    single<SstPlayApi> {
        SstApiClient().provideAPIEndpoints()
    }

    //Repository
    single<DeviceRepository> { DeviceRepositoryImpl(get()) }
    single<LoginRepository> { LoginRepositoryImpl(get()) }
    single<StreamRepository> { StreamRepositoryImpl(get()) }
}