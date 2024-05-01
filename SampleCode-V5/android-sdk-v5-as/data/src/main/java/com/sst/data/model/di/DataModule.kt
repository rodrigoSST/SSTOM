package com.sst.data.model.di

import com.sst.data.model.api.SstApiClient
import com.sst.data.model.api.SstPlayApi
import com.sst.data.model.repository.DeviceRepository
import com.sst.data.model.repository.DeviceRepositoryImpl
import com.sst.data.model.repository.LoginRepository
import com.sst.data.model.repository.LoginRepositoryImpl
import com.sst.data.model.repository.StreamRepository
import com.sst.data.model.repository.StreamRepositoryImpl
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