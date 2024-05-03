package dji.sampleV5.aircraft.views.login

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.views.base.BaseViewModel
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.comom.extensions.pop
import dji.sampleV5.aircraft.databinding.FragmentLoginBinding
import dji.sampleV5.aircraft.views.base.BaseActivityContract
import dji.sampleV5.aircraft.views.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginEmailFragment : BaseFragment<FragmentLoginBinding, BaseViewModel>(
    FragmentLoginBinding::inflate
) {

    override val viewModel: BaseViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BaseActivityContract)?.onChangeBottomNavigationVisibility(false)

        setupViews()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivityContract)?.onChangeBottomNavigationVisibility(false)
    }

    private fun setupViews() {
        binding.imgClose.setOnClickListener {
            pop()
        }

        binding.tiField.hint = getString(R.string.what_is_your_email)

        binding.btnConfirm.setOnClickListener {
            if (binding.etField.text.isNullOrEmpty()) {
                binding.tiField.error = getString(R.string.fill_the_field)
            } else {
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
    }
}