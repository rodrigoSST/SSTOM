package dji.sampleV5.aircraft.srtPlayer

import com.google.android.exoplayer2.upstream.DataSource
import dji.sampleV5.aircraft.srtPlayer.SrtDataSource

class SrtDataSourceFactory :
    DataSource.Factory {
    override fun createDataSource(): DataSource {
        return SrtDataSource()
    }
}