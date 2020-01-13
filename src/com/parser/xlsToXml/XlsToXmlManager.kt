package com.parser.xlsToXml

import com.parser.utils.FileUtils
import com.parser.utils.IOUtils
import com.parser.xlsToXml.bean.XlsBean
import com.parser.xlsToXml.bean.XlsRowBean
import jxl.Workbook
import jxl.WorkbookSettings
import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


/**
 * 表格内容转换为string.xml文件
 *
 * @author: Guazi.
 * @date  : 2019-12-30.
 */
class XlsToXmlManager {

    private var failMap = mutableMapOf<String, MutableList<String>>()
    /**
     * 开始转换文件
     *
     * @param dir 根目录
     * @param fileName 文件名
     */
    fun startParserFile(dir: String, fileName: String) {
        val mapList = this.readExcelFile(dir = dir, fileName = fileName)
        if (fileName.contains(".")) {
            val putDir = fileName.substring(0, fileName.indexOf("."))
            val file = File("$dir/$putDir")
            if (file.exists()) {
                file.delete()
            } else {
                file.createNewFile()
            }
            this.writeXmlFile(xlsBeans = mapList, dir = file.absolutePath)
        } else {
            this.writeXmlFile(xlsBeans = mapList, dir = dir)
        }
    }

    /**
     * 转换目录下的所有excel文件
     *
     * @param dir 根目录
     */
    fun startParserDir(dir: String) {
        val dirs = File(dir).walk()
        val files = dirs.filter {
            it.isFile
        }.filter {
            it.name.endsWith(".xls", ignoreCase = true) || it.name.endsWith(".xlsx", ignoreCase = true)
        }
        files.forEach {
            startParserFile(dir = it.parent, fileName = it.name)
        }
    }


    /**
     * 读取excel内容
     * @param dir 根目录
     * @param fileName 文件名
     */
    private fun readExcelFile(dir: String, fileName: String): MutableList<XlsBean> {
        val failFile = FileUtils.createNewFile(dir, "未翻译资源.xml")
        var input: InputStream? = null
        var wb: Workbook? = null
        var woSettings: WorkbookSettings?
        var xlsBeans = mutableListOf<XlsBean>()
        try {
            input = FileInputStream("$dir\\$fileName")
            if (null != input) {
                woSettings = WorkbookSettings()
                woSettings.encoding = "UTF-8"//设置编码格式
                wb = Workbook.getWorkbook(input, woSettings)
                val sheets = wb!!.sheets //工作簿的数量
                val sheetLen = sheets.size
                for (j in 0 until sheetLen) {
                    failMap.clear()
                    var xlsBean = XlsBean()
                    xlsBean.sheetName = sheets[j].name
                    failFile.appendText("------------------- ${xlsBean.sheetName} --------------------\n")
                    println(xlsBean.sheetName)
                    var colFirstMap: MutableMap<Int, String> = HashMap()
                    var colMap: MutableMap<Int, ArrayList<XlsRowBean>> = HashMap()
                    val rs = wb!!.getSheet(j)
                    val rowNum = rs.rows //行数
                    val colNum = rs.columns //列数
                    for (i in 0 until rowNum) {//第二行开始读取内容
                        val cell = rs.getRow(i)
                        for (j in 0 until colNum) {
                            if (i == 0) {//读取第一行的标题
                                var title = rs.getRow(0)[j].contents.trim()
                                if (title.isNotBlank()) {
                                    colFirstMap[j] = title
                                }
                                failMap[title] = mutableListOf()
                            } else {
                                if (j > 0) {
                                    if (!colMap.containsKey(j)) {
                                        colMap[j] = ArrayList()
                                    }
                                    if (cell.size > j) {
                                        colMap[j]?.add(XlsRowBean(cell[0].contents, cell[j].contents))
                                    } else {
                                        colMap[j]?.add(XlsRowBean(cell[0].contents, ""))

                                        val failStr = "<string name=\"${cell[0].contents}\"></string>"
                                        failMap[colFirstMap[j]]?.add(failStr)
                                    }
                                }
                            }
                        }
                    }
                    failMap.forEach { (key, values) ->
                        if (!values.isNullOrEmpty()) {
                            failFile.appendText("                   $key                  \n")
                            values.forEach {
                                failFile.appendText("$it\n")
                            }
                        }
                    }
                    var contents: MutableMap<String, MutableList<XlsRowBean>> = mutableMapOf()
                    colMap.forEach { (key, values) ->
                        contents[colFirstMap[key] ?: "无标题"] = values
                    }
                    xlsBean.contents = contents
                    xlsBeans.add(xlsBean)
                    failFile.appendText("\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            IOUtils.close(input, wb)
        }
        return xlsBeans
    }

    /**
     * 将所读excel的数据写入xml中，并按<String></String>格式保存
     * @param xlsBeans excel解析的内容
     * @param dir 根目录
     */
    private fun writeXmlFile(xlsBeans: MutableList<XlsBean>, dir: String) {
        if (xlsBeans.isNullOrEmpty()) {
            return
        }
        xlsBeans.forEach {
            if (!it.contents.isNullOrEmpty()) {
                val db = getDocumentBuilder()
                var document: Document?
                it.contents?.forEach { key, values ->
                    document = db?.newDocument()
                    document?.xmlStandalone = true
                    val resource = document!!.createElement("resources")//创建元素节点
                    resource.setAttribute("xmlns:xliff", "urn:oasis:names:tc:xliff:document:1.2")
                    document?.appendChild(resource)//添加元素

                    if (values.isNotEmpty()) {
                        values.forEach { parserBean ->
                            val string = document!!.createElement("string")
                            string.setAttribute("name", parserBean.key)
                            string.appendChild(document!!.createTextNode(parserBean.value))//添加值
                            resource.appendChild(string)//添加子元素
                        }
                    }

                    val packStr = it.sheetName
                    val fileName = key
                    val file = File("$dir/$packStr")
                    if (!file.exists()) {
                        file.mkdirs()
                    }
                    saveXmlData(document, dir, packStr, fileName)
                }
            }
        }
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
     * 保存xml文件
     * @param document
     * @param dir 根目录
     * @param packStr 包名
     * @param fileName 文件名
     */
    private fun saveXmlData(document: Document?, dir: String, packStr: String, fileName: String) {
        val tFactory = TransformerFactory.newInstance()
        try {
            val tFTransformer = tFactory.newTransformer()
            tFTransformer.setOutputProperty(OutputKeys.INDENT, "yes")
            tFTransformer.transform(
                DOMSource(document), StreamResult(
                    "$dir/$packStr/$fileName.txt"
                )
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}