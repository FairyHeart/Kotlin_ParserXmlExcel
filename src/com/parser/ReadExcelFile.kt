package com.parser

import com.parser.bean.ParserBean
import com.parser.utils.IOUtils
import jxl.Workbook
import jxl.WorkbookSettings
import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val SRC = "d://parserExcel"

const val SRC_FILE = "$SRC/kds.xls"

/**
 * 表格内容转换为string.xml文件
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */
class ReadExcelFile {

    fun startParserFile() {
        val mapList = readExcelFile()
        val namelists = readCol5Name()
        writeXmlFile(mapList, namelists)
    }

    /**
     * 读取excel表名，并保存在List列表中
     * @return
     */
    private fun readSheetName(): List<String>? {
        var input: InputStream? = null
        var wb: Workbook? = null
        var list: MutableList<String>? = null
        try {
            input = FileInputStream(SRC_FILE)
            if (null != input) {
                list = ArrayList()
                wb = Workbook.getWorkbook(input)
                val sheets = wb!!.sheets
                val sheetLen = sheets.size
                for (j in 0 until sheetLen) {
                    list.add(sheets[j].name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != wb) {
                wb!!.close()
            }
            IOUtils.close(input)
        }
        return list
    }

    /**
     * Map<Integer></Integer>, BeanValue>,保持单张表中第三行开始，第2列和第5列的值（TreeMap可以按顺序加载）
     * 返回Map<Integer></Integer>, Map<Integer></Integer>, BeanValue>>，保证Integer和表的索引一一对应
     * 也可保持为List<Map></Map><Integer></Integer>, BeanValue>>
     * @return
     */
    fun readExcelFile(): Map<Int, Map<Int, ParserBean>>? {
        var input: InputStream? = null
        var wb: Workbook? = null
        var mapList: MutableMap<Int, Map<Int, ParserBean>>? = HashMap()
        var maps: MutableMap<Int, ParserBean>? = TreeMap()
        var list: List<Map<Int, ParserBean>>? = ArrayList()
        var woSettings: WorkbookSettings? = null
        try {
            input = FileInputStream(SRC_FILE)
            if (null != input) {
                woSettings = WorkbookSettings()
                woSettings.encoding = "UTF-8"//设置编码格式
                wb = Workbook.getWorkbook(input, woSettings)
                val sheets = wb!!.sheets //工作簿的数量
                val sheetLen = sheets.size
                for (j in 0 until sheetLen) {
                    val rs = wb!!.getSheet(j)
                    val rowNum = rs.rows //行数
                    val colNum = rs.columns //列数
                    maps = TreeMap<Int, ParserBean>()
                    for (i in 2 until rowNum) {
                        val cell = rs.getRow(i)
                        if (cell[3].contents == null || cell[3].contents.trim() == "") {
                        } else {
                            val beanValue = ParserBean(cell[2].contents, cell[3].contents)
                            maps!![i] = beanValue
                        }
                    }
                    if (maps!!.size > 0) {
                        mapList!![j] = maps
                        System.out.println(sheets[j].name)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != wb) {
                wb!!.close()
            }
            IOUtils.close(input)
        }
        return mapList
    }


    /**
     * 读取第五列的标题名，并保持在List中
     * @return
     */
    fun readCol5Name(): List<String>? {
        var input: InputStream? = null
        var wb: Workbook? = null
        var list: MutableList<String>? = null
        try {
            input = FileInputStream(SRC_FILE)
            if (null != input) {
                list = ArrayList()
                wb = Workbook.getWorkbook(input)
                val sheets = wb!!.sheets
                val sheetLen = sheets.size
                for (j in 0 until sheetLen) {
                    val rs = wb!!.getSheet(j)
                    val cell = rs.getRow(0)
                    val packageName = cell[3].contents
                    list.add(packageName)
                    // System.out.println(packageName);
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != wb) {
                wb!!.close()
            }
            IOUtils.close(input)
        }
        return list
    }

    /**
     * 返回DocumentBuilder
     * @return
     */
    private fun getDocumentBuilder(): DocumentBuilder? {
        val dbFactory = DocumentBuilderFactory.newInstance()
        var dbBuilder: DocumentBuilder? = null
        try {
            dbBuilder = dbFactory.newDocumentBuilder()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        }

        return dbBuilder
    }

    /**
     * 将所读excel的数据写入xml中，并按<String></String>格式保存
     * @param mapList
     * @param nameList
     */
    fun writeXmlFile(
        mapList: Map<Int, Map<Int, ParserBean>>?, nameList: List<String>?
    ) {
        if (mapList.isNullOrEmpty()) {
            return
        }
        if (nameList.isNullOrEmpty()) {
            return
        }
        val db = getDocumentBuilder()
        var document: Document? = null
        val iteratorMap = mapList
            .entries.iterator()
        // int i = 0;
        while (iteratorMap.hasNext()) {
            val entryMap = iteratorMap
                .next()
            val i = entryMap.key
            val map = entryMap.value
            document = db!!.newDocument()
            document!!.setXmlStandalone(true)
            val resource = document!!.createElement("resource")//创建元素节点
            resource.setAttribute(
                "xmlns:xliff", "urn:oasis:names:tc:xliff:document:1.2"
            )
            document!!.appendChild(resource)//添加元素
            val iterator = map.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val beanValue = entry.value
                val key = beanValue.key
                val value = beanValue.value
                if (value == null || value!!.trim({ it <= ' ' }) == "") {
                } else {
                    val string = document!!.createElement("string")
                    string.setAttribute("name", key)
                    string.appendChild(document!!.createTextNode(value))//添加值
                    resource.appendChild(string)//添加子元素
                }
            }
            val nameStr = nameList[i]
            val packStr = nameStr
            val fileName = nameStr
            val file = File("$SRC/$packStr")
            if (!file.exists()) {
                file.mkdirs()
            }
            saveXmlData(document, packStr, fileName)
        }
    }

    private fun saveXmlData(
        document: Document, packStr: String,
        fileName: String
    ) {
        val tFactory = TransformerFactory.newInstance()
        try {
            val tFTransformer = tFactory.newTransformer()
            tFTransformer.setOutputProperty(OutputKeys.INDENT, "yes")
            tFTransformer.transform(
                DOMSource(document),
                StreamResult(
                    "$SRC/$packStr/$fileName"
                )
            )
        } catch (e: TransformerConfigurationException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

    }
}