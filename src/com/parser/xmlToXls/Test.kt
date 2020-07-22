package com.parser.xmlToXls

import com.parser.xmlToXls.bean.XlsWriteBean

/**
 * string.xml转excel测试类
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
fun main() {
    val printDir = "E://stringToXml"
    val rootDir = "E://dfire-i18n-workspace"
//    val dirName = "mobile.res"
    val dirName = "cashline"
    val xlsName = "cashline.xlsx"
    val isMoreDire = true
    val xlsWriteBean = XlsWriteBean.Builder
        .setRootDir(rootDir)
        .setDirName(dirName)
        .setXlsName(xlsName)
        .setPrintDir(printDir)
        .setMoreDir(isMoreDire)
        .builder()
    XmlToXlsManager.getInstance().startParserXls(xlsWriteBean.mBuilder)

}