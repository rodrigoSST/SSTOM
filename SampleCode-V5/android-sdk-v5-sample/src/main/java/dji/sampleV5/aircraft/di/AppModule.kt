package dji.sampleV5.aircraft.di

import dji.sampleV5.aircraft.models.LiveStreamVM
import dji.sampleV5.aircraft.views.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    //ViewModel
    viewModel { LiveStreamVM(get()) }
    viewModel { LoginViewModel(get()) }

}