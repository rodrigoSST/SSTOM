package dji.sampleV5.aircraft

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dji.sampleV5.aircraft.comom.SHARED_PREFS
import dji.sampleV5.aircraft.databinding.ActivityMainDefaultBinding
import dji.sampleV5.aircraft.models.MSDKInfoVm
import dji.sampleV5.aircraft.models.MSDKManagerVM
import dji.sampleV5.aircraft.models.globalViewModels
import dji.sampleV5.aircraft.util.ToastUtils
import dji.sampleV5.aircraft.views.login.LoginPasswordFragment
import dji.v5.common.utils.GeoidManager
import dji.v5.utils.common.LogUtils
import dji.v5.utils.common.PermissionUtil
import dji.v5.utils.common.StringUtils
import dji.v5.ux.core.communication.DefaultGlobalPreferences
import dji.v5.ux.core.communication.GlobalPreferencesManager
import dji.v5.ux.core.util.UxSharedPreferencesUtil
import dji.v5.ux.sample.showcase.defaultlayout.DefaultLayoutActivity
import dji.v5.ux.sample.showcase.widgetlist.WidgetsActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainDefaultBinding

    val tag: String = LogUtils.getTag(this)
    private val permissionArray = arrayListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.KILL_BACKGROUND_PROCESSES,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.CAMERA,
    )

    init {
        permissionArray.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.RECORD_AUDIO)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

        }
    }

    private val msdkInfoVm: MSDKInfoVm by viewModels()
    private val msdkManagerVM: MSDKManagerVM by globalViewModels()
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainDefaultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.contentMain.isVisible = false

        val videoPath = "android.resource://" + packageName + "/" + R.raw.intro_video
        val uri = Uri.parse(videoPath)

        binding.videoView.setVideoURI(uri)
        binding.videoView.start()

        binding.videoView.setOnCompletionListener {
            lifecycleScope.launch {
                var count = 0
                while (count < 10) {
                    count++
                    if (msdkInfoVm.msdkInfo.value?.productType?.name != "UNKNOWN") {
                        savePrefs(msdkInfoVm.msdkInfo.value?.productType?.name ?: "")
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    } else {
                        showToast(getString(R.string.not_connected_yet))
                    }
                    delay(1000)
                }
            }
        }

        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
            finish()
            return
        }

        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        initMSDKInfoView()
        observeSDKManager()
        checkPermissionAndRequest()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermission()) {
            handleAfterPermissionPermitted()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            handleAfterPermissionPermitted()
        }
    }

    override fun onPostResume() {
        binding.videoView.resume()
        super.onPostResume()
    }

    override fun onRestart() {
        binding.videoView.start()
        super.onRestart()
    }

    override fun onPause() {
        binding.videoView.suspend()
        super.onPause()
    }

    private fun handleAfterPermissionPermitted() {
        binding.btnTestingTools.isEnabled = true
    }

    @SuppressLint("SetTextI18n")
    private fun initMSDKInfoView() {
        msdkInfoVm.msdkInfo.observe(this) { msdkInfo ->
            /*if (msdkInfo.productType.name != "UNKNOWN") {
                savePrefs(msdkInfo.productType.name)
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                showToast(getString(R.string.not_connected_yet))
            }*/
            /*binding.imgDevice.setImageResource(setImageDevice(it.productType.name))
            binding.txtInfo.text =
                        "${StringUtils.getResStr(R.string.model, it.productType.name)}\n" +
                        "${StringUtils.getResStr(R.string.package_product_category, it.packageProductCategory)}\n" +
                        it.coreInfo.toString()*/
        }

        binding.btnWidgetList.isEnabled = false
        binding.btnTestingTools.isEnabled = false
        binding.btnLiveStream.isEnabled = false

        binding.btnWidgetList.setOnClickListener {
            Intent(this, WidgetsActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.btnTestingTools.setOnClickListener {
            Intent(this, AircraftTestingToolsActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.btnDefaultLayout.setOnClickListener {
            Intent(this, DefaultLayoutActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.btnLiveStream.setOnClickListener {
            Intent(this, LiveStreamingActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun savePrefs(deviceName: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val prefEdit = sharedPreferences?.edit()
        prefEdit?.putString(PREFS_DEVICE_NAME, deviceName)
        prefEdit?.apply()
    }

    private fun observeSDKManager() {
        msdkManagerVM.lvRegisterState.observe(this) { resultPair ->
            val statusText: String?
            if (resultPair.first) {
                Log.i(TAG,"Register Success")
                statusText = StringUtils.getResStr(this, R.string.registered)
                msdkInfoVm.initListener()
                handler.postDelayed({
                    binding.btnWidgetList.isEnabled = true
                    binding.btnLiveStream.isEnabled = true
                    UxSharedPreferencesUtil.initialize(this)
                    GlobalPreferencesManager.initialize(DefaultGlobalPreferences(this))
                    GeoidManager.getInstance().init(this)
                }, 5000)
            } else {
                showToast("Register Failure: ${resultPair.second}")
                statusText = StringUtils.getResStr(this, R.string.unregistered)
            }
            binding.txtRegistrationStatus.text =
                StringUtils.getResStr(R.string.registration_status, statusText)
        }

        msdkManagerVM.lvProductConnectionState.observe(this) { resultPair ->
            Log.i(TAG, "Product: ${resultPair.second} ,ConnectionState:  ${resultPair.first}")
        }

        msdkManagerVM.lvProductChanges.observe(this) { productId ->
            Log.i(TAG, "Product: $productId Changed")
        }

        msdkManagerVM.lvInitProcess.observe(this) { processPair ->
            Log.i(TAG, "Init Process event: ${processPair.first.name}")
        }

        msdkManagerVM.lvDBDownloadProgress.observe(this) { resultPair ->
            Log.i(TAG, "Database Download Progress current: ${resultPair.first}, total: ${resultPair.second}")
        }
    }

    private fun setImageDevice(deviceName: String): Int =
        when(deviceName) {
            "M30_SERIES" -> R.drawable.drone_matrice_30_series
            "DJI_MAVIC_3_ENTERPRISE_SERIES" -> R.drawable.drone_mavic_3_enterprise
            else -> R.drawable.ic_question
        }


    private fun showToast(content: String) {
        ToastUtils.showToast(content)
    }

    private fun checkPermissionAndRequest() {
        if (!checkPermission()) {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        for (i in permissionArray.indices) {
            if (!PermissionUtil.isPermissionGranted(this, permissionArray[i])) {
                return false
            }
        }
        return true
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result?.entries?.forEach {
            if (!it.value) {
                requestPermission()
                return@forEach
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(permissionArray.toArray(arrayOf()))
    }

    override fun onDestroy() {
        binding.videoView.stopPlayback()
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        disposable.dispose()
    }

    companion object {
        const val TAG = "MainActivityLog"
        const val PREFS_DEVICE_NAME = "PREFS_DEVICE_NAME"
    }
}