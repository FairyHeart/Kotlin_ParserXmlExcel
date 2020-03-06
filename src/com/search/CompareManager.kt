package com.search

import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-03-05.
 */
class CompareManager {

    /**
     * 基准文件
     */
    private val defaultValue = "values"
    /**
     * 配置检测的语言文件夹
     */
    private var filterDir = mutableListOf(
        defaultValue,
        "values-en",
        "values-es",
        "values-ko",
        "values-pt-rPT",
        "values-th-rTH",
        "values-zh-rTW"
    )

    private object Holder {
        internal var writeXlsManager = CompareManager()
    }

    companion object {
        fun getInstance(): CompareManager {
            return Holder.writeXlsManager
        }
    }

    fun startCompareFile(mBuilder: CompareBean.Builder) {
        val rootDir = mBuilder.rootDir
        val dirName = mBuilder.dirName
        val file = File(rootDir, dirName)
        //获取工程目录列表
        val files = file.listFiles()
            .filter {
                it.isDirectory
            }.filter {
                !it.name.startsWith(".")
            }.filter {
                it.name != "gradle" && it.name != "build"
            }

        files.forEach {
            readFile(it, mBuilder.rootDir)
        }
    }

    private fun readFile(file: File, dir: String) {
        val valueMap = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()

        //获取单个目录下的values目录
        val files = file.walk()
            .maxDepth(10)
            .filter {
                it.isDirectory
            }
            .filter {
                it.absolutePath.contains("src\\main\\res")
            }
            .filter {
                !it.absolutePath.contains("novotill")
            }
            .filter {
                filterDir.contains(it.name)
            }
        files.forEach {
            //获取单个values下的strings.xml文件
            println("${it.path}")
            var fileMap = mutableMapOf<String, File>()
            val targeFiles = it.walkTopDown()
                .filter { file ->
                    !file.name.contains("arrays")
                }.filter { file ->
                    !file.name.contains("attrs")
                }.filter { file ->
                    !file.name.contains("colors")
                }.filter { file ->
                    !file.name.contains("styles")
                }.filter { file ->
                    file.name.contains("strings")
                }

            targeFiles.forEach { strFile ->
                val parentFileName = it.name
                if (!valueMap.containsKey(parentFileName) || valueMap[parentFileName] == null) {
                    valueMap[parentFileName] = mutableMapOf()
                }
                var strMap = valueMap[parentFileName]
                strMap?.set(strFile.name, parseStringXml(strFile))
            }
        }
        //默认资源，即values目录
        val defaultMap = valueMap[defaultValue] ?: return
        valueMap.remove(defaultValue)
        valueMap.forEach { (t, u) ->
            defaultMap.forEach { (stringXml, values) ->
                var unTranslated = mutableMapOf<String, String>()
                if (u.containsKey(stringXml)) {
                    //strings.xml里面具体内容比较
                    val enValues = u[stringXml]
                    values.forEach { (key, value) ->
                        //未翻译的内容
                        if (enValues?.containsKey(key) == false) {
                            unTranslated[key] = value
                        }
                    }
                } else {
                    //整个string.xml文件没有翻译
                    unTranslated = values
                }
                writeXmlFile(unTranslated, dir, "${file.name}-$t", stringXml)
            }
        }
    }

    /**
     * 将所读excel的数据写入xml中，并按<String></String>格式保存
     * @param values excel解析的内容
     * @param dir 根目录
     */
    private fun writeXmlFile(values: MutableMap<String, String>, dir: String, packStr: String, fileName: String) {
        if (values.isNullOrEmpty()) {
            return
        }
        val db = getDocumentBuilder()
        var document = db?.newDocument()
        document?.xmlStandalone = true
        val resource = document?.createElement("resources")//创建元素节点
        resource?.setAttribute("xmlns:xliff", "urn:oasis:names:tc:xliff:document:1.2")
        document?.appendChild(resource)//添加元素
        values.forEach { (key, value) ->
            val string = document!!.createElement("string")
            string.setAttribute("name", key)
            string.appendChild(document.createTextNode(value))//添加值
            resource?.appendChild(string)//添加子元素
        }
        saveXmlData(document, dir, packStr, fileName)
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
    private fun saveXmlData(document: org.w3c.dom.Document?, dir: String, packStr: String, fileName: String) {
        val tFactory = TransformerFactory.newInstance()
        try {
            val newFileName = "${fileName}.xml"
            val newDir = File("$dir/$packStr/$fileName")
            if (newDir.exists()) {
                newDir.deleteOnExit()
            }
            if (!newDir.exists()) {
                newDir.mkdirs()
            }
            val tFTransformer = tFactory.newTransformer()
            tFTransformer.setOutputProperty(OutputKeys.INDENT, "yes")
            tFTransformer.transform(DOMSource(document), StreamResult("${newDir.path}/$newFileName"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 使用 DOM 解析 xml
     * @param path
     * @param stringName
     * @return
     */
    private fun parseStringXml(file: File): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            if (file.exists()) {
                val read = SAXReader()
                val document: Document = read.read(file)
                val root: Element = document.rootElement//获取根元素
                //   List<Element> childElements = root.elements();//获取当前元素下的全部子元素
                val it: Iterator<Element> = root.elementIterator() as Iterator<Element>
                while (it.hasNext()) {
                    val element = it.next()
                    map[element.attributeValue("name")] = element.stringValue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }
}