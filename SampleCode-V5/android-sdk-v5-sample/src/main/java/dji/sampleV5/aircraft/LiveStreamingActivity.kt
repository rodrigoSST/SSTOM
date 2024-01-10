package dji.sampleV5.aircraft

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dji.sampleV5.aircraft.views.LiveStreamingFragment

class LiveStreamingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_stream)

        supportFragmentManager.commit {
            replace(R.id.nav_host_fragment_content_main, LiveStreamingFragment())
        }
    }
}