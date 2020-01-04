package com.parser.xmlToXls

import com.parser.xmlToXls.bean.XlsWriteBean

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
fun main() {

    val rootDir = "D://parserExcel"
    val dirName = "res"
    val xlsName = "display.xlsx"
    val xlsWriteBean = XlsWriteBean.Builder
        .setRootDir(rootDir)
        .setDirName(dirName)
        .setXlsName(xlsName)
        .builder()
    XmlToXlsManager.getInstance().startParserXls(xlsWriteBean.mBuilder)

}