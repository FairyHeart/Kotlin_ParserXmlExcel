package com.txt

import java.io.File

/**
 * txt每一行的类容读写到excel中
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
fun main() {

    val filePath = "E://stringToXml"
    val targetFileName = "test.txt"
    val xlsName = "text.xlsx"
    val file = File(filePath, targetFileName)
    if (file.exists()) {
        val datas = file.readLines()
        TxtToXlsManager.getInstance().startParserXls(filePath = filePath, xlsName = xlsName, lists = datas)
    }

}
