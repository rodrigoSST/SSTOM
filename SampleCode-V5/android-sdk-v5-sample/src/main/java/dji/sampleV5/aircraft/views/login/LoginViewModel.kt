package dji.sampleV5.aircraft.views.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sst.data.repository.LoginRepository
import com.sst.data.model.request.LoginRequest
import com.sst.data.model.response.LoginResponse
import dji.sampleV5.aircraft.views.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository
) :  BaseViewModel() {

    private val _login = MutableLiveData<LoginResponse>()
    val login: LiveData<LoginResponse> = _login
    fun login(login: LoginRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            try {
                _login.postValue(loginRepository.doLogin(login))
            } catch (e: Exception) {
                _error.postValue(e.message)
                showLoading.postValue(false)
            }
        }
    }
}