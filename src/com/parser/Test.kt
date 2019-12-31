package com.parser

/**
 * 测试类
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */

fun main() {

    startParserFile()

    startParserDir()
}

/**
 * 转换指定文件
 */
private fun startParserFile() {

    val src = "d://parserExcel"
    val fileName = "kds.xls"

    val excelFile = ReadExcelFile()
    excelFile.startParserFile(dir = src, fileName = fileName)
}

/**
 * 目录下的所有excel文件转换
 */
private fun startParserDir() {
    val excelFile = ReadExcelFile()
    excelFile.startParserDir("D://parserExcel")
}