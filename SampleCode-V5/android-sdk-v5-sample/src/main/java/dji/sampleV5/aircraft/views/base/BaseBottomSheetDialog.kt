package dji.sampleV5.aircraft.views.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dji.sampleV5.aircraft.comom.extensions.toPx

abstract class BaseBottomSheetDialog<T : ViewBinding> : BottomSheetDialogFragment() {

    abstract fun onViewBinding(inflater: LayoutInflater, container: ViewGroup?): T
    lateinit var binding: T
    open var isFullScreen: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = onViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setDimAmount(0.4f)

            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    open fun initialize() {}

    override fun onStart() {
        super.onStart()

        setFullScreen()
    }

    open fun setFullScreen() {
        if (isFullScreen) {
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.also { v ->
                    v.layoutParams?.height =
                        resources.displayMetrics.heightPixels - (RemoteViews.MARGIN_TOP * 2).toPx(
                            requireContext()
                        )

                    BottomSheetBehavior.from(v).apply {
                        isFitToContents = false
                        expandedOffset = RemoteViews.MARGIN_TOP.toPx(requireContext())
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
        }
    }
}