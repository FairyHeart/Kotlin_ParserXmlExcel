package com.compare

import com.parser.utils.FileUtils
import com.parser.xlsToXml.bean.XlsBean
import com.parser.xmlToXls.bean.CusRowBean
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * 从对比文件中获取所需文件的资源内容
 */
fun main() {
    val filePath = "F://stringToXml"
    val targetFileName = "strings_target_rTW.xml"
    val sourceFileName = "strings.xml"
    val printFileName = "new_strings.xml"

    val targetList = parseStringXml(filePath, targetFileName)
    val sourceList = parseStringXml(filePath, sourceFileName)
    val newList = compareFile(targetList, sourceList)

    writeXmlFile(newList, filePath, printFileName)
}

private fun compareFile(targetList: List<CusRowBean>, sourceList: List<CusRowBean>): List<CusRowBean> {
    val newList = mutableListOf<CusRowBean>()
    sourceList.forEach {
        val key = it.key
        var index = false
        targetList.forEach { row ->
            if (row.key == key) {
                it.value = row.value
                newList.add(it)
                index = true
            }
        }
        if (!index) {
            newList.add(it)
        }
    }
    return newList
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
 * 将所读excel的数据写入xml中，并按<String></String>格式保存
 * @param rows excel解析的内容
 * @param fileName 输出文件名字和路径
 */
private fun writeXmlFile(rows: List<CusRowBean>, dir: String, fileName: String) {
    if (rows.isNullOrEmpty()) {
        return
    }
    val db = getDocumentBuilder()
    val document: org.w3c.dom.Document? = db?.newDocument()
    document?.xmlStandalone = true
    val resource = document!!.createElement("resources")//创建元素节点
//    resource.setAttribute("xmlns:xliff", "urn:oasis:names:tc:xliff:document:1.2")
    document.appendChild(resource)//添加元素
    rows.forEach {
        val string = document.createElement("string")
        string.setAttribute("name", it.key)
        string.appendChild(document.createTextNode(it.value))//添加值
        resource.appendChild(string)//添加子元素
    }
    saveXmlData(document, dir, fileName)
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
 * @param dir
 * @param fileName 文件名
 */
private fun saveXmlData(document: org.w3c.dom.Document?, dir: String, fileName: String) {
    val tFactory = TransformerFactory.newInstance()
    try {
        val newDir = File(dir, fileName)
        if (newDir.exists()) {
            newDir.delete()
        }
        if (!newDir.exists()) {
            newDir.createNewFile()
        }
        val tFTransformer = tFactory.newTransformer()
        tFTransformer.setOutputProperty(OutputKeys.INDENT, "yes")
        tFTransformer.transform(DOMSource(document), StreamResult("$dir/$fileName"))
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}