package com.uvisual.beauty.ability.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.util.Log
import android.util.Size
import android.view.Surface
import com.uvisual.beauty.utils.generateNv21Data

internal class Camera2Ability(private val activity: Activity) : CameraAbility() {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var cameraFacing = CameraCharacteristics.LENS_FACING_BACK
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private val cameraManager: CameraManager by lazy {
        activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onResume(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        setUpCamera()
    }

    override fun onPause() {
        releaseCamera()
    }

    override fun switchCamera() {
        cameraFacing = when (cameraFacing) {
            CameraCharacteristics.LENS_FACING_BACK -> CameraCharacteristics.LENS_FACING_FRONT
            CameraCharacteristics.LENS_FACING_FRONT -> CameraCharacteristics.LENS_FACING_BACK
            else -> return
        }
        releaseCamera()
        setUpCamera()
    }

    override fun getCameraOrientation(): Int {
        val degrees = when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val cameraId = getCameraId(cameraFacing) ?: return 0
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: return 0
        return if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            (orientation + degrees) % 360
        } else { // back-facing
            (orientation - degrees) % 360
        }
    }

    override fun hasMultipleCamera(): Boolean = cameraManager.cameraIdList.size > 1


    @SuppressLint("MissingPermission")
    private fun setUpCamera() {
        val cameraId = getCameraId(cameraFacing) ?: return
        try {
            cameraManager.openCamera(cameraId, CameraDeviceCallback(), null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun releaseCamera() {

    }

    private fun getCameraId(cameraFacing: Int): String? {
        return cameraManager.cameraIdList.find { id ->
            cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == cameraFacing
        }

    }

    private inner class CameraDeviceCallback : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }

    }

    private fun startCaptureSession() {
        val size = chooseOptimalSize()
        imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.YUV_420_888, 1).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireNextImage() ?: return@setOnImageAvailableListener
                Log.d(TAG, "startCaptureSession: width = ${image.width}, height = ${image.height}")
                onPreviewFrame?.invoke(image.generateNv21Data(), image.width, image.height)
                image.close()
            }, null)
        }

        try {
            cameraDevice?.createCaptureSession(
                listOf(imageReader!!.surface),
                CaptureStateCallback(),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun chooseOptimalSize(): Size {
        if (viewWidth == 0 || viewHeight == 0) {
            return Size(0, 0)
        }
        val cameraId = getCameraId(cameraFacing) ?: return Size(0, 0)
        val outputSizes = cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(ImageFormat.YUV_444_888)
        val orientation = getCameraOrientation()
        val maxPreviewWidth = if (orientation == 90 or 270) viewHeight else viewWidth
        val maxPreviewHeight = if (orientation == 90 or 270) viewWidth else viewHeight
        return outputSizes?.filter {
            it.width < maxPreviewWidth / 2 && it.height < maxPreviewHeight / 2
        }?.maxByOrNull {
            it.width * it.height
        } ?: Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)

    }

    private inner class CaptureStateCallback : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            cameraDevice ?: return
            captureSession = session
            val builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) ?: return
            builder.addTarget(imageReader!!.surface)
            session.setRepeatingRequest(builder.build(), null, null)
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
        }
    }

    companion object {
        private const val TAG = "Camera2Ability"

        private const val PREVIEW_WIDTH = 480
        private const val PREVIEW_HEIGHT = 640
    }
}


