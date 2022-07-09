package com.uvisual.beauty.ui.page

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.uvisual.beauty.ability.camera.CameraAbility
import com.uvisual.beauty.ability.camera.ICameraAbility
import com.uvisual.beauty.dto.PreviewFrameDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

private const val TAG = "CameraViewModel"

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private lateinit var cameraAbility: ICameraAbility

    private val _previewFrame = MutableStateFlow(PreviewFrameDto(byteArrayOf(), 0, 0))
    val previewFrame: StateFlow<PreviewFrameDto> = _previewFrame

    fun init(context: Context) {
        cameraAbility = CameraAbility.getInstance(context)
        cameraAbility.onPreviewFrame = { frame, width, height ->
            _previewFrame.value = PreviewFrameDto(frame, width, height)
        }
    }

    fun start(width: Int, height: Int) {
        cameraAbility.onResume(width, height)
    }

}