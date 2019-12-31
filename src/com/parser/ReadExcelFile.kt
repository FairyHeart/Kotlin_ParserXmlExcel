package com.parser

import com.parser.bean.ParserBean
import com.parser.bean.XlsBean
import com.parser.utils.IOUtils
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
class ReadExcelFile {

    fun startParserFile(src: String, fileName: String) {
        val mapList = this.readExcelFile(src = src, fileName = fileName)
        this.writeXmlFile(xlsBeans = mapList, src = src)
    }

    /**
     * 读取excel表名，并保存在List列表中
     * @return
     */
    private fun readSheetName(src: String, fileName: String): List<String>? {
        var input: InputStream? = null
        var wb: Workbook? = null
        var list: MutableList<String>? = null
        try {
            input = FileInputStream("$src\\$fileName")
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
            IOUtils.close(input, wb)
        }
        return list
    }

    /**
     * 读取excel内容
     * @param src 跟抹零
     * @param fileName 文件名
     */
    private fun readExcelFile(src: String, fileName: String): MutableList<XlsBean> {
        var input: InputStream? = null
        var wb: Workbook? = null
        var woSettings: WorkbookSettings?
        var xlsBeans = mutableListOf<XlsBean>()
        try {
            input = FileInputStream("$src\\$fileName")
            if (null != input) {
                woSettings = WorkbookSettings()
                woSettings.encoding = "UTF-8"//设置编码格式
                wb = Workbook.getWorkbook(input, woSettings)
                val sheets = wb!!.sheets //工作簿的数量
                val sheetLen = sheets.size
                for (j in 0 until sheetLen) {
                    var xlsBean = XlsBean()
                    xlsBean.sheetName = sheets[j].name

                    var colFirstMap: MutableMap<Int, String> = HashMap()
                    var colMap: MutableMap<Int, ArrayList<ParserBean>> = HashMap()
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
                            } else {
                                if (j > 0) {
                                    if (!colMap.containsKey(j)) {
                                        colMap[j] = ArrayList()
                                    }
                                    colMap[j]?.add(ParserBean(cell[0].contents, cell[j].contents))
                                }
                            }
                        }
                    }
                    var contents: MutableMap<String, MutableList<ParserBean>> = mutableMapOf()
                    colMap.forEach { key, values ->
                        contents[colFirstMap[key] ?: "无标题"] = values
                    }
                    xlsBean.contents = contents
                    xlsBeans.add(xlsBean)
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
     * @param src 跟目录
     */
    private fun writeXmlFile(xlsBeans: MutableList<XlsBean>, src: String) {
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
                    val file = File("$src/$packStr")
                    if (!file.exists()) {
                        file.mkdirs()
                    }
                    saveXmlData(document, src, packStr, fileName)
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
     * @param src 跟目录
     * @param packStr 包名
     * @param fileName 文件名
     */
    private fun saveXmlData(document: Document?, src: String, packStr: String, fileName: String) {
        val tFactory = TransformerFactory.newInstance()
        try {
            val tFTransformer = tFactory.newTransformer()
            tFTransformer.setOutputProperty(OutputKeys.INDENT, "yes")
            tFTransformer.transform(
                DOMSource(document), StreamResult(
                    "$src/$packStr/$fileName.txt"
                )
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}