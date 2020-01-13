package com.parser.xmlToXls

import com.parser.xmlToXls.bean.CusRowBean
import com.parser.xmlToXls.bean.FloderBean
import com.parser.xmlToXls.constant.Constants
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.io.File
import java.util.*

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-01-13.
 */
object ArraysUtils {
    fun readArrayStringData(
        valueNames: List<String>,
        valueName: String,
        bean: FloderBean,
        createHelper: CreationHelper,
        mWorkbook: Workbook
    ) {
        var sheet: Sheet? = null
        var row: Row? = null
        var startColume = 1
        val size = valueNames.size

        if (size == 1) { //如果只有 array，那么sheet 从0开始
            mWorkbook.setSheetName(0, Constants.ARRAY_NAME)
            sheet = mWorkbook.getSheet(Constants.ARRAY_NAME)
        } else { //如果有string，则自动排到第二个
            sheet = mWorkbook.getSheet(Constants.ARRAY_NAME)
            if (sheet == null) {
                sheet = mWorkbook.createSheet(Constants.ARRAY_NAME)
            }
        }
        row = sheet.createRow(0)
        row.createCell(0).setCellValue(Constants.ARRAY_TYPE_DECARE)
        sheet.setColumnWidth(0, 30 * 256)
        startColume = 2
        //开始写其他行的数据
        for (langIndex in 0 until (bean.languages?.size ?: 0)) {
            row.createCell(langIndex + startColume).setCellValue(
                createHelper.createRichTextString(bean.languages?.get(langIndex))
            )
            val lists = parseArrayStringXml(bean.floderPaths!![langIndex], valueName)
            if (lists != null && lists.isNotEmpty()) {
                writeArrayDataToXls(lists, sheet, langIndex, mWorkbook)
            }
        }
    }

    /**
     * 使用 DOM 解析 xml
     * @param path
     * @param stringName
     * @return
     */
    private fun parseArrayStringXml(path: String, stringName: String): List<CusRowBean> {
        val lists = ArrayList<CusRowBean>()
        try {
            val file = File(path, stringName)
            if (file.exists()) {
                val read = SAXReader()
                val document: Document = read.read(file)
                val root: Element = document.rootElement//获取根元素
                //   List<Element> childElements = root.elements();//获取当前元素下的全部子元素
                val it: Iterator<Element> = root.elementIterator() as Iterator<Element>
                while (it.hasNext()) {
                    val element = it.next()
                    val cusRow = CusRowBean()
                    cusRow.key = element.attributeValue("name")
                    cusRow.value = element.stringValue
                    val items = cusRow.value.split("\n")
                    cusRow.items = ArrayList()
                    for (item in items) {
                        if (item.length > 1) {
                            cusRow.items.add(item)
                        }
                    }
                    lists.add(cusRow)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lists
    }

    /**
     * 写数据到 xls 中
     * @param lists
     * @param sheet
     * @param rIndex
     */
    private fun writeArrayDataToXls(lists: List<CusRowBean>, sheet: Sheet?, rIndex: Int, mWorkbook: Workbook) {
        val itemCountList = ArrayList<Int>() //记录item的个数
        val cellStyle = mWorkbook.createCellStyle()
        cellStyle.wrapText = true
        val size = lists.size
        //列
        for (cIndex in 0 until size) {
            val cusRow = lists[cIndex]
            /**
             * 把 array 的数据，写进 xls 中
             * 这里的原理是(先看生成的表格再来看这段思路)：
             * 1、首先先写 key 和 item，key在第一行，第一列；所以，当有数据过来的时候，我们首先，先判断是否是 key，如果是，
             * 则写上 key，并继续判断是否有 item，如果有，则把item逐行写上；
             * 2、当 item 写完，又遇到key，这时候，行的起始位置，应该是上一次的  key 加 item 的行数 再加1，补充标签
             */
            var index = cIndex + 1
            if (cIndex > 0) {
                var num = 0
                for (itemIndex in itemCountList.indices) {
                    num += itemCountList[itemIndex]
                }
                index = cIndex + 1 + num
            }
            val valueRow = sheet?.createRow(index)
            valueRow?.createCell(0)?.setCellValue(cusRow.key)
            if (cusRow.items != null && cusRow.items.isNotEmpty()) {
                val count = cusRow.items.size - 1
                itemCountList.add(count)
                index += 1
                println("count: $count ")
                for (cRow in 0 until count) {
                    val item = cusRow.items[cRow]
                    val itemIndex = index + cRow
                    var itemRow: Row? = null
                    itemRow = sheet?.getRow(itemIndex)
                    if (itemRow == null) {
                        itemRow = sheet?.createRow(itemIndex)

                    }
                    itemRow?.createCell(1)?.setCellValue("<item>")
                    val cell = itemRow?.createCell(rIndex + 2) //从第二列开始
                    cell?.cellStyle = cellStyle
                    sheet?.setColumnWidth(cIndex + 1, 35 * 256)
                    cell?.setCellValue(item)
                    println("item: " + itemRow?.getCell(0) + " " + itemRow?.getCell(1))
                }
            }
        }
    }

}