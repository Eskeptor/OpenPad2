package com.esk.openpadnew.Util

import java.security.MessageDigest

/**
 * MD5 값을 생성해주는 클래스
 */
class CreateMD5 {
    companion object {
        /**
         * MD5 값을 생성한다.
         * @param string MD5을 만들 문장
         */
        fun create(string: String): String {
            val digest: MessageDigest = MessageDigest.getInstance("MD5")
            val md5 = StringBuilder()
            digest.update(string.toByteArray(Charsets.UTF_8))
            val hash: ByteArray = digest.digest()
            for (h in hash) {
                md5.append(String.format("%02x", h))
            }

            return md5.toString()
        }
    }
}