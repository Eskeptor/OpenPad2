package com.esk.openpadnew.DataType

import com.esk.openpadnew.*
import java.io.File

/**
 * 폴더 오브젝트
 * @param path 폴더의 경로
 * @param isBig 크게보기 유무
 */
class FolderObject(val path: String, private val isBig: Boolean) {
    /**
     * 폴더 타입용 enum class
     * @param value 타입 대조를 위한 값
     */
    enum class FolderType(val value: Int) {
        Primitive(0), Widget(1), Normal(2)
    }

    var fileCounts: Int = 0                             // 해당 폴더의 파일의 개수를 저장하는 변수
        private set
    var fileName: String = ""                           // 해당 폴더의 순수 이름
    var folderType: FolderType = FolderType.Normal      // 해당 폴더의 타입
        private set
    var folderLastModified: Long = 0L                   // 해당 폴더의 최근 수정 날짜
        private set

    init {
        val file = File(path)
        setFolderTypeAndName(file)
        setName(file)
        setFileCounts(file)
        setModified(file)
    }

    /**
     * 폴더의 이름을 설정하는 함수
     * @param file 폴더
     */
    private fun setName(file: File) {
        fileName = file.name
    }

    /**
     * 폴더 내에 존재하는 메모 파일의 개수를 세는 함수
     * @param file 폴더
     */
    private fun setFileCounts(file: File) {
        if (isBig) {
            val files: Array<File>? = file.listFiles { pathname ->
                if (pathname != null) {
                    return@listFiles pathname.isFile
                            && (pathname.name.endsWith(FILE_EXTENSION_TEXT) || pathname.name.endsWith(
                        FILE_EXTENSION_IMAGE
                    ))
                }
                return@listFiles false
            }
            if (files != null)
                fileCounts = files.size
        }
    }

    /**
     * 폴더의 타입을 이름에 맞게 설정하는 함수
     * @param file 폴더
     */
    private fun setFolderTypeAndName(file: File) {
        folderType = when (file.name) {
            APP_DEFAULT_FOLDER_NAME -> {
                FolderType.Primitive
            }
            APP_WIDGET_FOLDER_NAME -> {
                FolderType.Widget
            }
            else -> {
                FolderType.Normal
            }
        }
    }

    /**
     * 폴더의 수정된 날짜를 체크하여 저장하는 함수
     * @param file 폴더
     */
    private fun setModified(file: File) {
        folderLastModified = file.lastModified()
    }
}