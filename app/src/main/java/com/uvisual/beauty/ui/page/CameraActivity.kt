package com.uvisual.beauty.ui.page

import android.content.Context
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.uvisual.archi.BaseActivity
import com.uvisual.beauty.ui.theme.UltraBeautyTheme
import com.uvisual.beauty.vm.MainActivityViewModel
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
                        CameraPriview(this@CameraActivity, cameraViewModel)

                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPriview(context: Context, vm: CameraViewModel) {
    val text: String by vm.preview.collectAsState("initial")
    val surfaceView = remember(context) {
        val surfaceView = SurfaceView(context)
        surfaceView.holder.apply {
            addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    vm.openCamera(context, surfaceView = surfaceView)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    vm.stopCamera()
                }

            })
        }
        return@remember surfaceView
    }

    AndroidView({ surfaceView })
}