package com.parser.xlsToXml

/**
 * 测试类
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */

fun main() {

    startParserFile()

//    startParserDir()
}

/**
 * 转换指定文件
 */
private fun startParserFile() {

    val dir = "d://parserExcel"
    val fileName = "queue.xls"

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