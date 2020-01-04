package com.parser.xmlToXls.bean

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
class XlsWriteBean(val mBuilder: Builder) {

    object Builder {
        lateinit var rootDir: String

        lateinit var xlsName: String

        lateinit var dirName: String

        fun setRootDir(rootDir: String): Builder {
            Builder.rootDir = rootDir
            return this
        }

        fun setXlsName(xlsName: String): Builder {
            Builder.xlsName = xlsName
            return this
        }

        fun setDirName(dirName: String): Builder {
            Builder.dirName = dirName
            return this
        }

        fun builder(): XlsWriteBean {
            return XlsWriteBean(this)
        }
    }
}