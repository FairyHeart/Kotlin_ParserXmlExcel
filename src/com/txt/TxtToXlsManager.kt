package com.txt

import com.parser.utils.IOUtils
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream


/**
 * txt每一行的类容读写到excel中
 *
 * https://www.cnblogs.com/janson071/p/10119935.html
 *
 * @author: Guazi.
 * @date  : 2020-01-02.
 */
class TxtToXlsManager private constructor() {

    private object Holder {
        internal var writeXlsManager = TxtToXlsManager()
    }

    companion object {
        fun getInstance(): TxtToXlsManager {
            return Holder.writeXlsManager
        }
    }

    /**
     * 开始转换文件
     */
    fun startParserXls(filePath: String, xlsName: String, lists: List<String>) {
        val file = File(filePath, xlsName)
        if (file.exists()) {
            file.deleteOnExit()
        }
        var fos: FileOutputStream? = null
        try {
            //创建工作簿
            var xssfWorkbook = XSSFWorkbook()

            //创建工作表
            val xssfSheet = xssfWorkbook.createSheet()

            //创建行
            var xssfRow: XSSFRow

            //创建列，即单元格Cell
            var xssfCell: XSSFCell

            //把List里面的数据写到excel中
            lists.forEachIndexed { index, value ->
                //从第一行开始写入
                xssfRow = xssfSheet.createRow(index)
                //创建每个单元格Cell，即列的数据
                xssfCell = xssfRow.createCell(0) //创建单元格
                xssfCell.setCellValue(value) //设置单元格内容
            }
            fos = FileOutputStream(File(filePath, xlsName))
            xssfWorkbook.write(fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            IOUtils.close(fos)
        }

    }


}