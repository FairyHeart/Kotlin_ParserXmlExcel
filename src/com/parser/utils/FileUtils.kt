package com.parser.utils

import java.io.File

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-01-13.
 */
object FileUtils {

    /**
     * 创建文件或者文件夹，如果文件存在，先清空内容
     * @param dir
     * @param fileName
     */
    fun createNewFile(dir: String, fileName: String): File {
        val file = File(dir, fileName)
        if (file.exists()) {
            file.delete()
        } else {
            file.createNewFile()
        }
        return file
    }

    /**
     * 如果文件存在,创建文件或者文件夹
     * @param dir
     * @param fileName
     */
    fun careteFileNoExit(dir: String, fileName: String): File {
        val file = File(dir, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }
}