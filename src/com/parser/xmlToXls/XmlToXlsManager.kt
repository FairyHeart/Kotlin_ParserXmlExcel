package com.parser.xmlToXls

import com.parser.utils.FileUtils
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
    private lateinit var mWorkbook: Workbook
    private lateinit var failFile: File

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
                failFile = FileUtils.createNewFile(file.absolutePath, "默认资源没有资源.xml")
                this.writeNewFile(rootFile = file)
                //第一步，创建一个webbook，对应一个Excel文件
                mWorkbook = if (xlsName.toLowerCase().endsWith("xlsx")) {
                    XSSFWorkbook()
                } else {
                    HSSFWorkbook()
                }
                //如果已经有了，先删除
                val filePath = rootDir + File.separator + dirName
                val xlsFile = File(filePath, xlsName)
                if (xlsFile.exists()) {
                    xlsFile.delete()
                }
                //开始写数据到 xls
                if (mBuilder.isMoreDire) {
                    val fils = file.listFiles()
                        .filter {
                            it.isDirectory
                        }.filter {
                            !it.name.startsWith(".")
                        }.filter {
                            it.name != "gradle" && it.name != "build"
                        }
                    fils.forEach { file ->
                        this.startWriteWorkbook(file)
                    }
                } else {
                    this.startWriteWorkbook(file)
                }
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
            val strFiles = mutableListOf<String>()
            val files = rootFile.walk()
                .maxDepth(300)
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
                    val folderName = getLangByFloder(getFloderName(file), file)
                    if (!folderName.isNullOrBlank()) {
                        strFiles.add(file.absolutePath)
                    }
                }
            }

            strFiles.forEachIndexed { index, it ->
                var newFile = File(it, "new_strings.xml")
                if (!newFile.exists()) {
                    newFile.createNewFile()
                    newFile.appendText("<resources>")
                } else {
                    newFile.delete()
                    newFile.appendText("<resources>")
                }
                val folderFile = File(it)
                if (folderFile.exists()) {
                    val stringFiles = folderFile.walk()
                        .filter {
                            it.name.contains("strings")
                        }
                    stringFiles.forEach {
                        val txt = it.readText()
                            .replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "", ignoreCase = true)
                            .replace("<?xml version=\"1.0\" ?>", "", ignoreCase = true)
                            .replace("<resources><?xml version=\"1.0\" encoding=\"utf-8\"?>", "", ignoreCase = true)
                            .replace("<resources>", "", ignoreCase = true)
                            .replace("</resources>", "", ignoreCase = true)
                            .replace("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">","",ignoreCase = true)
                            .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?><resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">","",ignoreCase = true)
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
        mKeyList.clear()
        if (rootFile.exists()) {
            //设置自动换行
            val cellStyle = mWorkbook.createCellStyle()
            cellStyle.wrapText = true
            val createHelper = mWorkbook.creationHelper
            val bean = getFloderBean(rootFile) //获取到所有路径,并保存到 floderBean 中
            if (bean != null) {
                mWorkbook.createSheet(rootFile.name)//第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
                val folderPaths = bean.floderPaths
                if (!folderPaths.isNullOrEmpty()) {
//                    for (folderPath in folderPaths) { //浏览文件夹
                    for (langIndex in 0 until (folderPaths?.size ?: 0)) {
                        val folderPath = folderPaths[langIndex]
                        val folderFile = File(folderPath)
                        if (folderFile.exists()) {
                            val valueNames = getFloderFileNameList(folderPath)
                            for (valueName in valueNames) { //浏览各个文件夹中的文件
                                if (!valueName.contains("array") && !valueName.contains("strings")) {
                                    continue
                                }
//                                if (valueName.contains("array")) {
//                                    ArraysUtils.readArrayStringData(valueNames, valueName, bean, createHelper, mWorkbook)
//                                } else {
//                                    this.readStringData(valueNames, valueName, bean, createHelper)
//                                }
                                if (valueName == "new_strings.xml") {
                                    this.readStringData(valueName, bean, createHelper, langIndex, rootFile.name)
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun readStringData(
        valueName: String,
        bean: FloderBean,
        createHelper: CreationHelper,
        langIndex: Int,
        sheetName: String
    ) {
        var sheet: Sheet? = null
        var row: Row? = null
        var startColume = 1

        sheet = mWorkbook.getSheet(sheetName)
        if (sheet == null) {
            mWorkbook.setSheetName(0, sheetName)
            sheet = mWorkbook.getSheet(sheetName)
        }
        row = sheet.createRow(0)//在sheet中添加表头第0行
        row.createCell(0).setCellValue(Constants.STRING_TYPE_DECARE)

        sheet.setColumnWidth(0, 30 * 256)
        //开始写其他行的数据
        for (langIndex in 0 until (bean.languages?.size ?: 0)) {
            row.createCell(langIndex + startColume).setCellValue(//创建第0行的第几列
                createHelper.createRichTextString(bean.languages?.get(langIndex))
            )
        }
        val path = bean.floderPaths!![langIndex]
        val lists = parseStringXml(path, valueName)
        if (lists.isNotEmpty()) {
            writeDataToXls(lists, sheet, langIndex, "$path $valueName")
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
                .maxDepth(10)
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
                    val folderName = getLangByFloder(getFloderName(file), file)
                    if (!folderName.isNullOrBlank()) {
                        bean.languages!!.add(folderName)
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
    private fun writeDataToXls(lists: List<CusRowBean>, sheet: Sheet?, rIndex: Int, tipName: String) {
        val cellStyle = mWorkbook.createCellStyle()//创建单元格
        cellStyle.wrapText = true
        val size = lists.size
        failFile.appendText("------------------- $tipName --------------------\n")
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
                } else {
                    failFile.appendText("<string name=\"${cusRow.key}\">${cusRow.value}</string>\n")
                    println("${cusRow.key},${cusRow.value}")
                }
            }
        }
        failFile.appendText("\n")
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
                    val cusRow = CusRowBean()
                    cusRow.key = element.attributeValue("name")
                    cusRow.value = element.stringValue
                    lists.add(cusRow)
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
        return if (paths.isNotEmpty()) {
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