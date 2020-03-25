package com.`null`

import java.io.File

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-03-09.
 */
class Test {


}

fun main() {
    val rootDir = "D://testnull"
    val targeDir = "D://testnull/new"

    val rootFile = File(rootDir)
    val targeDirs = File(targeDir)
    if (!targeDirs.exists()) {
        targeDirs.mkdirs()
    }
    if (rootFile.exists()) {
        val targetFiles = rootFile.walk().maxDepth(2).filter {
            it.name.endsWith(".txt")
        }
        targetFiles.forEach {
            val lines = it.readLines()

            val file = File(targeDir, "${it.name}")
            if (file.exists()) {
                file.deleteRecursively()
            }
            file.createNewFile()
            lines.forEach { line ->
                var text = line
                    .replace(": ", ":")
//                .replace(" \$ ", "\$")
                    .replace("\$ ", "\$")
                    .replace("} <", "}<")
                    .replace(" <", "<")
                    .replace("> ", ">")
                    .replace(" >", ">")
                    .replace("< / c>", "</c>")
                    .replace(" ()", "()")
                    .replace(" + ", "+")
                    .replace(" +", "+")
                    .replace("#final", "#end")
                    .replace("~ ", "~")
                    .replace(" en \$", " in \$")
                    .replace("if (", "if(")
                    .replace(" ! =", " !=")
                    .replace("! =", "!=")
                    .replace("\${ahora","\${now")
                    .replace("#más","#else")
                    .replace("! {","!{")

                file.appendText(text)
                file.appendText("\n")
            }
        }

    }
}