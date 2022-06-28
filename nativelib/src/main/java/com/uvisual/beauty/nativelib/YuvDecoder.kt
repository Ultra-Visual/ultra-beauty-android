package com.uvisual.beauty.nativelib

import android.graphics.Bitmap

object YuvDecoder {
    init {
        System.loadLibrary("yuv-decoder-lib")
    }

    external fun yuvToRgba(yuv: ByteArray, width: Int, height: Int, out: IntArray)

    external fun yuvToArgb(yuv: ByteArray, width: Int, height: Int, out: IntArray)

    external fun adjustBitmap(bitmap: Bitmap)
}