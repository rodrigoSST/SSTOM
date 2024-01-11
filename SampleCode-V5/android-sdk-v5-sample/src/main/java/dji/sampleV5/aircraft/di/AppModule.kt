package dji.sampleV5.aircraft.di

import dji.sampleV5.aircraft.api.APIClient
import dji.sampleV5.aircraft.api.DeviceStreamAPI
import dji.sampleV5.aircraft.data.repository.DeviceDataRepository
import dji.sampleV5.aircraft.data.repository.DeviceDataRepositoryImpl
import dji.sampleV5.aircraft.models.LiveStreamVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    //API
    single<DeviceStreamAPI> {
        APIClient().provideDeviceStreamEndpoints()
    }

    //ViewModel
    viewModel { LiveStreamVM(get()) }

    //Repository
    single<DeviceDataRepository> { DeviceDataRepositoryImpl(get()) }
}