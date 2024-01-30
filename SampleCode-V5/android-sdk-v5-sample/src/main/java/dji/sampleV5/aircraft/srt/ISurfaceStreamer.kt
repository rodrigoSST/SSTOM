package dji.sampleV5.aircraft.srt

import android.Manifest
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.RequiresPermission

interface ISurfaceStreamer {

    @RequiresPermission(allOf = [Manifest.permission.CAMERA])
    fun startPreview(previewSurface: Surface)

    @RequiresPermission(allOf = [Manifest.permission.CAMERA])
    fun startPreview(surfaceView: SurfaceView) =
        startPreview(surfaceView.holder.surface)

    @RequiresPermission(allOf = [Manifest.permission.CAMERA])
    fun startPreview(surfaceHolder: SurfaceHolder) =
        startPreview(surfaceHolder.surface)

    @RequiresPermission(allOf = [Manifest.permission.CAMERA])
    fun startPreview(textureView: TextureView) =
        startPreview(Surface(textureView.surfaceTexture))

    fun stopPreview()
}