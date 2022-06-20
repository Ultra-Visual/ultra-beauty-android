package com.uvisual.beauty.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.uvisual.beauty.BeautyApplication
import com.uvisual.beauty.ui.page.CameraRender
import dagger.hilt.android.lifecycle.HiltViewModel

private const val TAG = "CameraRepository"

@HiltViewModel
class CameraRepository {
    private lateinit var cameraId: String

    private var frameAvailableCallback: ((SurfaceTexture) -> Unit)? = null

    private val backgroundHandler: Handler by lazy {
        val handlerThread = HandlerThread("back_camera_thread")
        handlerThread.start()
        return@lazy Handler(handlerThread.looper)

    }

    private lateinit var surfaceTexture: SurfaceTexture
    private lateinit var cameraRender: CameraRender

    fun createCameraRender(onSurfaceCreated: () -> Unit): CameraRender {
        cameraRender = CameraRender {
            surfaceTexture = cameraRender.surfaceTexture!!
            onSurfaceCreated()
        }
        return cameraRender
    }

    @SuppressLint("MissingPermission")
    fun open(width: Int, height: Int, frameAvailableCallback: ((SurfaceTexture) -> Unit)) {
        this.frameAvailableCallback = frameAvailableCallback
        val context = BeautyApplication.application
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (cameraId in cameraManager.cameraIdList) {
            if (setupCamera(cameraManager, cameraId)) {
                break
            }
        }
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.d(TAG, "onOpened: ")
                createCameraPreviewSession(camera, width, height)
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
    }

    private fun createCameraPreviewSession(camera: CameraDevice, width: Int, height: Int) {
        Log.d(TAG, "createCameraPreviewSession: ")
        val createCaptureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        surfaceTexture.setDefaultBufferSize(width, height)
        surfaceTexture.setOnFrameAvailableListener {
            frameAvailableCallback?.invoke(it)
        }

        val surface = Surface(surfaceTexture)

        createCaptureRequest.addTarget(surface)
        createSession(camera, surface, createCaptureRequest)
    }


    private fun createSession(
        camera: CameraDevice, surface: Surface, createCaptureRequest: CaptureRequest.Builder
    ) {
        camera.createCaptureSession(
            listOf(
                surface
            ), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    session.setRepeatingRequest(
                        createCaptureRequest.build(), null, backgroundHandler
                    )
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.w(TAG, "onConfigureFailed: ")
                }
            }, backgroundHandler
        )
    }


    private fun setupCamera(cm: CameraManager, cameraId: String): Boolean {
        val cameraCharacteristics = cm.getCameraCharacteristics(cameraId)
        val facing = cameraCharacteristics[CameraCharacteristics.LENS_FACING]
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
            return false
        }
        val streamConfigurationMap =
            (cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP] ?: return false)
        this.cameraId = cameraId
        return true
    }

}