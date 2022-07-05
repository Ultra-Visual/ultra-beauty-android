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
import com.uvisual.beauty.ui.theme.UltraBeautyTheme
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "CameraActivity"

@AndroidEntryPoint
class CameraActivity : BaseActivity() {
    private val cameraViewModel: CameraViewModel by viewModels()
    override fun initViewModel() {
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
    val text: String by vm.preview.collectAsState("initial")

    val cameraPreviewView = remember(context) {
        return@remember CameraPreviewView(context)
    }

    AndroidView({ cameraPreviewView }) {
        Log.d(TAG, "CameraPreview:")
        val previewFrame = vm.previewFrame.value
        it.updateFrame(previewFrame.data, previewFrame.width, previewFrame.height)
    }
}