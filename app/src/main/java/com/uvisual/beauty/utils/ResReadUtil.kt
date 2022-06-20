package com.uvisual.beauty.utils

import com.uvisual.beauty.BeautyApplication
import java.io.BufferedReader
import java.io.InputStreamReader

object ResReadUtil {
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
}