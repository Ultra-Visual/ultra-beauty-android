package com.uvisual.beauty.utils

import android.util.Log
import com.uvisual.beauty.BeautyApplication
import java.io.BufferedReader
import java.io.InputStreamReader

object ResReadUtil {
    private const val TAG = "ResReadUtil"
    fun read(id: Int): String {
        val stringBuilder = StringBuilder()
        val inputStream = BeautyApplication.application.resources.openRawResource(id)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        bufferedReader.forEachLine {
            stringBuilder.append(it)
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    fun readAsserts(path: String): String {
        val sb = StringBuilder()
        val resources = BeautyApplication.application.resources
        val inputStream = resources.assets.open(path)
        inputStream.bufferedReader().forEachLine {
            Log.d(TAG, "readAsserts: $it")
            sb.append(it)
        }
        return sb.toString()
    }
}