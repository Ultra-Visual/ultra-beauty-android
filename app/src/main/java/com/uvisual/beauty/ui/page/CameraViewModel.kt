package com.uvisual.beauty.ui.page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.uvisual.beauty.dto.PreviewFrameDto
import com.uvisual.beauty.repository.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

private const val TAG = "CameraViewModel"

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val cameraRepository: CameraRepository by lazy { CameraRepository() }

    private val _previewFrame = MutableStateFlow(PreviewFrameDto(byteArrayOf(), 0, 0))
    val previewFrame: StateFlow<PreviewFrameDto> = _previewFrame

    private val _preview = MutableStateFlow("")
    val preview = _preview as StateFlow<String>

    fun createRender(onSurfaceViewCreated: () -> Unit): CameraRender {
        return cameraRepository.createCameraRender(onSurfaceViewCreated)
    }

    fun start(width: Int, height: Int) {
        cameraRepository.open(width, height) {
            _preview.value = it.timestamp.toString()
        }
    }

}