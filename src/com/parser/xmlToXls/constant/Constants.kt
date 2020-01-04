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
        Pair("简体中文/簡中", "values-zh-rCN"),
        Pair("繁体中文/繁中", "values-zh-rTW"),
        Pair("English", "values-en"),
        Pair("简体中文", "values"),
        Pair("Czech", "values-cs-rCZ"),
        Pair("Danish", "values-da-rDK"),
        Pair("Dutch", "values-nl"),
        Pair("Spanish", "values-es"),
        Pair("Finnish", "values-fi-rFI"),
        Pair("Portuguese", "values-pt"),
        Pair("French", "values-fr"),
        Pair("Deutsch", "values-de"),
        Pair("Greek", "values-el-rGR"),
        Pair("Italiano/Italian", "values-it"),
        Pair("日语/Japanese", "values-ja-rJP"),
        Pair("Norwegian", "values-nb-rNO"),
        Pair("Polski/Polish", "values-pl-rPL"),
        Pair("Romanian", "values-ro-rRO"),
        Pair("Russian", "values-ru-rRU"),
        Pair("Swedish", "values-sv-rSE"),
        Pair("Turkish", "values-tr-rTR"),
        Pair("Arabic", "values-ar"),
        Pair("Chinese (Simple)", "values-zh-rCN"),
        Pair("Chinese (Traditional)", "values-zh-rTW"),
        Pair("Hungarian", "values-hu-rHU"),
        Pair("Thai", "values-th-rTH"),
        Pair("Persian", "values-fa"),
        Pair("Vietnam/Vietnamese", "values-vi-rVN"),
        Pair("Korea/Korean", "values-ko-rKR"),
        Pair("Deutsch (German)", "values-de")
    )
}