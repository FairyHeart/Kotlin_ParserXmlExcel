package com.parser.utils

import jxl.Workbook
import java.io.Closeable
import java.io.IOException

/**
 * 工具类
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */
object IOUtils {

    fun close(input: Closeable?, wb: Workbook? = null) {
        try {
            wb?.close()
            input?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}