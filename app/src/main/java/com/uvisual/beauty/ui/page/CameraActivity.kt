package com.uvisual.beauty.ui.page

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.uvisual.archi.BaseActivity
import com.uvisual.beauty.ability.camera.preview.CameraPreviewView
import com.uvisual.beauty.ui.theme.UltraBeautyTheme
import com.uvisual.beauty.utils.doOnLayout
import com.uvisual.core.ext.findActivity

private const val TAG = "CameraActivity"

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
                        CameraPreview(cameraViewModel)
                        SeekBar(cameraViewModel)
                    }
                }
            }
        }
        cameraViewModel.init(this)

    }

}

@Composable
private fun SeekBar(vm: CameraViewModel) {
    val filterStrength by vm.filterStrength.collectAsState()
    Slider(filterStrength, vm.onFilterStrengthChanged())
}

@Composable
private fun CameraPreview(vm: CameraViewModel) {
    val context = LocalContext.current
    val preview by vm.previewFrame.collectAsState()
    val rotation by vm.previewRotation.collectAsState()
    val cameraPreviewView = remember(context) {
        val cameraPreviewView = CameraPreviewView(context)
        cameraPreviewView.doOnLayout {
            Log.d(TAG, "CameraPreview: doOnLayout: width = ${it.width}, height = ${it.height}")
            vm.start(it.width, it.height)
        }
        return@remember cameraPreviewView
    }
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    AndroidView({ cameraPreviewView }, modifier = Modifier) {
        Log.d(TAG, "CameraPreview:")
        if (preview.width == 0) {
            return@AndroidView
        }
        it.updateFrame(preview.data, preview.width, preview.height)
        it.setRotation(rotation)
    }
}