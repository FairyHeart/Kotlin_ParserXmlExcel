package com.search

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-03-05.
 */
fun main() {
//    val rootDir = "D://studio_dfire3"
    val rootDir = "D://parserExcel"
//    val dirName = "mobile.res"
    val dirName = "cashline"
    val isMoreDire = true
    val xlsWriteBean = CompareBean.Builder
        .setRootDir(rootDir)
        .setDirName(dirName)
        .setMoreDir(isMoreDire)
        .builder()
    CompareManager.getInstance().startCompareFile(xlsWriteBean.mBuilder)

}