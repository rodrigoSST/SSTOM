package dji.sampleV5.aircraft.views.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.views.base.BaseViewModel
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.comom.SHARED_PREFS
import dji.sampleV5.aircraft.comom.extensions.hideKeyboard
import dji.sampleV5.aircraft.comom.extensions.pop
import dji.sampleV5.aircraft.databinding.FragmentLoginBinding
import dji.sampleV5.aircraft.views.base.BaseActivityContract
import dji.sampleV5.aircraft.views.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginEmailFragment : BaseFragment<FragmentLoginBinding, BaseViewModel>(
    FragmentLoginBinding::inflate
) {

    override val viewModel: BaseViewModel by viewModel()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BaseActivityContract)?.onChangeBottomNavigationVisibility(false)

        activity?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)?.let {
            sharedPreferences = it
        }

        setupViews()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivityContract)?.onChangeBottomNavigationVisibility(false)
    }

    private fun setupViews() {
        if (rememberActivated()) {
            binding.etField.setText(getEmailPrefs())
            binding.cbRemember.isChecked = true
        }

        binding.imgClose.setOnClickListener {
            pop()
        }

        binding.tiField.hint = getString(R.string.what_is_your_email)

        binding.btnConfirm.setOnClickListener {
            next()
        }

        binding.etField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                next()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun next() {
        binding.etField.hideKeyboard(requireContext())

        if (binding.etField.text.isNullOrEmpty()) {
            binding.tiField.error = getString(R.string.fill_the_field)
        } else {
            savePrefs(binding.etField.text.toString(), binding.cbRemember.isChecked)

            findNavController().navigate(
                R.id.action_navigation_login_email_to_navigation_login_password,
                Bundle().apply {
                    putString(
                        LoginPasswordFragment.EXTRA_EMAIL,
                        binding.etField.text.toString()
                    )
                }
            )
        }
    }

    private fun rememberActivated() =  sharedPreferences.getBoolean(PREFS_REMEMBER_ME, false)

    private fun getEmailPrefs(): String {
        return sharedPreferences.getString(LoginPasswordFragment.PREFS_USER_EMAIL, "") ?: ""
    }

    private fun savePrefs(email: String, remember: Boolean) {
        val sharedPreferences = activity?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val prefEdit = sharedPreferences?.edit()
        prefEdit?.putString(LoginPasswordFragment.PREFS_USER_EMAIL, email)
        prefEdit?.putBoolean(PREFS_REMEMBER_ME, remember)
        prefEdit?.apply()
    }

    companion object {
        const val PREFS_REMEMBER_ME = "PREFS_REMEMBER_ME"
    }

}