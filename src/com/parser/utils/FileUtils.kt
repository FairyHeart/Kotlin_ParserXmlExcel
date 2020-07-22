package com.parser.utils

import java.io.*

/**
 *
 *
 * @author: Guazi.
 * @date  : 2020-01-13.
 */
object FileUtils {

    /**
     * 创建文件或者文件夹，如果文件存在，先清空内容
     * @param dir
     * @param fileName
     */
    fun createNewFile(dir: String, fileName: String): File {
        this.createNewDir(dir)
        val file = File(dir, fileName)
        if (file.exists()) {
            file.delete()
        } else {
            file.createNewFile()
        }
        return file
    }

    fun createNewDir(dir: String, dirName: String? = null): File {
        val dirFile = if (dirName.isNullOrBlank()) {
            File(dir)
        } else {
            File(dir, dirName)
        }
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        return dirFile
    }

    /**
     * 如果文件存在,创建文件或者文件夹
     * @param dir
     * @param fileName
     */
    fun careteFileNoExit(dir: String, fileName: String): File {
        val file = File(dir, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    /**
     * 获取文件夹的名字
     * @param file
     * @return
     */
    fun getFolderName(file: File, lastIndex: Int): String? {
        val path = file.absolutePath
        //window 路径转换
        val paths = path.replace("\\", "/").split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (paths.isNotEmpty()) {
            paths[paths.size - lastIndex]
        } else null
    }

    /**
     * 复制文件
     *
     * @param srcFilePath  源文件路径
     * @param destFilePath 目标文件路径
     * @return `true`: 复制成功<br></br>`false`: 复制失败
     */
    fun copyFile(srcFilePath: String, destFilePath: String): Boolean {
        return copyFile(
            getFileByPath(srcFilePath),
            getFileByPath(destFilePath)
        )
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    fun getFileByPath(filePath: String): File? {
        return if (filePath.isNullOrBlank()) null else File(filePath)
    }


    /**
     * 复制文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @return `true`: 复制成功<br></br>`false`: 复制失败
     */
    fun copyFile(srcFile: File?, destFile: File?): Boolean {
        return copyOrMoveFile(srcFile, destFile, false)
    }

    /**
     * 复制或移动文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param isMove   是否移动
     * @return `true`: 复制或移动成功<br></br>`false`: 复制或移动失败
     */
    private fun copyOrMoveFile(srcFile: File?, destFile: File?, isMove: Boolean): Boolean {
        if (srcFile == null || destFile == null) return false
        // 源文件不存在或者不是文件则返回false
        if (!srcFile.exists() || !srcFile.isFile) return false
        // 目标文件存在且是文件则返回false
        if (destFile.exists() && destFile.isFile) return false
        // 目标目录不存在返回false
        if (!createOrExistsDir(destFile.parentFile)) return false
        return try {
            writeFileFromIS(
                destFile,
                FileInputStream(srcFile),
                false
            ) && !(isMove && !deleteFile(srcFile))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }

    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    fun createOrExistsDir(file: File?): Boolean {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    /**
     * 将输入流写入文件
     *
     * @param file   文件
     * @param input     输入流
     * @param append 是否追加在文件末
     * @return `true`: 写入成功<br></br>`false`: 写入失败
     */
    fun writeFileFromIS(file: File?, input: InputStream?, append: Boolean): Boolean {
        if (file == null || input == null) return false
        if (!createOrExistsFile(file)) return false
        var os: OutputStream? = null
        return try {
            os = BufferedOutputStream(FileOutputStream(file, append))
            val data = ByteArray(1024)
            var len: Int
            do {
                len = input.read(data, 0, 1024)
                if (len != -1) os.write(data, 0, len) else break
            } while (true)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            IOUtils.close(input, null)
            IOUtils.close(os, null)

        }
    }

    /**
     * 删除文件
     *
     * @param file 文件
     * @return `true`: 删除成功<br></br>`false`: 删除失败
     */
    fun deleteFile(file: File?): Boolean {
        return file != null && (!file.exists() || file.isFile && file.delete())
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists()) return file.isFile
        if (!createOrExistsDir(file.parentFile)) return false
        return try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }

    }
}