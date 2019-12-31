package com.parser

/**
 * 测试类
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */

fun main() {

    val src = "d://parserExcel"

//    val fileName = "kds.xls"
    val fileName = "waiting.xls"

    val excelFile = ReadExcelFile()
    excelFile.startParserFile(src = src, fileName = fileName)
}