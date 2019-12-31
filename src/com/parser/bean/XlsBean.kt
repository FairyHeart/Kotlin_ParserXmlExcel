package com.parser.bean

/**
 * excel表格属性
 *
 * @author: Guazi.
 * @date  : 2019-12-31.
 */
class XlsBean {

    /**
     * 工作簿名字
     */
    var sheetName: String = ""

    /**
     * key = 第一列的标题 values = 每一列的内容，不包含第一列，第一列为内容的key
     */
    var contents: MutableMap<String, MutableList<ParserBean>>? = null
}