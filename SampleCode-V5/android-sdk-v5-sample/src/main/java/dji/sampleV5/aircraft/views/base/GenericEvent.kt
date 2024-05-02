package com.sst.sstdevicestream.ui.base

import dji.sampleV5.aircraft.views.base.Error

typealias OnErrorBaseViewModel = suspend (error: Error) -> Unit
typealias OnSuccessBaseViewModel<T> = suspend (value: T) -> Unit
typealias OnCompletionBaseViewModel = suspend () -> Unit
typealias OnCallBackClick<T> = (item: T) -> Unit
typealias OnPositiveButtonClick = () -> Unit
typealias OnNegativeButtonClick = () -> Unit
typealias OnAdapterClickItem<T> = (item: T) -> Unit
typealias OnSaveValue = (value: String?) -> Unit