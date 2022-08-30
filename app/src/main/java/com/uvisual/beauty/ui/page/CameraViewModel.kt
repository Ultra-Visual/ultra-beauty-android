package com.uvisual.beauty.ui.page

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.uvisual.beauty.ability.camera.CameraAbility
import com.uvisual.beauty.ability.camera.ICameraAbility
import com.uvisual.beauty.ability.camera.Rotation
import com.uvisual.beauty.dto.PreviewFrameDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "CameraViewModel"

class CameraViewModel constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private lateinit var cameraAbility: ICameraAbility

    private val _previewFrame = MutableStateFlow(PreviewFrameDto(byteArrayOf(), 0, 0))
    val previewFrame: StateFlow<PreviewFrameDto> = _previewFrame

    private val _filterStrength = MutableStateFlow(0.5f)
    val filterStrength: StateFlow<Float> = _filterStrength

    private val _previewRotation = MutableStateFlow(Rotation.NORMAL)
    val previewRotation: StateFlow<Rotation> = _previewRotation

    fun init(activity: Activity) {
        cameraAbility = CameraAbility.getInstance(activity)
        cameraAbility.onPreviewFrame = { frame, width, height ->
            _previewFrame.value = PreviewFrameDto(frame, width, height)
        }
        _previewRotation.value = getRotation(cameraAbility.getCameraOrientation())
    }

    fun start(width: Int, height: Int) {
        cameraAbility.onResume(width, height)
    }

    fun onFilterStrengthChanged(): (Float) -> Unit = {
        _filterStrength.value = it
    }

    private fun getRotation(orientation: Int): Rotation = when (orientation) {
        90 -> Rotation.ROTATION_90
        180 -> Rotation.ROTATION_180
        270 -> Rotation.ROTATION_270
        else -> Rotation.NORMAL
    }
}