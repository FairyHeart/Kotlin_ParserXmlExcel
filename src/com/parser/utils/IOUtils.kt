package com.parser.utils

import java.io.Closeable
import java.io.IOException

/**
 *
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */
object IOUtils {

    fun close(input: Closeable?) {
        try {
            input?.close()
        } catch (e: IOException) {
        }
    }
}