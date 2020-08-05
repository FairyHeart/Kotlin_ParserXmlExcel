package com.parser.xlsToXml

import com.parser.utils.FileUtils
import java.io.File

/**
 * excel转string.xml测试类
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */

fun main() {

    startParserFile()

//    startParserDir()

    copyFiles()
}

/**
 * 转换指定文件
 */
private fun startParserFile() {

    val dir = "E://stringToXml"
    val fileName = "display.xls"

    val excelFile = XlsToXmlManager()
    excelFile.startParserFile(dir = dir, fileName = fileName)
}

/**
 * 目录下的所有excel文件转换
 */
private fun startParserDir() {
    val excelFile = XlsToXmlManager()
    excelFile.startParserDir("D://parserExcel")
}

private fun copyFiles() {
    val dir = "d://parserExcel"
    val targeDir = "${dir}/display"
    val file = File(targeDir)
    if (file.exists()) {
        val files = file.walk()
            .maxDepth(20)
            .filter {
                it.isFile
            }
            .filter {
                it.name.contains(".xml")
            }
        files.forEach {
            val path = FileUtils.getFolderName(it, 2)
            val fileName = FileUtils.getFolderName(it, 1)
            val file = File("$dir/$path")
            if (!file.exists()) {
                file.mkdirs()
            }
            FileUtils.copyFile(file,it)
        }

    }
}