package com.parser.xmlToXls

import com.parser.xmlToXls.bean.XlsWriteBean

/**
 * string.xml转excel测试类
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
fun main() {

    val rootDir = "D://parserExcel"
    val dirName = "module.login"
    val xlsName = "display.xlsx"
    val xlsWriteBean = XlsWriteBean.Builder
        .setRootDir(rootDir)
        .setDirName(dirName)
        .setXlsName(xlsName)
        .builder()
    XmlToXlsManager.getInstance().startParserXls(xlsWriteBean.mBuilder)

}