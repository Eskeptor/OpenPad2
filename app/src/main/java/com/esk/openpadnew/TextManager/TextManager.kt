package com.esk.openpadnew.TextManager

import com.esk.openpadnew.Util.LogBot
import io.github.mthli.knife.KnifeParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * 텍스트 I/O를 담당하는 클래스
 */
class TextManager {
    companion object {
        /**
         * 설정한 줄 수 만큼 해당 파일에서 내용을 가져온다.
         * 이때 반환 형식은 List<String>? 이다.
         * @param file 파일명
         * @param lines 줄 수
         */
        fun getLines(file: File?, lines: Int): List<String>? {
            if (file != null) {
                if (file.exists()) {
                    val token = "\n"
                    var origin: String = KnifeParser.fromHtml(openText(file.path)).toString()
                    val list = mutableListOf<String>()

                    for (i in 0 until lines) {
                        var start = origin.indexOf(token)
                        while (start == 0) {
                            origin = origin.substring(token.length)
                            start = origin.indexOf(token)
                        }
                        if (start == -1) {
                            list.add(origin)
                        } else {
                            list.add(origin.substring(0, start))
                            origin = origin.substring(start)
                        }
                    }
                    return list
                }
            }
            return null
        }


        /**
         * 파일에 내용을 저장한다.
         * @param path 파일 경로
         * @param string 저장할 내용
         */
        fun saveText(string: String?, path: String): Boolean {
            if (string != null) {
                var saveFileOutputStream: FileOutputStream? = null
                var saveFileChannel: FileChannel? = null
                var fileBuffer: ByteBuffer? = null
                try {
                    saveFileOutputStream = FileOutputStream(File(path))
                    saveFileChannel = saveFileOutputStream.channel
                    fileBuffer = ByteBuffer.allocate(string.toByteArray().size)
                    fileBuffer.put(string.toByteArray())
                    fileBuffer.flip()
                    saveFileChannel.write(fileBuffer)
                    return true
                } catch (e: Exception) {
                    LogBot.logName("saveText").logLevel(LogBot.Level.Debug).log("파일 저장 실패")
                } finally {
                    fileBuffer?.clear()
                    saveFileChannel?.close()
                    saveFileOutputStream?.close()
                }
            }

            return false
        }


        /**
         * 파일의 전체 내용을 불러온다.
         * @param path 파일의 경로
         */
        fun openText(path: String): String {
            var openFileInputStream: FileInputStream? = null
            var openFileChannel: FileChannel? = null
            var fileBuffer: ByteBuffer? = null
            try {
                openFileInputStream = FileInputStream(File(path))
                openFileChannel = openFileInputStream.channel
                fileBuffer = ByteBuffer.allocate(openFileChannel.size().toInt())
                if (openFileInputStream.available() != 0) {
                    openFileChannel.read(fileBuffer)
                    fileBuffer.flip()
                    return String(fileBuffer.array()).trim()
                }
            } catch (e: Exception) {
                LogBot.logName("openText").logLevel(LogBot.Level.Debug).log("파일 열기 실패")
            } finally {
                fileBuffer?.clear()
                openFileChannel?.close()
                openFileInputStream?.close()
            }

            return ""
        }
    }
}