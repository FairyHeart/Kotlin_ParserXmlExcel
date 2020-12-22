package com.parser.xmlToXls.constant

/**
 * 语言和目录配置
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
object Constants {

    const val ARRAY_TYPE_DECARE = "key"
    const val STRING_TYPE_DECARE = "key"
    const val STRING_NAME = "strings"
    const val ARRAY_NAME = "arrays"

    val languageMap = mutableMapOf(
        Pair("简体中文", "values"),
//        Pair("English", "values-en")
//        Pair("Spanish", "values-es"),
//        Pair("韩语", "values-ko"),
//        Pair("葡萄牙", "values-pt-rPT"),
//        Pair("西班牙", "values-th-rTH"),
        Pair("繁体中文/繁中", "values-zh-rTW")
    )
}