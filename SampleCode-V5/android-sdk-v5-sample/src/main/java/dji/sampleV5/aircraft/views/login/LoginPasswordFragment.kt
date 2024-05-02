package dji.sampleV5.aircraft.views.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import com.sst.data.model.request.LoginRequest
import dji.sampleV5.aircraft.comom.extensions.pop
import dji.sampleV5.aircraft.comom.extensions.safeLet
import dji.sampleV5.aircraft.views.base.BaseActivityContract
import dji.sampleV5.aircraft.LiveStreamingActivity
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.data.MSDKInfo
import org.koin.androidx.viewmodel.ext.android.viewModel
import dji.sampleV5.aircraft.databinding.FragmentLoginBinding
import dji.sampleV5.aircraft.enums.DeviceType
import dji.sampleV5.aircraft.models.MSDKInfoModel
import dji.sampleV5.aircraft.views.base.BaseFragment
import dji.v5.utils.inner.SDKConfig

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

        binding.btnConfirm.setOnClickListener {
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
    }

    private fun setupObservers() {
        viewModel.login.observe(viewLifecycleOwner) {
            savePrefs(it.userData.idUser, it.device.idDevice)
            if (it != null) {
                Intent(requireContext(), LiveStreamingActivity::class.java).also { intent ->
                    startActivity(intent)
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            showError(it)
        }
    }

    private fun getDeviceName(): String =
        MSDKInfo(MSDKInfoModel().getSDKVersion()).productType.name

    private fun getDeviceId() =
        SDKConfig.getInstance().deviceId

    private fun savePrefs(userId: String, deviceId: String) {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val prefEdit = sharedPreferences?.edit()
        prefEdit?.putString(PREFS_USER_ID, userId)
        prefEdit?.putString(PREFS_DEVICE_ID, deviceId)
        prefEdit?.apply()
    }

    companion object {
        const val EXTRA_EMAIL = "EXTRA_EMAIL"
        const val PREFS_USER_ID = "PREFS_USER_ID"
        const val PREFS_DEVICE_ID = "PREFS_DEVICE_ID"
    }
}