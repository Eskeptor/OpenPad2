package com.esk.openpadnew.DataType

import com.esk.openpadnew.*
import com.esk.openpadnew.TextManager.TextManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// 한국 날짜용 String Format 상수
private const val DATE_FORMAT_MAIN_KOREA: String = "yyyy년 MM월 dd일 hh:mm a"
// 미국 날짜용 String Format 상수
private const val DATE_FORMAT_MAIN_USA: String = "MM/dd/yyyy hh:mm a"
// 유럽 날짜용 String Format 상수
private const val DATE_FORMAT_MAIN_UK: String = "dd/MM/yyyy hh:mm a"


/**
 * Main 액티비티에서 사용할 메인파일 오브젝트
 * @param file 파일
 * @param imgTitleName 해당 파일이 이미지 메모일 때 보여줄 타이틀 명
 * @param locale 로케일
 */
class MainFileObject(private var file: File, private val locale: String, private val imgTitleName: String) {

    enum class FileType(val value: Int) {
        Text(0), Image(1);
    }

    var fileTitle: String = ""                  // 파일의 제목
    var filePath: String = ""                   // 파일의 경로(Full Path)
    var oneLinePreview: String = ""             // 한 줄 요약
    var modifiedDate: String = ""               // 수정 날짜
    var fileType: FileType = FileType.Text      // 파일의 타입

    init {
        fileExtensionCheck()
        localeStringCheck()
    }

    /**
     * 파일의 확장자를 체크하는 함수
     */
    private fun fileExtensionCheck() {
        val check: Boolean = file.name.endsWith(FILE_EXTENSION_IMAGE)
        if (check) {
            filePath = file.path
            val idx = filePath.lastIndexOf(".")
            val path = filePath.substring(0, idx) + FILE_EXTENSION_IMAGE_SUMMARY
            val lines = TextManager.openText(path)
            fileType = FileType.Image
            fileTitle = imgTitleName
            oneLinePreview = when (lines != "") {
                true -> lines
                false -> ""
            }
        } else {
            val lines = TextManager.getLines(file, 2)
            fileType = FileType.Text
            fileTitle = lines!![0]
            filePath = file.path
            if (lines.size > 1) {
                oneLinePreview = lines[1]
            }
        }
    }


    /**
     * 파일의 수정날짜를 체크하는 함수
     * 로케일에 따라서 알맞게 설정한다.
     */
    private fun localeStringCheck() {
        modifiedDate = when (locale) {
            Locale.KOREA.displayCountry -> SimpleDateFormat(DATE_FORMAT_MAIN_KOREA, Locale.KOREA).format(Date(file.lastModified()))
            Locale.UK.displayCountry    -> SimpleDateFormat(DATE_FORMAT_MAIN_UK, Locale.UK).format(Date(file.lastModified()))
            else                        -> SimpleDateFormat(DATE_FORMAT_MAIN_USA, Locale.US).format(Date(file.lastModified()))
        }
    }
}