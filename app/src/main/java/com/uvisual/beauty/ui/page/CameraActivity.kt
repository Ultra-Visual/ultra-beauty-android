package com.uvisual.beauty.ui.page

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import com.uvisual.archi.BaseActivity
import com.uvisual.beauty.ability.camera.preview.CameraPreviewView
import com.uvisual.beauty.dto.PreviewFrameDto
import com.uvisual.beauty.ui.theme.UltraBeautyTheme
import com.uvisual.beauty.utils.doOnLayout
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "CameraActivity"

@AndroidEntryPoint
class CameraActivity : BaseActivity() {
    private val cameraViewModel: CameraViewModel by viewModels()
    override fun initViewModel() {
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UltraBeautyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {

                    Column {
                        CameraPreview(this@CameraActivity, cameraViewModel)

                    }
                }
            }
        }
        cameraViewModel.init(this)

    }

}

@Composable
private fun CameraPreview(context: Context, vm: CameraViewModel) {
    val preview by vm.previewFrame.collectAsState(vm.previewFrame.value)
    val rotation by vm.previewRotation.collectAsState(vm.previewRotation.value)
    val cameraPreviewView = remember(context) {
        val cameraPreviewView = CameraPreviewView(context)
        cameraPreviewView.doOnLayout {
            vm.start(it.width, it.height)
        }
        return@remember cameraPreviewView
    }

    AndroidView({ cameraPreviewView }) {
        Log.d(TAG, "CameraPreview:")
        if (preview.width == 0) {
            return@AndroidView
        }
        it.updateFrame(preview.data, preview.width, preview.height)
        it.setRotation(rotation)
    }
}