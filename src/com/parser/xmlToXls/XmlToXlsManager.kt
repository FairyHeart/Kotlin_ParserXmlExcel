package com.parser.xmlToXls

import com.parser.utils.IOUtils
import com.parser.xmlToXls.bean.CusRowBean
import com.parser.xmlToXls.bean.FloderBean
import com.parser.xmlToXls.bean.XlsWriteBean
import com.parser.xmlToXls.constant.Constants
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * xml转换为excel
 *
 * @author: Guazi.
 * @date  : 2020-01-02.
 */
class XmlToXlsManager private constructor() {
    private val mKeyList = ArrayList<String>() //用来string的key保存
    private val rowMap = mutableMapOf<String, Row?>()
    private lateinit var mWorkbook: Workbook


    private object Holder {
        internal var writeXlsManager = XmlToXlsManager()
    }

    companion object {
        fun getInstance(): XmlToXlsManager {
            return Holder.writeXlsManager
        }
    }

    /**
     * 开始转换文件
     * @param mBuilder 根目录
     */
    fun startParserXls(mBuilder: XlsWriteBean.Builder) {
        val rootDir = mBuilder.rootDir
        val dirName = mBuilder.dirName
        val xlsName = mBuilder.xlsName
        val file = File(rootDir, dirName)
        var fos: FileOutputStream? = null
        if (file.exists()) {
            try {
                writeNewFile(rootFile = file)
                //第一步，创建一个webbook，对应一个Excel文件
                if (xlsName.toLowerCase().endsWith("xlsx")) {
                    mWorkbook = XSSFWorkbook()
                } else {
                    mWorkbook = HSSFWorkbook()
                }
                //如果已经有了，先删除
                val filePath = rootDir + File.separator + dirName
                val xlsFile = File(filePath, xlsName)
                if (xlsFile.exists()) {
                    xlsFile.delete()
                }
                //开始写数据到 xls
                startWriteWorkbook(file)
                fos = FileOutputStream(File(filePath, xlsName))
                mWorkbook.write(fos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                IOUtils.close(fos)
            }
        } else {
            println("找不到目录 $rootDir${File.separator}$dirName")
        }
    }

    private fun writeNewFile(rootFile: File) {
        if (rootFile.exists()) {
            val floderPaths = mutableListOf<String>()
            val files = rootFile.walk()
                .maxDepth(20)
                .filter {
                    it.isDirectory
                }
                .filter {
                    it.absolutePath.contains("src\\main\\res")
                }
                .filter {
                    it.name.contains("values", ignoreCase = true)
                }
            for (file in files) {
                if (file.isDirectory && file.name.contains("values")) {
                    val floderName = getLangByFloder(getFloderName(file), file)
                    if (!floderName.isNullOrBlank()) {
                        floderPaths.add(file.absolutePath)
                    }
                }
            }

            floderPaths.forEachIndexed { index, it ->
                var newFile = File(it, "new_strings.xml")
                if (!newFile.exists()) {
                    newFile.createNewFile()
                    newFile.appendText("<resources>")
                } else {
                    newFile.delete()
                    newFile.appendText("<resources>")
                }
                val floderFile = File(it)
                if (floderFile.exists()) {
                    val stringFiles = floderFile.walk()
                        .filter {
                            it.name.contains("strings")
                        }
                    stringFiles.forEach {
                        val txt = it.readText()
                            .replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "")
                            .replace("<?xml version=\"1.0\" ?>", "")
                            .replace("<resources><?xml version=\"1.0\" encoding=\"utf-8\"?>", "")
                            .replace("<resources>", "")
                            .replace("</resources>", "")
                        newFile.appendText(txt)
                    }
                }
                newFile.appendText("</resources>")
            }
        }
    }

    /**
     * 开始写数据，主要配置第一行，还有写相关数据
     * @param rootFile
     */
    private fun startWriteWorkbook(rootFile: File) {
        if (rootFile.exists()) {
            //设置自动换行
            val cellStyle = mWorkbook.createCellStyle()
            cellStyle.wrapText = true
            val createHelper = mWorkbook.creationHelper
            val bean = getFloderBean(rootFile) //获取到所有路径,并保存到 floderBean 中
            if (bean != null) {
                mWorkbook.createSheet("sheetName")//第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
                val floderPaths = bean.floderPaths
                if (!floderPaths.isNullOrEmpty()) {
                    for (floderPath in floderPaths) { //浏览文件夹
                        val floderFile = File(floderPath)
                        if (floderFile.exists()) {
                            val valueNames = getFloderFileNameList(floderPath)
                            for (valueName in valueNames) { //浏览各个文件夹中的文件
                                if (!valueName.contains("array") && !valueName.contains("strings")) {
                                    continue
                                }
//                                if (valueName.contains("array")) {
//                                    this.readArrayStringData(valueNames, valueName, bean, createHelper)
//                                } else {
//                                    this.readStringData(valueNames, valueName, bean, createHelper)
//                                }
                                if (valueName == "new_strings.xml") {
                                    this.readStringData(valueNames, valueName, bean, createHelper)
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun readStringData(
        valueNames: List<String>,
        valueName: String,
        bean: FloderBean,
        createHelper: CreationHelper
    ) {
        var sheet: Sheet? = null
        var row: Row? = null
        var startColume = 1

        sheet = mWorkbook.getSheet(Constants.STRING_NAME)
        if (sheet == null) {
            mWorkbook.setSheetName(0, Constants.STRING_NAME)
            sheet = mWorkbook.getSheet(Constants.STRING_NAME)
        }
        row = sheet.createRow(0)//在sheet中添加表头第0行
        row.createCell(0).setCellValue(Constants.STRING_TYPE_DECARE)

        sheet.setColumnWidth(0, 30 * 256)
        //开始写其他行的数据
        for (langIndex in 0 until (bean.languages?.size ?: 0)) {
            row.createCell(langIndex + startColume).setCellValue(//创建第0行的第几列
                createHelper.createRichTextString(bean.languages?.get(langIndex))
            )
            val lists = parseStringXml(bean.floderPaths!![langIndex], valueName)
            if (lists != null && lists.isNotEmpty()) {
                writeDataToXls(lists, sheet, langIndex)
            }
        }
    }

    private fun readArrayStringData(
        valueNames: List<String>,
        valueName: String,
        bean: FloderBean,
        createHelper: CreationHelper
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
                writeArrayDataToXls(lists, sheet, langIndex)
            }
        }
    }

    /**
     * 获取路径下数据
     * @param rootFile
     * @return
     */
    private fun getFloderBean(rootFile: File): FloderBean {
        val bean = FloderBean()
        bean.path = rootFile.absolutePath
        bean.languages = ArrayList()
        bean.floderPaths = ArrayList()
        if (rootFile.exists()) {
            val files = rootFile.walk()
                .maxDepth(5)
                .filter {
                    it.isDirectory
                }
                .filter {
                    it.absolutePath.contains("src\\main\\res")
                }
                .filter {
                    it.name.contains("values", ignoreCase = true)
                }
            for (file in files) {
                if (file.isDirectory && file.name.contains("values")) {
                    val floderName = getLangByFloder(getFloderName(file), file)
                    if (!floderName.isNullOrBlank()) {
                        bean.languages!!.add(floderName)
                        bean.floderPaths!!.add(file.absolutePath)
                    }
                }
            }
        }
        return bean
    }


    /**
     * 写数据到 xls 中
     * @param lists
     * @param sheet
     * @param rIndex 第几列，从零开始
     */
    private fun writeDataToXls(lists: List<CusRowBean>, sheet: Sheet?, rIndex: Int) {
        val itemCountList = ArrayList<Int>() //记录item的个数
        val cellStyle = mWorkbook.createCellStyle()//创建单元格
        cellStyle.wrapText = true
        val size = lists.size
        //列
        for (cIndex in 0 until size) {
            val cusRow = lists[cIndex]
            /**
             * 把 string 的数据，写进 xls 中
             * 这里的原理是：
             * 1、当cIndex == 0,即当前为value文件夹，以这个为基准，保存 key 和 row ，row 用map保存，记录第几行
             * 2、当cIndex != 0,即其他文件夹，但是又不能保证 strings.xml 和 values 里的 strings.xml 是一致的
             * 所以，通过第一次保存的 key，获取到对应的 index，然后通过这个index，即可获取到对应的 row，这样就不会重复了
             */
            if (rIndex == 0) {
                //从第二行开始
                val valueRow = sheet?.createRow(cIndex + 1)//第一行
                valueRow?.createCell(0)?.setCellValue(cusRow.key)//先写key
                val cell = valueRow?.createCell(rIndex + 1)
                cell?.cellStyle = cellStyle
                sheet?.setColumnWidth(cIndex + 1, 25 * 256)
                cell?.setCellValue(cusRow.value) //再写value
                mKeyList.add(cusRow.key)
                rowMap[cusRow.key] = valueRow
            } else { //array
                val index = getIndexFromKey(cusRow.key)
                if (index != -1) {
                    //找到对应的row
                    var valueRow = sheet?.getRow(index + 1)
                    if (sheet == null) {
                        valueRow = sheet?.createRow(index + 1)
                    }

                    val cell = valueRow?.createCell(rIndex + 1)
                    cell?.cellStyle = cellStyle
                    sheet?.setColumnWidth(index, 35 * 256)
                    cell?.setCellValue(cusRow.value)
                }
            }

        }
    }

    /**
     * 写数据到 xls 中
     * @param lists
     * @param sheet
     * @param rIndex
     */
    private fun writeArrayDataToXls(lists: List<CusRowBean>, sheet: Sheet?, rIndex: Int) {
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
            if (cusRow.key != null) {
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


    /**
     * 使用 DOM 解析 xml
     * @param path
     * @param stringName
     * @return
     */
    private fun parseStringXml(path: String, stringName: String): List<CusRowBean> {
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
                    if (element != null) {
                        val cusRow = CusRowBean()
                        cusRow.key = element.attributeValue("name")
                        cusRow.value = element.stringValue
                        lists.add(cusRow)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lists
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
                    if (element != null) {
                        val cusRow = CusRowBean()
                        cusRow.key = element.attributeValue("name")
                        cusRow.value = element.stringValue
                        if (cusRow.value != null) {
                            val items = cusRow.value.split("\n")
                            if (items != null) {
                                cusRow.items = ArrayList()
                                for (item in items) {
                                    if (item != null && item.length > 1) {
                                        cusRow.items.add(item)
                                    }
                                }
                            }
                        }
                        lists.add(cusRow)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lists
    }

    /**
     * 获取文件夹的名字
     * @param file
     * @return
     */
    private fun getFloderName(file: File): String? {
        val path = file.absolutePath
        //window 路径转换
        val paths = path.replace("\\", "/").split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (paths != null && paths.isNotEmpty()) {
            paths[paths.size - 1]
        } else null
    }

    /**
     * 获取文件夹中的文件名
     * @param path
     * @return
     */
    private fun getFloderFileNameList(path: String): List<String> {
        val dir = File(path)
        val lists = ArrayList<String>()
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                val length = files.size
                for (i in 0 until length) {
                    val file = files[i]
                    val paths = file.absolutePath.toString()
                        .replace("\\", "/").split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    lists.add(paths[paths.size - 1])
                }
            }
        }
        return lists
    }


    /**
     * 获取key中的 index
     * @param key
     * @return
     */
    private fun getIndexFromKey(key: String): Int {
        if (mKeyList.isNotEmpty()) {
            for (rowkey in mKeyList) {
                if (key == rowkey) {
                    return mKeyList.indexOf(key)
                }
            }
        }
        return -1
    }

    /**
     * 通过语言，获取对应的文件夹名称
     * @param floderName
     * @return
     */
    private fun getLangByFloder(floderName: String?, file: File): String? {
        for (entry in Constants.languageMap.values) {
            if (entry == floderName) {
                if (file.absolutePath.contains("novotill", ignoreCase = true)) {
                    return "novotill_${entry}"
                }
                return entry
            }
        }
        return null
    }

}