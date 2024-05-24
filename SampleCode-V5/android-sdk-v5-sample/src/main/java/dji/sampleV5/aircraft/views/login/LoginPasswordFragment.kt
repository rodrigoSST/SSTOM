package dji.sampleV5.aircraft.views.login

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.sst.data.model.request.LoginRequest
import dji.sampleV5.aircraft.MainActivity
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.comom.SHARED_PREFS
import dji.sampleV5.aircraft.comom.extensions.hideKeyboard
import dji.sampleV5.aircraft.comom.extensions.pop
import dji.sampleV5.aircraft.comom.extensions.safeLet
import dji.sampleV5.aircraft.data.MSDKInfo
import dji.sampleV5.aircraft.databinding.FragmentLoginBinding
import dji.sampleV5.aircraft.enums.DeviceType
import dji.sampleV5.aircraft.models.MSDKInfoModel
import dji.sampleV5.aircraft.views.LiveStreamingFragment.Companion.EXTRA_ID_DEVICE
import dji.sampleV5.aircraft.views.base.BaseActivityContract
import dji.sampleV5.aircraft.views.base.BaseFragment
import dji.v5.utils.inner.SDKConfig
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginPasswordFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>(
    FragmentLoginBinding::inflate
) {

    override val viewModel: LoginViewModel by viewModel()

    private val email by lazy {
        arguments?.getString(EXTRA_EMAIL)
    }

    private lateinit var deviceModel: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BaseActivityContract)?.onChangeBottomNavigationVisibility(false)

        deviceModel = getDeviceName()

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.imgClose.setOnClickListener {
            pop()
        }
        binding.tiField.hint = getString(R.string.what_is_your_password)

        binding.etField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        binding.etField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                doLogin()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.btnConfirm.setOnClickListener {
            doLogin()
        }
    }

    private fun setupObservers() {
        viewModel.login.observe(viewLifecycleOwner) {
            hideKeyboard(binding.etField)
            savePrefs(it.userData.idUser, it.device.idDevice)
            if (it != null) {
                findNavController().navigate(
                    R.id.action_navigation_login_password_to_navigation_livestreaming,
                    Bundle().apply {
                        putString(EXTRA_ID_DEVICE, it.device.idDevice)
                    }
                )
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            showError(it)
        }

        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
    }

    private fun doLogin() {
        binding.etField.hideKeyboard(requireContext())

        if (binding.etField.text.isNullOrEmpty()) {
            binding.tiField.error = getString(R.string.fill_the_field)
        } else {
            safeLet(email, binding.etField.text.toString()) { emailLet, passwordLet ->
                val login = LoginRequest(
                    emailLet,
                    passwordLet,
                    getDeviceId(),
                    deviceModel,
                    DeviceType.DRONE.type
                )
                viewModel.login(login)
            }
        }
    }

    private fun getDeviceName(): String {
        val sharedPreferences = activity?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences?.getString(MainActivity.PREFS_DEVICE_NAME, "") ?: ""
    }

    private fun getDeviceId() =
        SDKConfig.getInstance().deviceId

    private fun savePrefs(userId: String, deviceId: String) {
        val sharedPreferences = activity?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val prefEdit = sharedPreferences?.edit()
        prefEdit?.putString(PREFS_USER_ID, userId)
        prefEdit?.putString(PREFS_DEVICE_ID, deviceId)
        prefEdit?.apply()
    }

    private fun hideKeyboard(editText: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    companion object {
        const val EXTRA_EMAIL = "EXTRA_EMAIL"
        const val PREFS_USER_ID = "PREFS_USER_ID"
        const val PREFS_DEVICE_ID = "PREFS_DEVICE_ID"
    }
}