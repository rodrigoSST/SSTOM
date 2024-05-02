package dji.sampleV5.aircraft.views.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sst.sstdevicestream.ui.base.OnCompletionBaseViewModel
import com.sst.sstdevicestream.ui.base.OnErrorBaseViewModel
import com.sst.sstdevicestream.ui.base.OnSuccessBaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val showLoading = MutableLiveData<Boolean>().apply {
        value = false
    }

    val _error: MutableLiveData<String> = MutableLiveData<String>()
    val error: LiveData<String> = _error

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    open fun <T : Any> Flow<ResultWrapper<T?>>.singleExec(
        onError: OnErrorBaseViewModel? = null,
        onSuccessBaseViewModel: OnSuccessBaseViewModel<T?>? = null,
        onCompletionBaseViewModel: OnCompletionBaseViewModel? = null,
        showLoadingFlag: Boolean = true
    ) = onEach {
        when (val result = it) {
            is ResultWrapper.Loading -> {
                showLoadingWithFlag(showLoadingFlag)
            }
            is ResultWrapper.Failure -> {
                onError?.invoke(result.error)
            }
            is ResultWrapper.Success<T?> ->
                onSuccessBaseViewModel?.invoke(result.value)
            is ResultWrapper.DismissLoading -> {
                hideLoadingWithFlag(showLoadingFlag)
            }
        }
    }.onCompletion {
        onCompletionBaseViewModel?.invoke()
    }.catch {
        hideLoadingWithFlag(showLoadingFlag)
        onError?.invoke(Error.UnknownException(it))
    }.launchIn(CoroutineScope(coroutineContext))

    private fun hideLoadingWithFlag(showLoadingFlag: Boolean) {
        if (showLoadingFlag) {
            hideLoading()
        }
    }

    private fun showLoadingWithFlag(showLoadingFlag: Boolean) {
        if (showLoadingFlag) {
            showLoading()
        }
    }

    open fun hideLoading() {
        showLoading.value = false
    }

    open fun showLoading() {
        showLoading.value = true
    }
}