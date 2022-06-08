package com.uvisual.beauty.ui.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.LENS_FACING
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.SurfaceView
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject

private const val TAG = "CameraViewModel"

@HiltViewModel
class CameraViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private val _preview = MutableStateFlow("")
    val preview = _preview as StateFlow<String>
    private lateinit var imageReader: ImageReader
    private lateinit var cameraId: String

    private val backgroundHandler: Handler by lazy {
        val handlerThread = HandlerThread("back_camera_thread")
        Log.d(TAG, "create backgroundHandler: ")
        handlerThread.start()
        Log.d(TAG, "create backgroundHandler: ")
        return@lazy Handler(handlerThread.looper)

    }

    fun start() {

    }

    @SuppressLint("MissingPermission")
    fun openCamera(context: Context, surfaceView: SurfaceView) {
        Log.d(TAG, "openCamera: ")
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (cameraId in cameraManager.cameraIdList) {
            if (setupCamera(cameraManager, cameraId, surfaceView.width, surfaceView.height)) {
                break
            }
        }
        Log.d(TAG, "openCamera: 11")
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.d(TAG, "onOpened: ")
                createCameraPreviewSession(camera, surfaceView)
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.d(TAG, "onDisconnected: ")
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.d(TAG, "onError: ")
                camera.close()
            }

        }, backgroundHandler)
        return
    }

    private fun createCameraPreviewSession(camera: CameraDevice, surfaceView: SurfaceView) {
        val createCaptureRequest = camera.createCaptureRequest(TEMPLATE_PREVIEW)
//        createCaptureRequest.addTarget(surfaceView.holder.surface)
        createCaptureRequest.addTarget(imageReader.surface)
//        camera.createCaptureSession(SessionConfiguration(SessionConfiguration.SESSION_REGULAR, ))
        camera.createCaptureSession(
            listOf(
                surfaceView.holder.surface,
                imageReader.surface
            ), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    session.setRepeatingRequest(
                        createCaptureRequest.build(),
                        object : CameraCaptureSession.CaptureCallback() {
                            override fun onCaptureCompleted(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                result: TotalCaptureResult
                            ) {
                                Log.d(TAG, "onCaptureCompleted: ")
                            }
                                                                        },
                        backgroundHandler
                    )
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                }
            }, backgroundHandler
        )
//        camera.createCaptureSession(
//            listOf(
//                mSurfaceView.holder.surface,
//                mImageReader.surface
//            ), object : , backgroundHandler
//        )
    }

    fun stopCamera() {
        Log.d(TAG, "stopCamera: ")
    }

    private fun setupCamera(cm: CameraManager, cameraId: String, width: Int, height: Int): Boolean {
        val cameraCharacteristics = cm.getCameraCharacteristics(cameraId)
        val facing = cameraCharacteristics[LENS_FACING]
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
            return false
        }
        val streamConfigurationMap =
            (cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP] ?: return false)
        imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener(ImageReaderAvailableListenerImp(), backgroundHandler)
        this.cameraId = cameraId
        return true
    }

    private class ImageReaderAvailableListenerImp : ImageReader.OnImageAvailableListener {
        private lateinit var y: ByteArray
        private lateinit var u: ByteArray
        private lateinit var v: ByteArray
        private val lock = ReentrantLock()

        override fun onImageAvailable(reader: ImageReader) {
            val image = reader.acquireNextImage()
            Log.d(TAG, "onImageAvailable: ")
            if (image.format == ImageFormat.YUV_420_888) {
                val planes = image.planes
                lock.lock()
                if (!::y.isInitialized) {
                    y = ByteArray(planes[0].buffer.limit() - planes[0].buffer.position())
                    u = ByteArray(planes[1].buffer.limit() - planes[1].buffer.position())
                    v = ByteArray(planes[2].buffer.limit() - planes[2].buffer.position())

                }
                if (planes[0].buffer.remaining() == y.size) {
                    planes[0].buffer.get(y)
                    planes[1].buffer.get(u)
                    planes[2].buffer.get(v)
                    // 接下来通过转换，可以转换为 Bitmap 进行展示
                }
                lock.unlock()
            }
            image.close()
        }
    }
}