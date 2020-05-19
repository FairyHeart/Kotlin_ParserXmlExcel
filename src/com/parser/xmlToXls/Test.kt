package com.parser.xmlToXls

import com.parser.xmlToXls.bean.XlsWriteBean

/**
 * string.xml转excel测试类
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
fun main() {
//    val rootDir = "D://studio_dfire3"
    val rootDir = "D://parserExcel"
//    val dirName = "mobile.res"a
    val dirName = "cashline"
    val xlsName = "cashline.xlsx"
    val isMoreDire = true
    val xlsWriteBean = XlsWriteBean.Builder
        .setRootDir(rootDir)
        .setDirName(dirName)
        .setXlsName(xlsName)
        .setMoreDir(isMoreDire)
        .builder()
    XmlToXlsManager.getInstance().startParserXls(xlsWriteBean.mBuilder)

}