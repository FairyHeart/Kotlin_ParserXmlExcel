package com.parser.xmlToXls.bean

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
class CusRow {

    lateinit var key: String

    lateinit var value: String

    var isArray: Boolean = false

    lateinit var items: MutableList<String>
}