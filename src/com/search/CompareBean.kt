package com.search

/**
 * 转换文件目录配置bean
 *
 * @author: Guazi.
 * @date  : 2020-01-04.
 */
class CompareBean(val mBuilder: Builder) {

    object Builder {
        /**
         * 跟目录
         */
        lateinit var rootDir: String
        /**
         * 需要输出的excel目录名字
         */
        lateinit var xlsName: String
        /**
         * 转换values存放目录
         */
        lateinit var dirName: String

        /**
         * 是否多目录读取
         */
        var isMoreDire = false

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

        fun setMoreDir(isMoreDire: Boolean): Builder {
            Builder.isMoreDire = isMoreDire
            return this
        }

        fun builder(): CompareBean {
            return CompareBean(this)
        }
    }
}